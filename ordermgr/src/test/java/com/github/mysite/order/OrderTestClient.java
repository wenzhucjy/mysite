package com.github.mysite.order;

import com.github.mysite.common.pageutil.PageInfo;
import com.github.mysite.order.service.IOrderService;
import com.github.mysite.order.vo.Order;
import com.github.mysite.order.vo.OrderQueryModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * description: Order test module.
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/24 - 17:14
 */
@Service
@Transactional
public class OrderTestClient {
    @Autowired
    private IOrderService s = null;

    public IOrderService getS() {
        return s;
    }

    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        OrderTestClient t = (OrderTestClient) ctx.getBean("orderTestClient");

        Order order = new Order();
        order.setCustomerId("1");
        order.setOrderId(System.currentTimeMillis() + "");
        order.setOrderStatus(0);
        order.setOrderPrice(new BigDecimal(0));
        t.getS().create(order);

        OrderQueryModel cqm = new OrderQueryModel();
        cqm.getPageInfo().setCurrentPage(2);
        cqm.getPageInfo().setPageSize(6);

        PageInfo<Order> p = t.getS().getByConditionPage(cqm);

        System.out.println("order list==" + p);

    }

}
