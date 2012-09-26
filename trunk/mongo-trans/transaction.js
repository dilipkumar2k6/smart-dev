/**
 * Transaction Module for MongoDB.
 * Author: Yuxing Wang
 * Version: 0.1
 * Reference:
 * http://cookbook.mongodb.org/patterns/perform-two-phase-commits/
 *
*/

var mongodb = require('mongodb');
var mongoskin = require('mongoskin');
var util = require('util');
var assert = require('assert');


//var db = mongoskin.db('127.0.0.1:27017/test');


var TBN_TRANSACTION = 'transaction';

var TRANS_INITIAL = 'initial';
var TRANS_PENDING = 'pending';
var TRANS_COMMITTED = 'committed';
var TRANS_DONE = 'done';
var TRANS_CANCELED = 'canceled';
var TRANS_CANCELING = 'canceling';

var QUERY_SINGLE_ORDER = exports.QUERY_SINGLE_ORDER = [['_id', 'asc']];

/**
 * Get DB connection, overwrite by invoking transaction.init(fnGetDB);
 */
var getdb = function() {
  console.log('[WARN] No db connection function provided, "localhost" server and "test" collection used.');
  return mongoskin.db('127.0.0.1:27017/test');
};

/**
 * Init transaction module 
 */
exports.init = function(fnGetDB) {
  getdb = fnGetDB;
};


/**
 * Create transaction with user business logic data, return transaction id if success.
 * @data user-defined business data bind to transaction, used to recover after system failure.
 * @fnCallback return (transId) if success.
 */
exports.createTransaction = function(data, fnCallback) {
  data['state'] = 'initial';
  data['createtime'] = Date.now();
  getdb().collection(TBN_TRANSACTION).insert(data, function(err, results) {
    if(err || !results || results.length == 0) {
      console.log('[T] Fail to create transaction %j ', util.inspect(err));
      return fnCallback();
    }
    var transId = results[0]._id;
    console.log('[T] Transaction created: %j', transId);
    fnCallback(transId);
  });
};

/**
 * Begin a new created transaction.
 */
exports.beginTransaction = function(transId, fnCallback) {  
  console.log('[T] Begin transaction: %j', transId);
  changeTransState(transId, 'pending', function(trans) {
    if(trans) {
      fnCallback(transId);      
    }
  });
};

/**
 * Commit transactions, unbinding with business documents.
 * @commitment business documents identifiers, form as: [{collection:'<collection name>', id:'<doc id>'}, ...]
 */
exports.commit = function(transId, commitment, fnCallback) {  
  console.log('[T] Commit transaction: %j', transId);

  changeTransState(transId, 'committed', function(trans) {

    exports.unbindWithTransaction(transId, commitment, function(result) {
      if(result) {
        //assert.ok(result.pendingTransactions && result.pendingTransactions.length === 0, 'Pending transactions leak, failover may be needed ' + result.pendingTransactions);
        exports.endTransaction(transId, function(message) {
          console.log(message);
          fnCallback(true);
        });
      }
      else {
        fnCallback(false);
      }
    });
  });  
};

/**
 * 
 */
exports.unbindWithTransaction = function(transId, commitments, fnCallback) {
  console.log('[T] Unbind with transaction %j', commitments);
  var n = 0;
  var fail = false;
  for(var i=0;i<commitments.length;i++) {
    var r = commitments[i];
    console.log(util.inspect(r));
    var tb = r.collection, op = r.op, id = r.id;
    getdb().collection(tb).findAndModify({_id:getdb().toObjectID(id)}, QUERY_SINGLE_ORDER , {$pull:{pendingTransactions:transId}}, {safe:true, 'new':true}, function(err, result) {  
      if(err || !result) {
        // 无需处理错误，交给恢复程序处理。
        console.warn('  [FAIL] %j %j', util.inspect(err), result);
        fail = true;
        fnCallback(false);
      }
      if(!fail && ++n == commitments.length) {
        fnCallback(true);
      }
    });
  }
};

/**
 * Rollback transaction by executing provided user-defined operation.
 */
exports.rollback = function(transId, fnOperation, fnCallback) {
  console.log('[T] Rollback transaction: %j', transId);
    // 先将事务状态变为'canceling'  
    changeTransState(transId, 'canceling', function(trans) {
      // 开始具体的回滚操作  
      getdb().collection(TBN_TRANSACTION).findOne({_id:getdb().toObjectID(transId)}, function(err, trans) {
        fnOperation(trans, function(result) {
          if(!result) {
            return fnCallback();
          }
          // 完成事务，将事务状态变为'canceled'， 回滚结束  
          exports.endTransaction(transId, function(result) {
            console.log(result);
            fnCallback(result);
	        });
        });
      });
  });  
};

/**
 * End transaction for either 'commit' or 'rollback' operation, depend on transaction state itself.
 */
exports.endTransaction = function(transId, fnCallback) {
  console.log('[T] End transaction: %j', transId);
    getdb().collection(TBN_TRANSACTION).findOne({_id:getdb().toObjectID(transId)},function(err, trans) {  
      if(trans.state == 'committed') {
        changeTransState(transId, 'done', function(result) {
          // 其他处理  
          fnCallback('[T] Transaction done'); 
       });  
      }
      else if(trans.state == 'canceling') {  
        changeTransState(transId, 'canceled', function(result) {
          // 其他处理  
          fnCallback('[T] Transaction canceled'); 
        });  
      }
    else {
      console.log('[T] Unkown transaction state: %j', trans);
    }
  });  
};

/**
 * Change transaction state.
 */
function changeTransState(transId, newState, fnCallback) {
  getdb().collection(TBN_TRANSACTION).findAndModify({_id:getdb().toObjectID(transId)}, QUERY_SINGLE_ORDER, {$set:{state:newState}}, {safe:true, 'new':true}, function(err, result) {  
    if(err || !result) {
      console.log('[T] Failed to change transaction state to %j for %j', newState, transId);
        return;
      }
      fnCallback(result);
  });
};

// 从故障中恢复，暂时只能处理一种业务逻辑. TODO
// @fnCallbackPending(total, transId) 处理pending状态事务的回调
// 
exports.recoverPendingTransactions = function(fnCallbackPending) {
  console.log('[T] Try to recover from pending transactions');
  // Search 'initial' and 'pending' transactions and recover;
  getdb().collection(TBN_TRANSACTION).findItems({state:{$in:['initial', 'pending']}}, {safe:true}, function(err, transs) {
    if(err) {
      console.log('[T] Fail to recover: %j', util.inspect(err));
      return fnCallbackPending();
    }
    if(transs.length == 0) {
      console.log('[T] No pending transactions to be recovered');
      return fnCallbackPending(0);
    }
    for(var i=0; i<transs.length; i++) {
      var transId = transs[i]._id;
      fnCallbackPending(transs.length, transId);
    }
  });
};

// 恢复处于committed的事务。
// @fnCallbackCommitted(total, trans) 处理commited状态事务的回调
exports.recoverCommittedTransactions = function(fnCallbackCommitted) {
  console.log('[T] Try to recover from committed transactions');
  // Search 'committed' transactions and recover;
  getdb().collection(TBN_TRANSACTION).findItems({state:'committed'}, function(err, transs) {
    if(err) {
      console.log('[T] Fail to recover: %j', util.inspect(err));
      return fnCallbackCommitted();
    }
    if(transs.length == 0) {
      console.log('[T] No committed transactions to be recovered');
      return fnCallbackCommitted(0);
    } 
    for(var i=0; i<transs.length; i++) {
      var trans = transs[i];
      fnCallbackCommitted(transs.length, trans);
    }
  });
};

/**
 * 用于返回commit信息的类，包含的业务逻辑中更新过的文档信息。
 * @param collname
 * @param docid
 * @returns
 */
exports.Commitment = function(collname, docid) {
  this.collection = collname;
  this.id = docid;
};


exports.Operation = function() {
  
};

exports.Operation.insert = function(data) {
  
};