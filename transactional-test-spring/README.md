#### Spring代码提交事物示例
```bash
public class App {

	@Autowired
	private PlatformTransactionManager transactionManager;

	public void transactionalTest() {
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		// 设置Session（连接）的事物隔离级别
		definition.setPropagationBehavior(TransactionDefinition.ISOLATION_READ_COMMITTED);
		TransactionStatus status = transactionManager.getTransaction(definition);
		try {
			// 业务逻辑写在这里
			
			
			// 提交事物
			transactionManager.commit(status);
		}catch(Exception e) {
			transactionManager.rollback(status);
		}
	}
}
```