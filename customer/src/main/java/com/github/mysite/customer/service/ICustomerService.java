package com.github.mysite.customer.service;

import com.github.mysite.common.service.IBaseService;
import com.github.mysite.customer.vo.CustomerModel;
import com.github.mysite.customer.vo.CustomerQueryModel;

public interface ICustomerService extends IBaseService<CustomerModel, CustomerQueryModel> {
	public CustomerModel getByConditionPage(String customerId);
}
