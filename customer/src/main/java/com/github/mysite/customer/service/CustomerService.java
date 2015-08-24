package com.github.mysite.customer.service;

import com.github.mysite.common.service.BaseService;
import com.github.mysite.customer.dao.CustomerDAO;
import com.github.mysite.customer.vo.CustomerModel;
import com.github.mysite.customer.vo.CustomerQueryModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerService extends BaseService<CustomerModel, CustomerQueryModel> implements ICustomerService{
	private CustomerDAO dao = null;
	@Autowired
	private void setDao(CustomerDAO dao){
		this.dao = dao;
		super.setDAO(dao);
	}
	public CustomerModel getByConditionPage(String customerId) {
		return dao.getByConditionPage(customerId);
	}
	
}
