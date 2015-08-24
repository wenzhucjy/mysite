package com.github.mysite.customer;

import com.github.mysite.common.pageutil.PageInfo;
import com.github.mysite.customer.service.ICustomerService;
import com.github.mysite.customer.vo.CustomerModel;
import com.github.mysite.customer.vo.CustomerQueryModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:Customer test module.
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/21 - 9:49
 */
@Service
@Transactional
public class CustomerClientTest {
    @Autowired
    private ICustomerService s = null;

    public ICustomerService getS() {
        return s;
    }

    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        CustomerClientTest t = (CustomerClientTest) ctx.getBean("customerClientTest");

        //CustomerModel cm = new CustomerModel();
        //cm.setCustomerId("c1");
        //cm.setPwd("c1");
        //cm.setRegisterTime("111");
        //cm.setShowName("c1");
        //cm.setTrueName("张三");
        //t.getS().create(cm);
        
        CustomerQueryModel cqm = new CustomerQueryModel();
        cqm.getPageInfo().setCurrentPage(2);
        cqm.getPageInfo().setPageSize(6);
        
        PageInfo<CustomerModel> p = t.getS().getByConditionPage(cqm);
        
        System.out.println("customer list==" + p);
        
    }

}