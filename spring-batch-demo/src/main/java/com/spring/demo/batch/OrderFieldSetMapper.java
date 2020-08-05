package com.spring.demo.batch;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import java.time.Instant;
import java.time.ZoneId;

public class OrderFieldSetMapper implements FieldSetMapper<Order> {

    // Received the fieldSet parsed from the csv file
    @Override
    public Order mapFieldSet(FieldSet fieldSet) throws BindException {
        Order order = new Order();
        order.setOrderId(fieldSet.readLong("order_id"));
        order.setCost(fieldSet.readBigDecimal("cost"));
        order.setEmail(fieldSet.readString("email"));
        order.setFirstName(fieldSet.readString("first_name"));
        order.setLastName(fieldSet.readString("last_name"));
        order.setItemId(fieldSet.readString("item_id"));
        order.setItemName(fieldSet.readString("item_name"));
        order.setShipDate(
                Instant.ofEpochMilli(fieldSet.readDate("ship_date", "MM/dd/yyyy").getTime())
                        .atZone(ZoneId.systemDefault()).toLocalDateTime()
        );

        return order;
    }
}
