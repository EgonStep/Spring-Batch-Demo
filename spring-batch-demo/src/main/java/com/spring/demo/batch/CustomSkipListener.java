package com.spring.demo.batch;

import org.springframework.batch.core.SkipListener;

public class CustomSkipListener implements SkipListener<Order, TrackedOrder> {
    @Override
    public void onSkipInRead(Throwable throwable) {

    }

    @Override
    public void onSkipInWrite(TrackedOrder trackedOrder, Throwable throwable) {

    }

    @Override
    public void onSkipInProcess(Order order, Throwable throwable) {
        System.out.println("Skipping Processing of Item With id: " + order.getOrderId());
    }
}
