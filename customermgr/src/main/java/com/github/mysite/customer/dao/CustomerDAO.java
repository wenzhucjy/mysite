package com.github.mysite.customer.dao;

import com.github.mysite.common.annotation.MyBatisDao;
import com.github.mysite.common.dao.BaseDAO;
import com.github.mysite.customer.vo.CustomerModel;
import com.github.mysite.customer.vo.CustomerQueryModel;

@MyBatisDao
public interface CustomerDAO extends BaseDAO<CustomerModel,CustomerQueryModel> {
	public CustomerModel getByConditionPage(String customerId);
}
