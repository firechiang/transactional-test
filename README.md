### Transaction（事物）相关使用和说明（注意：正常情况下@Transactional不能保证多个数据源的事物）
#### 一、事物ACID特性
 - A: 原子性（一个事物当中多个操作，要么都成功要么都失败）
 - C: 一致性（一个事物完成以后，它的状态改变是一致的。比如两个账户相互转账，事物完成以后两个账户的总和是不变的）
 - I: 隔离性（多个事物试图操控同一段数据，它们是相互独立的（所以操作数据我们都要设置隔离级别））
 - D: 持久性（事物完成以后数据才进入数据库进行持久化）

#### 二、Transaction（事物）隔离级别（注意：两个事物修改同一行数据是串行顺序执行的）
 - Read Uncommitted: 未提交读（可以读取未提交事务的数据）；（可能出现脏读（就是可能读取到错误数据因为有的没有提交事物嘛））
 - Read Committed: 提交读（只能读取已提交事物的数据）；（解决脏读问题，可能出现不可重复读问题（就是同一个事物内读取同一条数据，结果可能不一致））
 - Repeatable Read: 可重复读（同一个事物内重复读取同一条数据它的结果是一致的。即便是在重复读取的同时有另一个事物将数据修改了，它读到的也是以前的数据（未修改前的））；（解决不可重复读问题，可能出现幻读（就是在读取事物的同时有其它事物将数据修改了，等读取事物提交后，立即再发起查询，结果可能不一样））
 - Serializable: 序列化（事务串行化顺序执行（就是所有事务都是同步执行））；（解决所有问题，但是效率低，一般不推荐使用）
```bash 
# 查看数据库的事物隔离级别
# 注意：@@global.tx_isolation（数据库默认的事物隔离级别），@@tx_isolation（Session（当前连接）的事物隔离级别）
$ SELECT @@global.tx_isolation,@@tx_isolation; 

# 修改Session（当前连接）的事物隔离级别为read uncommitted（未提交读）
$ SET SESSION TRANSACTION ISOLATION LEVEL read uncommitted
```

#### 三、Spring事物传播特性
 - Propagation.REQUIRED（当前有事物就直接拿来用，没有就开启一个新的事物）
 - Propagation.SUPPORTS（当前有事物就用事物，没有就不用事物）
 - Propagation.MANDATORY（当前必须要有事物，没有就抛出异常）
 - Propagation.REQUIRES_NEW（不管当前有没有事物（如果有事物就挂起），都会开启一个新的事物）
 - Propagation.NOT_SUPPORTED（不管当前有没有事物（如果有事物就挂起），都不会在事物里执行）
 - Propagation.NEVER（不允许事物存在，如果有事物就抛出异常）
 - Propagation.NESTED（嵌套事物，相当于在一个大事物里面嵌套一个小事物）

#### 四、SQL方式模拟事物提交
```bash 
BEGIN TRANSACTION; -- 开始事物
update t_user set name = 'maomao' where id = 1;
update t_dept set amount = 10 where id = 1;
COMMIT; -- 提交事物
-- ROLLBACK;（回滚事物）
```

#### 五、分布式事物应用案例
 - TCC模式实现思路（每个事物都需要实现三个逻辑）
```bash
1，try（检查资源，预留资源，返回资源）
2，confirm（执行业务，使用上一步返回的资源，提交业务）
3，cancel（出错回滚资源，释放资源）
```
 - XA与最后资源博弈（一个事物里面包含XA事物和非XA事物）
```bash 
1，Start Messaging Transaction（开始事物）
2，Receive Message
3，Start JTA Transaction on DB（开始JTA事物）
4，Update database（只改数据）
5，Phase-1 Commit on DB Transaction（提交第一阶段事物）
6，Commit Messaging Transaction（提交非XA事物）
7，Phase-2 Commit on DB Transaction（提交第二阶段事物）
```
- 最大努力一次提交事物
```bash
1，依次提交事物
2，可能出错
3，通过AOP或Listener实现事物直接的同步
```
- （JMS+MySQL）最大努力一次提交事物+重试
```bash
1，适用于一个数据源是MQ，并且事物由读MQ开始
2，利用MQ消息的重试机制，保证原子性
3，重试的时候需要考虑重复消费消息的问题
@Bean
public ConnectionFactory connectionFactory() {
	ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
	TransactionAwareConnectionFactoryProxy proxy = new TransactionAwareConnectionFactoryProxy();
	proxy.setTargetConnectionFactory(factory);
	// 是否同步到本地事物管理器
	proxy.setSynchedLocalTransactionAllowed(true);
	return proxy;
}

@Bean
public JmsTemplate jmsTemplate() {
	JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());
	// 是否使用事物管理连接
	jmsTemplate.setSessionTransacted(true);
	return jmsTemplate;
}
```
- 链式事物管理（多个事物在一个事物管理器里面依次提交）
```bash
@Bean
public PlatformTransactionManager platformTransactionManager() {
	// user数据源事物管理器
	DataSourceTransactionManager userTransactionManager = new DataSourceTransactionManager(userDataSource());
	// order数据源事物管理器
	DataSourceTransactionManager orderTransactionManager = new DataSourceTransactionManager(orderDataSource());
	// 多数据源事物管理器（有了多数据源事物管理器@Transactional注解可以保证多个数据源在同一个事物内完成）
	ChainedTransactionManager chainedTransactionManager = new ChainedTransactionManager(userTransactionManager,orderTransactionManager);
	return chainedTransactionManager;
}
```
 - 分布式唯一ID生成
```bash
1，UUID：唯一ID标准，128位，32个长度
2，MongoDB的ObjectId：时间戳+机器ID+进程ID+序列
```

