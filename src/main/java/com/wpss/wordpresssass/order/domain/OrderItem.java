package com.wpss.wordpresssass.order.domain;

import com.wpss.wordpresssass.order.domain.CartItem;

import java.math.BigDecimal;

public class OrderItem {

    private final Long id;
    private final Long tenantId;
    private final Long orderId;
    private final Long productId;
    private final String sku;
    private final String productTitle;
    private final String sizeValue;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal lineTotal;

    public OrderItem(Long id,
                     Long tenantId,
                     Long orderId,
                     Long productId,
                     String sku,
                     String productTitle,
                     String sizeValue,
                     int quantity,
                     BigDecimal unitPrice,
                     BigDecimal lineTotal) {
        this.id = id;
        this.tenantId = tenantId;
        this.orderId = orderId;
        this.productId = productId;
        this.sku = sku;
        this.productTitle = productTitle;
        this.sizeValue = sizeValue;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public static OrderItem fromCartItem(Long tenantId, Long orderId, CartItem item) {
        return new OrderItem(
                null,
                tenantId,
                orderId,
                item.getProductId(),
                item.getSku(),
                item.getTitle(),
                null,
                item.getQuantity(),
                item.getUnitPrice(),
                item.lineTotal()
        );
    }

    public OrderItem withId(Long newId) {
        return new OrderItem(newId, tenantId, orderId, productId, sku, productTitle, sizeValue, quantity, unitPrice, lineTotal);
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public String getSizeValue() {
        return sizeValue;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
