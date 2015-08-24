package com.github.mysite.order.service;

import com.github.mysite.common.service.IBaseService;
import com.github.mysite.order.vo.Order;
import com.github.mysite.order.vo.OrderQueryModel;

/**
 * description:
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/20 - 14:17
 */
public interface IOrderService extends IBaseService<Order, OrderQueryModel> {
    
    public OrderQueryModel getByCustomerId(String customerId);
}
