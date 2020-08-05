package com.spring.demo.batch;

public class Constants {

    private Constants() { /* noop */ }

    public static final String[] CSV_HEADERS = new String[] {
            "order_id", "first_name", "last_name", "email", "cost", "item_id", "item_name", "ship_date"
    };

    public static final String[] ORDER_FIELDS = new String[] {
            "orderId", "firstName", "lastName", "email", "cost", "itemId", "itemName", "shipDate"
    };

    public static final String SELECT_ORDER_SQL = "select order_id, first_name, last_name, email, cost, item_id, item_name, " +
            "ship_date from SHIPPED_ORDER order by order_id";

    // Don't need to use class OrderItemPreparedStatementSetter if Name Parameter is being used
    public static final String INSERT_ORDER_SQL = "INSERT INTO SHIPPED_ORDER_OUTPUT " +
            "(order_id, first_name, last_name, email, item_id, item_name, cost, ship_date) " +
            "VALUES (:orderId, :firstName, :lastName, :email, :itemId, :itemName, :cost, :shipDate)";

    public static final String INSERT_TRACKED_SQL = "INSERT INTO TRACKED_ORDER " +
            "(order_id, first_name, last_name, email, item_id, item_name, cost, ship_date, tracking_number, free_shipping) " +
            "VALUES (:orderId, :firstName, :lastName, :email, :itemId, :itemName, :cost, :shipDate, :trackingNumber, :freeShipping)";
}
