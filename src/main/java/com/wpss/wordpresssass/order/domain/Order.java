package com.wpss.wordpresssass.order.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Order {

    private final Long id;
    private final Long tenantId;
    private final Long siteId;
    private final String orderNo;
    private final String customerFirstName;
    private final String customerLastName;
    private final String customerEmail;
    private final String customerPhone;
    private final String country;
    private final String state;
    private final String city;
    private final String addressLine1;
    private final String postalCode;
    private final String currency;
    private final BigDecimal subtotalAmount;
    private final BigDecimal shippingAmount;
    private final BigDecimal taxAmount;
    private final BigDecimal totalAmount;
    private final OrderStatus orderStatus;
    private final PaymentStatus paymentStatus;
    private final ShippingStatus shippingStatus;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Order(Long id,
                 Long tenantId,
                 Long siteId,
                 String orderNo,
                 String customerFirstName,
                 String customerLastName,
                 String customerEmail,
                 String customerPhone,
                 String country,
                 String state,
                 String city,
                 String addressLine1,
                 String postalCode,
                 String currency,
                 BigDecimal subtotalAmount,
                 BigDecimal shippingAmount,
                 BigDecimal taxAmount,
                 BigDecimal totalAmount,
                 OrderStatus orderStatus,
                 PaymentStatus paymentStatus,
                 ShippingStatus shippingStatus,
                 LocalDateTime createdAt,
                 LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.siteId = siteId;
        this.orderNo = orderNo;
        this.customerFirstName = customerFirstName;
        this.customerLastName = customerLastName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.country = country;
        this.state = state;
        this.city = city;
        this.addressLine1 = addressLine1;
        this.postalCode = postalCode;
        this.currency = currency;
        this.subtotalAmount = subtotalAmount;
        this.shippingAmount = shippingAmount;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.orderStatus = orderStatus;
        this.paymentStatus = paymentStatus;
        this.shippingStatus = shippingStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Order create(Long tenantId,
                               Long siteId,
                               String customerFirstName,
                               String customerLastName,
                               String customerEmail,
                               String customerPhone,
                               String country,
                               String state,
                               String city,
                               String addressLine1,
                               String postalCode,
                               String currency,
                               BigDecimal subtotalAmount,
                               BigDecimal shippingAmount,
                               BigDecimal taxAmount,
                               BigDecimal totalAmount) {
        LocalDateTime now = LocalDateTime.now();
        return new Order(
                null,
                tenantId,
                siteId,
                generateOrderNo(now),
                customerFirstName,
                customerLastName,
                customerEmail,
                customerPhone,
                country,
                state,
                city,
                addressLine1,
                postalCode,
                currency,
                subtotalAmount,
                shippingAmount,
                taxAmount,
                totalAmount,
                OrderStatus.CREATED,
                PaymentStatus.UNPAID,
                ShippingStatus.NOT_SHIPPED,
                now,
                now
        );
    }

    public Order withId(Long newId) {
        return new Order(
                newId,
                tenantId,
                siteId,
                orderNo,
                customerFirstName,
                customerLastName,
                customerEmail,
                customerPhone,
                country,
                state,
                city,
                addressLine1,
                postalCode,
                currency,
                subtotalAmount,
                shippingAmount,
                taxAmount,
                totalAmount,
                orderStatus,
                paymentStatus,
                shippingStatus,
                createdAt,
                updatedAt
        );
    }

    private static String generateOrderNo(LocalDateTime now) {
        return "ORD" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getSiteId() {
        return siteId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getCustomerFirstName() {
        return customerFirstName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getCountry() {
        return country;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public ShippingStatus getShippingStatus() {
        return shippingStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
