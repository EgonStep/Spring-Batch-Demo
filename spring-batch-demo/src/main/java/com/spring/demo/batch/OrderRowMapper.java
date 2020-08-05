package com.spring.demo.batch;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;

public class OrderRowMapper implements RowMapper<Order> {

    @Override
    public Order mapRow(ResultSet resultSet, int i) throws SQLException {
        Order order = new Order();
        order.setOrderId(resultSet.getLong("order_id"));
        order.setCost(resultSet.getBigDecimal("cost"));
        order.setEmail(resultSet.getString("email"));
        order.setFirstName(resultSet.getString("first_name"));
        order.setLastName(resultSet.getString("last_name"));
        order.setItemId(resultSet.getString("item_id"));
        order.setItemName(resultSet.getString("item_name"));
        order.setShipDate(
                Instant.ofEpochMilli(resultSet.getDate("ship_date").getTime())
                        .atZone(ZoneId.systemDefault()).toLocalDateTime()
        );
        return order;
    }
}
