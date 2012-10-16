var mongodb = require('mongodb');
var mongoskin = require('mongoskin');
var util = require('util');
var assert = require('assert');


var transaction = require('./transaction');
var test = require('./test_biz_flow');




// 测试恢复程序
// 恢复程序中重新执行的业务逻辑与正常业务逻辑不同，它不许要在失败的情况下进行回滚？？？？
// 返回：
function testRecoverPending(fnCallback) {

  var handleRecoverWork = function(result) {
    if(!result){
      fnCallback();
    }
    else {
      return fnCallback(true);
    }
  };

  // 处理每一个pending的事务。
  transaction.recoverEachPendingTransaction(function(idx, trans) {
    // 最后一个
    if(!trans) return fnCallback(idx);

    console.log('Redo business');

    // 重新执行业务逻辑
    var transId = trans._id;
    test.doBusiness(transId, trans, false, function(ids) {
      if(ids && ids.length > 0) {
        transaction.commit(transId, [{collection:test.COLL_USER, id:ids[0]}, {collection:test.COLL_USER, id:ids[1]}], function(result) {
          handleRecoverWork(result);
        });  
      }
      else {
        transaction.rollback(transId, test.rollbackScoreTransfer, function(result) {
          handleRecoverWork(result);
        });
      }
    });
  });
}

// 测试恢复committed状态的事务
var testRecoverCommitted = exports.testRecoverCommitted = function(fnCallback) {

  transaction.recoverEachCommittedTransaction(function(idx, trans) {

    // 最后一个
    if(!trans) return fnCallback(idx);

    console.log('Redo commitment: from %s to %s', trans.update.data.from, trans.update.data.to);

    // 重新执行commit业务逻辑
    var transId = trans._id;
    var ids = [];
    test.db.collection(test.COLL_USER).findOne({name:trans.update.data.from}, function(err, u0) {
      if(u0)ids.push(u0._id);

      test.db.collection(test.COLL_USER).findOne({name:trans.update.data.to}, function(err, u1) {
        if(u1)ids.push(u1._id);

        if(ids.length < 2) {
          console.log('  Failed to redo commitment');
          return;
        }

        transaction.unbindWithTransaction(trans._id, [{collection:test.COLL_USER, id:ids[0]}, {collection:test.COLL_USER, id:ids[1]}], function(result) {
          if(!result){
            fnCallback();
          }
          else {
            fnCallback(true);
          }
        });
      });
    });
  });

}


var  main = exports.main = function() {

  // 尝试恢复pendding状态的事务
  testRecoverPending(function(result){
    if(!result && result != 0) {
      console.log(result);
      return;
    }
    console.log('  Done to Test Recover Peding Transactions: %s', result);

    // 尝试恢复committed状态的事务
    testRecoverCommitted(function(result) {
      if(result || result == 0) {
        console.log('  Done to Test Recover Committed Transactions: %s', result);
        process.exit();
      }
    });
  });

}
