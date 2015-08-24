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

@Service
@Transactional
public class OrderClient {
	@Autowired
	private IOrderService s = null;
	
	public IOrderService getS(){
		return s;
	}
	
	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		OrderClient t = (OrderClient)ctx.getBean("orderClient");
		
		//Order qm = new Order();
		//qm.setCustomerId("2222222222222222");
		//qm.setOrderId(RandomUtils.nextInt(1111, 333336663) + "");
		//qm.setOrderPrice(new BigDecimal(1));
		//qm.setOrderStatus(0);
        //
		//t.getS().create(qm);

        OrderQueryModel cqm = new OrderQueryModel();
        cqm.getPageInfo().setPageNum(1);
        cqm.getPageInfo().setPageSize(2);

        PageInfo<Order> p = t.getS().getByConditionPage(cqm);

        System.out.println("list==" + p);


        //PageInfo<Order> p2 = t.getS().getByConditionPage(cqm);

       // System.out.println("list2222==" + p);
	}
}
