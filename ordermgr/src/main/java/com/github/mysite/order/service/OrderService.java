package com.github.mysite.order.service;

import com.github.mysite.common.service.BaseService;
import com.github.mysite.order.dao.OrderDAO;
import com.github.mysite.order.vo.Order;
import com.github.mysite.order.vo.OrderQueryModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/20 - 14:17
 */
@Service
@Transactional
public class OrderService extends BaseService<Order, OrderQueryModel> implements IOrderService {
    private OrderDAO dao = null;

    @Autowired
    private void setDao(OrderDAO dao) {
        this.dao = dao;
        super.setDAO(dao);
    }
    @Override
    public OrderQueryModel getByCustomerId(String customerId) {
        return null;
    }
}
