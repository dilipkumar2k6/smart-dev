var mongodb = require('mongodb');
var mongoskin = require('mongoskin');
var util = require('util');
var assert = require('assert');
var transaction = require('./transaction');
var Commitment = transaction.Commitment;

var db = exports.db = mongoskin.db('127.0.0.1:27017/test');


var COLL_USER = exports.COLL_USER = 'user';
var COLL_LOG = exports.COLL_LOG = 'mylog';


transaction.init(function(){
  return db;
});


// 测试故障恢复的标志（根据这个标志随机的强制发生故障）
var flagTestFailOver = exports.flagTestFailOver = false;

console.log('Node.js version: %s', process.env.NODE_VERSION);


// ================   业务逻辑    ========================

/**
 * 具体的业务逻辑，实现从from账户转移积分至to账户。
 * @transId 事务ID（required）
 * @data 业务逻辑操作数据（required）
 * @continueOnError 是否在出现错误时仍然继续进行（故障恢复时true）（required）
 * 返回：成功必须返回所有涉及到的文档ID（用于commit）
 */
var doScoreTransfer = exports.doScoreTransfer = function (transId, data, continueOnError, fnCallback) {
  // var ids = [];
  var commitments = [];
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
      if(!continueOnError)return fnCallback();
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

    // if(result)ids.push(result._id);
    commitments.push(new Commitment(COLL_USER, result?result._id:null));

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
        if(!continueOnError){
          fnCallback();
        } 
        else {
          fnCallback(commitments);
        }
      }
      else {
        commitments.push(new Commitment(COLL_USER, result?result._id:null));
        console.log('  Transfer to B: %s', result.score);
        fnCallback(commitments);
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
var doDeduction = exports.doDeduction = function(transId, data, continueOnError, fnCallback) {
  console.log('Do deduction business');
  // 开始业务逻辑
  var commitments = [];
  var upuser = data.update[0].data.user;
  var upscore = data.update[0].data.score;
  db.collection(COLL_USER).findAndModify(
      {name:upuser, pendingTransactions:{$ne:transId}}, transaction.QUERY_SINGLE_ORDER,
      {$inc:{score:upscore}, $push:{pendingTransactions:transId}}, {safe:true, 'new':true}, function(err, user){
    if(err || !user) {
      console.log('[T] 从账户%s扣除积分失败： %s ', 'bar,iteye.com', util.inspect(err));
      return fnCallback();
    }
    commitments.push(new Commitment(COLL_USER, user._id));

    //console.log('[T] 账户 %s', util.inspect(user));
    // var insuser = data.insert[0].data.user;
    // var insscore = data.insert[0].data.score;
    // var timestamp = data.insert[0].data.timestamp;
    data.insert[0].data.pendingTransactions = [transId];
    db.collection(COLL_LOG).insert(
        data.insert[0].data, {safe:true}, function(err, logs) {
      if(err || !logs || logs.length == 0) {
        console.log('[T] 插入扣分日志失败： %s %s', util.inspect(err), util.inspect(logs));
        fnCallback();
      }
      else {
        //console.log('[T] 日志 %s', util.inspect(logs));
        commitments.push(new Commitment(COLL_LOG, logs[0]._id));
        fnCallback(commitments);           
      }
    });
  });
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


