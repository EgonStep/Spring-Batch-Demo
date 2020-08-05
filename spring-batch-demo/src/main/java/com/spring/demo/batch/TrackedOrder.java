package com.spring.demo.batch;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

@Data
@EqualsAndHashCode(callSuper = true)
public class TrackedOrder extends Order{

    private String trackingNumber;
    private boolean freeShipping;

    public TrackedOrder(Order order) {
        BeanUtils.copyProperties(order, this);
    }
}
