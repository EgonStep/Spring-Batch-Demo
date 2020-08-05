package com.spring.demo.batch;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {

    private Long orderId;

    private String firstName;

    private String lastName;

    @Pattern(regexp = ".*\\.gov")
    private String email;

    private BigDecimal cost;

    private String itemId;

    private String itemName;

    private LocalDateTime shipDate;
}
