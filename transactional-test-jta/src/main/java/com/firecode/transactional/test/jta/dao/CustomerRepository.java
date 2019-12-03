package com.firecode.transactional.test.jta.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.firecode.transactional.test.jta.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Customer findOneByUsername(String username);

	@Override
	// 不自动同步数据库（就是每次插入数据立马同步到数据库里面）
	@Modifying(clearAutomatically = true)
	Customer save(Customer Customer);
    
    
}
