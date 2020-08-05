package com.spring.demo.batch;

import org.springframework.batch.item.ItemProcessor;
import java.util.UUID;

public class TrackedOrderItemProcessor implements ItemProcessor<Order, TrackedOrder> {

    @Override
    public TrackedOrder process(Order order) {
        System.out.println("Processing order with id: " + order.getOrderId());
        System.out.println("Processing with thread " + Thread.currentThread().getName());

        TrackedOrder trackedOrder = new TrackedOrder(order);
        trackedOrder.setTrackingNumber(getTrackingNumber());
        return trackedOrder;
    }

    private String getTrackingNumber() {
        if (Math.random() < .20)
            throw new OrderProcessingException();

        return UUID.randomUUID().toString();
    }
}
