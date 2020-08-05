package com.spring.demo.batch;

import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

public class FreeShippingOrderItemProcessor implements ItemProcessor<TrackedOrder, TrackedOrder> {

    @Override
    public TrackedOrder process(TrackedOrder item) {
        item.setFreeShipping(
                item.getCost().compareTo(BigDecimal.valueOf(80L)) > 0
        );

        return item.isFreeShipping() ? item : null;
    }
}
