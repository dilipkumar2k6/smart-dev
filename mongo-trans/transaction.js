/**
 * Transaction Module for MongoDB.
 * Author: Yuxing Wang
 * Version: 0.1
 * Require Module: mongodb, mongoskin
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
      console.log('[T]   Fail to create transaction %s ', util.inspect(err));
      return fnCallback();
    }
    var transId = results[0]._id;
    console.log('[T]   Transaction created: %s', transId);
    fnCallback(transId);
  });
};

/**
 * Begin a new created transaction.
 */
exports.beginTransaction = function(transId, fnCallback) {  
  console.log('[T] Begin transaction: %s', transId);
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
  console.log('[T] Commit transaction: %s', transId);

  changeTransState(transId, 'committed', function(trans) {

    exports.unbindWithTransaction(transId, commitment, function(result) {
      if(result) {
        //assert.ok(result.pendingTransactions && result.pendingTransactions.length === 0, 'Pending transactions leak, failover may be needed ' + result.pendingTransactions);
        exports.endTransaction(transId, function(message) {
          console.log('%s', message);
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
  console.log('[T] Unbind %s with transaction ', util.inspect(commitments));
  var n = 0;
  var fail = false;
  // Unbind all asynchrnously.
  for(var i=0;i<commitments.length;i++) {
    var r = commitments[i];
    //console.log('  %s', util.inspect(r));
    var tb = r.collection, op = r.op, id = r.id;
    getdb().collection(tb).findAndModify({_id:getdb().toObjectID(id)}, QUERY_SINGLE_ORDER , {$pull:{pendingTransactions:transId}}, {safe:true, 'new':true}, function(err, result) {  
      // 正常情况下result一定会返回记录的，所以如果result不存在，则表示失败。
      if(err || !result) {
        // 无需处理错误，交给恢复程序处理。
        console.warn('  [T]   Failed to unbind %s with transaction, let recover process handle it. %s', id, util.inspect(err));
        fail = true;
        fnCallback();
      }
      if(++n == commitments.length) {
        fnCallback(true);
      }
    });
  }
};

/**
 * Rollback transaction by executing provided user-defined operation.
 */
exports.rollback = function(transId, fnOperation, fnCallback) {
  console.log('[T] Rollback transaction: %s', transId);
    // 先将事务状态变为'canceling'  
    changeTransState(transId, 'canceling', function(trans) {
      // 开始具体的回滚操作  
      getdb().collection(COLL_TRANSACTION).findOne({_id:getdb().toObjectID(transId)}, function(err, trans) {
        fnOperation(trans, function(result) {
          if(!result) {
            return fnCallback();
          }
          // 完成事务，将事务状态变为'canceled'， 回滚结束  
          exports.endTransaction(transId, function(message) {
            console.log(message);
            fnCallback(message);
	        });
        });
      });
  });  
};

/**
 * End transaction for either 'commit' or 'rollback' operation, depend on transaction state itself.
 */
exports.endTransaction = function(transId, fnCallback) {
  console.log('[T] End transaction: %s', transId);
    getdb().collection(COLL_TRANSACTION).findOne({_id:getdb().toObjectID(transId)},function(err, trans) {  
      if(trans.state == 'committed') {
        changeTransState(transId, 'done', function(result) {
          // 其他处理  
          fnCallback('[T]   Transaction done'); 
       });  
      }
      else if(trans.state == 'canceling') {  
        changeTransState(transId, 'canceled', function(result) {
          // 其他处理  
          fnCallback('[T]   Transaction canceled'); 
        });  
      }
    else {
      console.log('[T]   Unkown transaction state: %s', trans);
    }
  });  
};

/**
 * Change transaction state.
 */
function changeTransState(transId, newState, fnCallback) {
  getdb().collection(COLL_TRANSACTION).findAndModify({_id:getdb().toObjectID(transId)}, QUERY_SINGLE_ORDER, 
      {$set:{state:newState, lastupdatetime:Date.now()}}, {safe:true, 'new':true}, function(err, result) {  
    if(err || !result) {
      console.log('[T]   Failed to change transaction state to %s for %s', newState, transId);
        return;
      }
      fnCallback(result);
  });
};

// db.transaction.find({state:'pendding'});
// 从故障中恢复，暂时只能处理一种业务逻辑. TODO
// @fnCallbackPending(total, transId) 处理pending状态事务的回调
// 
exports.recoverEachPendingTransaction = function(fnCallbackPending) {
  console.log('[T] ==== Try to recover from pending transactions ====');
  // Search 'initial' and 'pending' transactions and recover;
  getdb().collection(COLL_TRANSACTION).find({state:'pending'}, {batchSize:10}, function(err, cursor) {
    if(err) {
      console.log('   [ERROR] %s', err);
      return fnCallbackCommitted();
    }
    if(!cursor) {
      console.log('[T]   No pending transactions to be recovered');
      return fnCallbackPending(0);
    }
    //console.log('[T]   Found %d pending transactions to recover', cursor.totalNumberOfRecords);
    var idx = 0;
    cursor.each(function(err, trans) {
      if(err) {
        console.log('[T]   Fail to recover: %s', util.inspect(err));
        return fnCallbackPending();
      }
      if(!trans) {
        console.log('[T]   End of pending transactions. %d', idx);
        return fnCallbackPending(idx);
      }
      console.log('[T]   Pending transaction: %s', trans._id);
      fnCallbackPending(idx++, trans);
    });

  });
};

// db.transaction.find({state:'committed'}, {}, {limit:10});
// 恢复处于committed的事务。
// @fnCallbackCommitted(total, trans) 处理commited状态事务的回调
exports.recoverEachCommittedTransaction = function(fnCallbackCommitted) {
  console.log('[T] ==== Try to recover from committed transactions ====');
  // Search 'committed' transactions and recover;
  getdb().collection(COLL_TRANSACTION).find({state:'committed'}, {batchSize:10}, function(err, cursor) {
    if(err) {
      console.log('   [ERROR] %s', err);
      return fnCallbackCommitted();
    }
    if(!cursor) {
      console.log('[T]   No pending transactions to be recovered');
      return fnCallbackCommitted();
    }
    //console.log('[T]   Found %d committed transactions to recover', cursor.totalNumberOfRecords);
    var idx = 0;
    cursor.each(function(err, trans) {
      if(err) {
        console.log('[T]   Fail to recover: %s', util.inspect(err));
        return fnCallbackCommitted();
      }
      if(!trans) {
        console.log('[T]   End of committted transactions. %d', idx);
        return fnCallbackCommitted(idx);
      }
      fnCallbackCommitted(idx++, trans);
    });
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
  // 1. {insert -> [{coll:<coll name>, data:<unique key of deleted doc>}]}
  // 2. {update -> [{coll:<coll name>, data:<updated data in a doc>}]}
  // 3. {delete -> [{coll:<coll name>, data:<deleted doc>, children:[{coll:<>, ...}, ...]}]}
  this.data = {};
  this.fnBusiness;
  this.fnRollback;
};
/**
 * “插入”操作必须记录文档的唯一键，仅仅靠事务ID来确定一条记录的话，可能会误把“更新”操作当成“插入”操作检索出来。
 * 如果没有唯一键，那么恢复程序不做处理（假定此类集合是可以存在冗余的）
 * @uniqueKeys 唯一标识一个文档的键。
 */
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
// 删除有几种情况：
// 1. 删除的文档有关联的父文档，这不需要特殊处理。
// 2. 删除的文档有关联的父文档被更新了，这不需要特殊处理（恢复时有父文档的ID保存着）。
// 3. 删除的文档有关联的子文档（也被删除了），这需要记录关联的子文档，恢复时重新insert并建立关联。
// 4. 删除的文档有关联的子文档，但是没有被删除，这种情况不受支持（父文档删除子文档也就无效了，况且记录子文档的ID并且恢复是更新子文档外键也很麻烦）。
// @doc JSON格式，必须是删除掉的整个文档所有字段值。
exports.Operation.prototype.add_delete = function(coll, doc) {
  var inslist = this.data['delete'];
  if(!inslist){
    inslist = [];
    this.data['delete'] = inslist;
  }
  inslist.push({coll:coll, doc:doc});
  return this;
};
