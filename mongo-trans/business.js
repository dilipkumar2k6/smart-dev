var mongodb = require('mongodb');
var mongoskin = require('mongoskin');
var util = require('util');
var assert = require('assert');
var transaction = require('./transaction');
var Commitment = transaction.Commitment;

var db = exports.db = mongoskin.db('127.0.0.1:27017/test');


transaction.init(function(){
  return db;
});

var COLL_USER = exports.COLL_USER = 'user';
var COLL_LOG = exports.COLL_LOG = 'mylog';

// 测试故障恢复的标志（根据这个标志随机的强制发生故障）
var flagTestFailOver = false;

console.log('Node.js version: %s', process.env.NODE_VERSION);


// ================   业务逻辑    ========================

/**
 * 具体的业务逻辑，实现从from账户转移积分至to账户。
 * @transId 事务ID（required）
 * @data 业务逻辑操作数据（required）
 * @flag 是否在出现错误时仍然正常返回（故障恢复时使用）（required）
 * 返回：成功必须返回所有涉及到的文档ID（用于commit）
 */
var doScoreTransfer = exports.doScoreTransfer = function (transId, data, flag, fnCallback) {
  var ids = [];
  console.log(data);
  // 扣除用户A的分数10，并与事务记录关联，表示此记录已更新但可能会被回滚。注意将事务ID作为更新记录的条件，避免重复更新，用于故障恢复时找到恢复点。  
  var from = data.update[0].data.from;
  db.collection(COLL_USER).findAndModify({name:from, pendingTransactions:{$ne:transId}},  transaction.QUERY_SINGLE_ORDER,
      {$inc:{score:-10}, $push:{pendingTransactions:transId}}, {safe:true, 'new':true}, function(err, result) {
    if(err) {
      console.log('  [ERROR] %s %s', util.inspect(err), result);
      return fnCallback();
    }
    if(!result) {
      console.log('  [Fail] No score deducted from user %s', from);
      if(flag)return fnCallback();
    }
    else {
      console.log('  Deducted score from user A, %s left', result.score);
    }

    // 模拟故障（5%的概率）
    if(flagTestFailOver === 'true') {
      if(Math.random() > 0.95) {
        console.log('[ERROR] Ooops! system halted during update biz data, transaction ID is %s', transId);
        process.exit();
        return ;
      }
    }

    if(result)ids.push(result._id);

    // 增加用户B的分数10，其余同上。 
    var to = data.update[0].data.to;
    db.collection(COLL_USER).findAndModify({name:to, state:{$ne:'locked'}, pendingTransactions:{$ne:transId}},  transaction.QUERY_SINGLE_ORDER ,  
        {$inc:{score:10}, $push:{pendingTransactions:transId}}, {safe:true, 'new':true}, function(err, result) {  
      if(err) {
        console.log('  [Fail] %s %s', util.inspect(err),  result);
        return fnCallback();
      }  
      if(!result) {
        console.log('  No score transfered to user %s', to);
        if(flag){
          fnCallback();
        } 
        else {
          fnCallback(ids);
        }
      }
      else {
        ids.push(result ? result._id : null);
        console.log('  Transfer to B: %s', result.score);
        fnCallback(ids);
      }  
    });  
  });  
}


/**
 * 具体于转账业务逻辑的回滚业务逻辑。
 * @param trans
 * @param fnCallback
 */
var rollbackScoreTransfer = exports.rollbackScoreTransfer = function(trans, fnCallback) {
  console.log('Rollback score transfer');
  // B用户的操作一定没有完成，无需处理，直接返还积分给A用户，同时需要清除与事务的关联。  
  db.collection(COLL_USER).update({name:trans.from, pendingTransactions:trans._id},   
    {$inc:{score: trans.score}, $pull:{pendingTransactions:trans._id}}, {safe:true}, function(err, result) {
      if(err) {
        return fnCallback();
      }
      fnCallback(true);// 不管更新结果是多少个，都已完成rollback。
  });
};

// TODO
var doDeduction = exports.doDeduction = function(transId, data, flag,fnCallback) {
  console.log('Do deduction business');

}


// 回滚账户B的扣除操作
function rollbackDeduction(trans, fnCallback) {

  // 删除日志
  db.collection(COLL_LOG).delete({name:trans.insert.user, timestamp:trans.insert.timestamp}, function(err, result) {
    if(err) {
      return fnCallback();
    }

    // 返还从B扣除的积分
    db.collection(COLL_USER).update({name:trans.update.name, pendingTransactions:trans._id},   
      {$inc:{score: -trans.update.score}, $pull:{pendingTransactions:trans._id}}, {safe:true}, function(err, result) {
        if(err) {
          return fnCallback();
        }
        fnCallback(true);// 不管更新结果怎样，都完成rollback并返回成功，因为回滚操作可能会执行多次。
    });
  });
}


