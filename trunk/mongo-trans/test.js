var mongodb = require('mongodb');
var mongoskin = require('mongoskin');
var util = require('util');
var assert = require('assert');
var transaction = require('./transaction');
var Commitment = transaction.Commitment;

var db = mongoskin.db('127.0.0.1:27017/test');


transaction.init(function(){
  return db;
});

var TBN_USER = 'user';

console.log(process.env.NODE_VERSION);


// ================   业务逻辑    ========================

/**
 * 具体的业务逻辑，实现从from账户转移积分至to账户。
 * @flag 是否在出现错误时仍然正常返回
 * 返回：成功必须返回所有涉及到的文档ID（用于commit）
 */
function doBusiness(transId, from, to, flag, fnCallback) {
  var ids = [];
  // 扣除用户A的分数10，并与事务记录关联，表示此记录已更新但可能会被回滚。注意将事务ID作为更新记录的条件，避免重复更新，用于故障恢复时找到恢复点。  
  db.collection(TBN_USER).findAndModify({name:from, pendingTransactions:{$ne:transId}},  transaction.QUERY_SINGLE_ORDER,
    {$inc:{score:-10}, $push:{pendingTransactions:transId}}, {safe:true, 'new':true}, function(err, result) {
    if(err) {
      console.log('  ERROR: %j %j', util.inspect(err), result);
      return fnCallback();
    }
    if(!result) {
      console.log('  No score deducted from user %j', from);
      if(flag)return fnCallback();
    }
    else {
      console.log('  Deducted score from user A, %j left', result.score);
    }

    if(result)ids.push(result._id);

    // 增加用户B的分数10，其余同上。  
    db.collection(TBN_USER).findAndModify({name:to, state:{$ne:'locked'}, pendingTransactions:{$ne:transId}},  transaction.QUERY_SINGLE_ORDER ,  
      {$inc:{score:10}, $push:{pendingTransactions:transId}}, {safe:true, 'new':true}, function(err, result) {  
        if(err) {
          console.log('  [Fail] %j %j', util.inspect(err),  result);
          return fnCallback();
        }  
        if(!result) {
          console.log('  No score transfered to user %j', to);
          if(flag){
            fnCallback();
          } else {
            fnCallback(ids);
          }
        }
        else {
          ids.push(result ? result._id : null);
          console.log('  Transfer to B: %j', result.score);
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
function rollbackScoreTransfer(trans, fnCallback) {
  console.log('Rollback score transfer');
  // B用户的操作一定没有完成，无需处理，直接返还积分给A用户，同时需要清除与事务的关联。  
  db.collection(TBN_USER).update({name:trans.from, pendingTransactions:trans._id},   
    {$inc:{score: trans.score}, $pull:{pendingTransactions:trans._id}}, {safe:true}, function(err, result) {
      if(err) {
        return fnCallback();
      }
      fnCallback(true);// 不管更新结果是多少个，都已完成rollback。
  });
}  

// ================   测试程序    ========================

// 从A转移10个积分给处于非锁定状态的B。
function testTransactionSuccess(fnCallback){
  transaction.createTransaction({from:'allenny.iteye.com', to:'bar.iteye.com', score:10}, function(transId) {
    if(transId) {
      transaction.beginTransaction(transId, function(transId) {
        doBusiness(transId, 'allenny.iteye.com', 'bar.iteye.com', true, function(ids) {
          if(ids && ids.length > 0) {
            var commitments = [new Commitment(TBN_USER, ids[0]), new Commitment(TBN_USER, ids[1])];
            transaction.commit(transId, commitments, fnCallback);  
          }
          else {
            transaction.rollback(transId, rollbackScoreTransfer, fnCallback); 
          }
        });
      });
    }
  });
}

// 尝试从B转移10个积分给处于锁定状态的C，此操作应回滚。
function testTransactionFail(fnCallback) {
  transaction.createTransaction({from:'bar.iteye.com', to:'car.iteye.com', score:10}, function(transId) {
    if(transId) {
      transaction.beginTransaction(transId, function(transId) {
        doBusiness(transId, 'bar.iteye.com', 'car.iteye.com', true, function(ids) {
          if(ids && ids.length > 0) {
            var commitments = [new Commitment(TBN_USER, ids[0]), new Commitment(TBN_USER, ids[1])];
            transaction.commit(transId, commitments, fnCallback);  
          }
          else {
            transaction.rollback(transId, rollbackScoreTransfer, fnCallback); 
          }
        });
      });
    }
  });
}


// 测试恢复程序
// 恢复程序中重新执行的业务逻辑与正常业务逻辑不同，它不许要在失败的情况下进行回滚？？？？
// 返回：
function testRecoverPending(fnCallback) {
  var count = 0;
  var handleRecoverWork = function(result, total) {
    if(!result){
      fnCallback();
    }
    else {
      if(++count == total) fnCallback(true);
    }
  };

  transaction.recoverPendingTransactions(function(total, transId) {

    if(!total || !transId) return fnCallback();

    // 重新执行业务逻辑
    doBusiness(transId, 'bar.iteye.com', 'car.iteye.com', false, function(ids) {
      if(ids && ids.length > 0) {
        transaction.commit(transId, [{collection:TBN_USER, id:ids[0]}, {collection:TBN_USER, id:ids[1]}], function(result) {
          handleRecoverWork(result, total);
        });  
      }
      else {
        transaction.rollback(transId, rollbackScoreTransfer, function(result) {
         handleRecoverWork(result, total);
        });
      }
    });
  });
}

function testRecoverCommitted(fnCallback) {
  var count = 0;
  transaction.recoverCommittedTransactions(function(total, trans) {

    if(!total || !trans) return fnCallback();

    // 重新执行commit业务逻辑
    var ids = [];
    db.collection(TBN_USER).findOne({name:trans.from}, function(err, u0) {
      if(u0)ids.push(u0._id);
      db.collection(TBN_USER).findOne({name:trans.to}, function(err, u1) {
        if(u1)ids.push(u1._id);
        transaction.unbindWithTransaction(trans._id, [{collection:TBN_USER, id:ids[0]}, {collection:TBN_USER, id:ids[1]}], function(result) {
          if(!result){
            fnCallback();
          }
          else {
            if(++count == total) fnCallback(true);
          }
        });
      });
    });
  });

}


function initTestUsers(fnCallback) {
  db.collection(TBN_USER).ensureIndex([['name', 1]], {safe:true, unique:true}, function(err, result) {

    db.collection(TBN_USER).insert({name:'allenny.iteye.com', score:10000, state:'active'}, {safe:true}, function(err, result) {
      if(err || !result) {
        console.log(result);
        //return;
      }

      db.collection(TBN_USER).insert({name:'bar.iteye.com', score:0, state:'active'}, {safe:true}, function(err, result) {
        if(err || !result) {
          console.log(result);
          //return;
        }
        db.collection(TBN_USER).insert({name:'candy.iteye.com', score:0, state:'locked'}, {safe:true}, function(err, result) {
          if(err || !result) {
            console.log(result);
            //return;
          }
          fnCallback();
        });

      });
    });
  });
}

/**
 * 三个账户A，B，C，从A账户转积分至B，从B账户尝试转积分至C（回滚）
**/
function main() {
  
  var loops  = process.argv[2];
  if(!loops) {
    loops = 10;
  }

  testRecoverPending(function(result){
    //if(result) {

      testRecoverCommitted(function(result) {
        //if(result) {

          initTestUsers(function() {
            var count = 0;
            
            for(var i=0;i<loops;i++) {
              testTransactionSuccess(function() {
                if(++count == loops) {
                  count = 0;
                  
                  for(var i=0;i<loops;i++) {
                    testTransactionFail(function() {

                      if(++count == loops) {
                        process.exit();
                      }
                    });
                  }
                  
                }
              });
            }
            
          });

        //}
        //else {
        //  process.exit();
        //}
      });
    //}
    //else {
    //  process.exit();
    //}
  });

}

// 
main();




