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


var COLL_TRANSACTION = 'transaction';

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
 * @data TODO user-defined business data bind to transaction, used to recover after system failure.
 * @fnCallback return (transId) if success.
 */
exports.createTransaction = function(operation, fnCallback) {
  var data = operation.data;
  data['state'] = 'initial';
  data['createtime'] = Date.now();
  getdb().collection(COLL_TRANSACTION).insert(data, function(err, results) {
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
  console.log('[T] Unbind %j with transaction ', commitments);
  var n = 0;
  var fail = false;
  // Unbind asynchrnously.
  for(var i=0;i<commitments.length;i++) {
    var r = commitments[i];
    //console.log('  %j', util.inspect(r));
    var tb = r.collection, op = r.op, id = r.id;
    getdb().collection(tb).findAndModify({_id:getdb().toObjectID(id)}, QUERY_SINGLE_ORDER , {$pull:{pendingTransactions:transId}}, {safe:true, 'new':true}, function(err, result) {  
      if(err || !result) {
        // 无需处理错误，交给恢复程序处理。
        console.warn('  [T] Failed to unbind with transaction, let recover process handle it. %j', util.inspect(err));
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
      getdb().collection(COLL_TRANSACTION).findOne({_id:getdb().toObjectID(transId)}, function(err, trans) {
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
    getdb().collection(COLL_TRANSACTION).findOne({_id:getdb().toObjectID(transId)},function(err, trans) {  
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
  getdb().collection(COLL_TRANSACTION).findAndModify({_id:getdb().toObjectID(transId)}, QUERY_SINGLE_ORDER, {$set:{state:newState}}, {safe:true, 'new':true}, function(err, result) {  
    if(err || !result) {
      console.log('[T] Failed to change transaction state to %j for %j', newState, transId);
        return;
      }
      fnCallback(result);
  });
};

// db.transaction.find({state:'pendding'});
// 从故障中恢复，暂时只能处理一种业务逻辑. TODO
// @fnCallbackPending(total, transId) 处理pending状态事务的回调
// 
exports.recoverPendingTransactions = function(fnCallbackPending) {
  console.log('[T] Try to recover from pending transactions');
  // Search 'initial' and 'pending' transactions and recover;
  getdb().collection(COLL_TRANSACTION).findItems({state:{$in:['initial', 'pending']}}, {safe:true}, function(err, transs) {
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

// db.transaction.find({state:'committed'}, {}, {limit:10});
// 恢复处于committed的事务。
// @fnCallbackCommitted(total, trans) 处理commited状态事务的回调
exports.recoverCommittedTransactions = function(fnCallbackCommitted) {
  console.log('[T] Try to recover from committed transactions');
  // Search 'committed' transactions and recover;
  getdb().collection(COLL_TRANSACTION).findItems({state:'committed'}, function(err, transs) {
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


/**
 * 记录事务关联的业务逻辑数据（insert，update和delete的数据），该数据记录在transaction表中。
 * 用于故障恢复程序执行时用来恢复数据状态用的。
 */
exports.Operation = function() {
  this.data = {};
};
//exports.Operation.prototype. = function() {
//  
//};
//插入操作不可能事先知道ID，因此只记录关联的集合名称，恢复时通过查找该集合内含有待恢复事务ID的文档。
// 但是，如果不记录文档的唯一键，仅仅靠事务ID来确定一条记录的话，可能会误把“更新”操作当成“插入”操作检索出来。
// @uniqueKeys 唯一标识一个文档的键。
exports.Operation.prototype.add_insert = function(coll, uniqueKeys) {
  var inslist = this.data['insert'];
  if(!inslist){
    inslist = [];
    this.data['insert'] = inslist;
  }
  inslist.push({coll:coll, data:uniqueKeys});
  return this;
};
// @data JSON格式表示的所需要更新的字段值（部分字段）
exports.Operation.prototype.add_update = function(coll, data) {
  var inslist = this.data['update'];
  if(!inslist){
    inslist = [];
    this.data['update'] = inslist;
  }
  inslist.push({coll:coll, data:data});
  return this;
};
// @data JSON格式，必须是删除掉的整个文档所有字段值。
exports.Operation.prototype.add_delete = function(coll, data) {
  var inslist = this.data['delete'];
  if(!inslist){
    inslist = [];
    this.data['delete'] = inslist;
  }
  inslist.push({coll:coll, doc:data});
  return this;
};
