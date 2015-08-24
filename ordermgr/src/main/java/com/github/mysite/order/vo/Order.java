package com.github.mysite.order.vo;

import com.github.mysite.common.vo.BaseModel;
import com.google.common.base.MoreObjects;

import java.math.BigDecimal;

/**
 * description:
 *
 * @author: jy.chen
 * @version: 1.0
 * @since: 2015/8/20 - 11:10
 */
public class Order extends BaseModel {
    private String orderId;
    private BigDecimal orderPrice;
    private String customerId;
    private Integer orderStatus;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("orderId", orderId)
                .add("orderPrice", orderPrice)
                .add("customerId", customerId)
                .add("orderStatus", orderStatus)
                .toString();
    }
}
