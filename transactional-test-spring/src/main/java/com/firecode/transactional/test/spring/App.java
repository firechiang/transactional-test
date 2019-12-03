package com.firecode.transactional.test.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Hello world!
 *
 */
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
