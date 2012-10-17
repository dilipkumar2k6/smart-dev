var mongodb = require('mongodb');
var mongoskin = require('mongoskin');
var util = require('util');
var assert = require('assert');
var transaction = require('./transaction');
var base = require('./business');

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



// ================   测试程序    ========================

// 从A转移10个积分给处于非锁定状态的B。（测试同时更新的事务）
function testTransactionSuccess(fnCallback){
  var operation = new transaction.Operation();
  operation.add_update(COLL_USER, {from:'allenny.iteye.com', to:'bar.iteye.com', score:10});

  transaction.createTransaction(operation, function(transId) {
    if(transId) {
      transaction.beginTransaction(transId, function(transId) {
        base.doScoreTransfer(transId, operation, true, function(ids) {
          if(ids && ids.length > 0) {
            var commitments = [new Commitment(COLL_USER, ids[0]), new Commitment(COLL_USER, ids[1])];
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

// 从账户B扣除10分并做扣分日志。(测试新建文档和更新文档复合的事务)
function testTransactionWithInsertSuccess(fnCallback) {
  var operation = new transaction.Operation();
  var logTime = Date.now();
  operation.add_insert(COLL_LOG, {user:'bar.iteye.com',timestamp:logTime}).add_update(COLL_USER, {name:'bar.iteye.com', score:-10});
//  transaction.createTransaction({insert:[{coll:COLL_LOG}], update:[{coll:COLL_USER, name:'bar.iteye.com', score:-10}]}, function(transId) {
  transaction.createTransaction(operation, function(transId) {
    if(transId) {
      transaction.beginTransaction(transId, function(transId) {
        
        // 开始业务逻辑
        base.db.collection(COLL_USER).findAndModify(
            {name:'bar.iteye.com', pendingTransactions:{$ne:transId}}, transaction.QUERY_SINGLE_ORDER,
            {$inc:{score:-10}, $push:{pendingTransactions:transId}}, {safe:true, 'new':true}, function(err, user){
          if(err || !user) {
            console.log('[T] 从账户%s扣除积分失败： %s ', 'bar,iteye.com', util.inspect(err));
            return fnCallback();
          }
          //console.log('[T] 账户 %s', util.inspect(user));
          base.db.collection(COLL_LOG).insert(
              {user:'bar.iteye.com', deductscore:10, timestamp:logTime, pendingTransactions:[transId]}, 
              {safe:true}, function(err, logs) {
            if(err || !logs || logs.length == 0) {
              console.log('[T] 插入扣分日志失败： %s %s', util.inspect(err), util.inspect(logs));
              transaction.rollback(transId, rollbackDeduction, fnCallback); 
            }
            else {
              //console.log('[T] 日志 %s', util.inspect(logs));
              var commitments = [new Commitment(COLL_USER, user._id), new Commitment(COLL_LOG, logs[0]._id)];
              transaction.commit(transId, commitments, fnCallback);              
            }
          });
        });
        
      });
    }
  });
}


// 尝试从B转移10个积分给处于锁定状态的C，此操作应回滚。
function testTransactionFail(fnCallback) {
  var operation = new transaction.Operation();
  operation.add_update(COLL_USER, {from:'bar.iteye.com', to:'car.iteye.com', score:10});
  transaction.createTransaction(operation, function(transId) {
    if(transId) {
      transaction.beginTransaction(transId, function(transId) {
        base.doScoreTransfer(transId, operation, true, function(ids) {
          if(ids && ids.length > 0) {
            var commitments = [new Commitment(COLL_USER, ids[0]), new Commitment(COLL_USER, ids[1])];
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


/**
 * Create 3 test users A B C.
 */
var initTestUsers = exports.initTestUsers = function (fnCallback) {
  base.db.collection(COLL_USER).ensureIndex([['name', 1]], {safe:true, unique:true}, function(err, result) {

    // User A
    base.db.collection(COLL_USER).insert({name:'allenny.iteye.com', score:10000, state:'active'}, {safe:true}, function(err, result) {
      if(err || !result) {
        console.log('[Fail] %s', err);
      }

      // User B
      base.db.collection(COLL_USER).insert({name:'bar.iteye.com', score:0, state:'active'}, {safe:true}, function(err, result) {
        if(err || !result) {
          console.log('[Fail] %s', err);
        }

        // User C
        base.db.collection(COLL_USER).insert({name:'candy.iteye.com', score:0, state:'locked'}, {safe:true}, function(err, result) {
          if(err || !result) {
            console.log('[Fail] %s', err);
          }
          fnCallback();
        });

      });
    });
  });
};

/**
 * $ node <loops> <testFailOver=true:false>
 * 三个账户A，B，C，从A账户转积分至B，从B账户尝试转积分至C（回滚）
 */
var  main = exports.main = function() {

  console.log('[ARGV] %s', util.inspect(process.argv));
  
  var loops  = process.argv[2];
  if(!loops) {
    loops = 1;
  }

  if(process.argv[3]) {
    flagTestFailOver = process.argv[3];
  }

  if(flagTestFailOver === 'true') {
    console.log('Test with system failure'); 
  }

  // 尝试生成测试用户（不存在的话）
  initTestUsers(function() {
    var count = 0;
    
    // 循环若干词执行：成功事务，失败事务，带插入新文档的事务。
    for(var i=0;i<loops;i++) {
      testTransactionSuccess(function() {
        if(++count == loops) {
          count = 0;
          
          for(var j=0;j<loops;j++) {
            testTransactionFail(function() {

              if(++count == loops) {
                
                count = 0;
                for(var k=0;k<loops;k++) {
                  testTransactionWithInsertSuccess(function() {
                    
                    if(++count == loops) {
                      process.exit();
                    }
                  });
                }
              }
            });
          }
          
        }
      });
    }
  });
};

