var mongodb = require('mongodb');
var mongoskin = require('mongoskin');
var util = require('util');
var assert = require('assert');


var transaction = require('./transaction');
var test = require('./test_biz_flow');




// ���Իָ�����
// �ָ�����������ִ�е�ҵ���߼�������ҵ���߼���ͬ��������Ҫ��ʧ�ܵ�����½��лع���������
// ���أ�
function testRecoverPending(fnCallback) {

  var handleRecoverWork = function(result) {
    if(!result){
      fnCallback();
    }
    else {
      return fnCallback(true);
    }
  };

  // ����ÿһ��pending������
  transaction.recoverEachPendingTransaction(function(idx, trans) {
    // ���һ��
    if(!trans) return fnCallback(idx);

    console.log('Redo business');

    // ����ִ��ҵ���߼�
    var transId = trans._id;
    test.doBusiness(transId, 'bar.iteye.com', 'car.iteye.com', false, function(ids) {
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

// ���Իָ�committed״̬������
var testRecoverCommitted = exports.testRecoverCommitted = function(fnCallback) {

  transaction.recoverEachCommittedTransaction(function(idx, trans) {

    // ���һ��
    if(!trans) return fnCallback(idx);

    console.log('Redo commitment: from %s to %s', trans.update.data.from, trans.update.data.to);

    // ����ִ��commitҵ���߼�
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

  // ���Իָ�pendding״̬������
  testRecoverPending(function(result){
    if(!result && result != 0) {
      console.log(result);
      return;
    }
    console.log('  Done to Test Recover Peding Transactions: %s', result);

    // ���Իָ�committed״̬������
    testRecoverCommitted(function(result) {
      if(result || result == 0) {
        console.log('  Done to Test Recover Committed Transactions: %s', result);
        process.exit();
      }
    });
  });

}
