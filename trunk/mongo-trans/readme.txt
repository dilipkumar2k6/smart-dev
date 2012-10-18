

实现业务逻辑函数doBiz()，客户程序和故障恢复程序都会调用这个函数，其必须包括以下参数：
@transId 事务ID
@data 业务逻辑相关的数据
@continueOnError 是否在出现错误时仍然继续进行（故障恢复时设置）
@fnCallback(ids) 回调，返回参数为Commitment对象实例数组，每个Commitment包含被更新集合和文档ID，用于提交（commit）事务。doBiz()函数的调用者处理用transaction.commit()函数处理这些Commitment.

在业务逻辑函数doBiz()中更新集合中的某个文档的内容时，务必要加入事务控制（用来避免重复被故障恢复程序重复更新），比如：
  db.collection(COLL_NAME).update(
        {....., pendingTransactions:{$ne:transId}}, {......, $push:{pendingTransactions:transId}}, function(err, user){
  }




实现回滚逻辑函数doRollback()，其必须包含以下参数：
@trans 事务文档记录
@fnCallback 回调
此函数用于业务逻辑中数据库更新操作失败后的回滚（故障恢复程序不会直接调用?）

