package com.github.mysite.order.dao;

import com.github.mysite.common.annotation.MyBatisDao;
import com.github.mysite.common.dao.BaseDAO;
import com.github.mysite.order.vo.Order;
import com.github.mysite.order.vo.OrderQueryModel;

/**
 * description:
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/20 - 14:13
 */
@MyBatisDao
public interface OrderDAO extends BaseDAO<Order, OrderQueryModel> {
    public OrderQueryModel getByCustomerId(String customerId);
}
