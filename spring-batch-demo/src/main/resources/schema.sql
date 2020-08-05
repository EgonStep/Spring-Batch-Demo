DROP TABLE IF EXISTS SHIPPED_ORDER;
DROP TABLE IF EXISTS SHIPPED_ORDER_OUTPUT;

CREATE TABLE SHIPPED_ORDER (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(250),
    last_name VARCHAR(250),
    email VARCHAR(250),
    cost FLOAT,
    item_id VARCHAR(250),
    item_name VARCHAR(250),
    ship_date DATE
);

CREATE TABLE SHIPPED_ORDER_OUTPUT (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(250),
    last_name VARCHAR(250),
    email VARCHAR(250),
    cost FLOAT,
    item_id VARCHAR(250),
    item_name VARCHAR(250),
    ship_date DATE
);

CREATE TABLE TRACKED_ORDER (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(250),
    last_name VARCHAR(250),
    email VARCHAR(250),
    cost FLOAT,
    item_id VARCHAR(250),
    item_name VARCHAR(250),
    ship_date DATE,
    tracking_number VARCHAR(250),
    free_shipping BOOLEAN
);