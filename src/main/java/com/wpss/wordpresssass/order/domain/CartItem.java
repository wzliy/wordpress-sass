package com.wpss.wordpresssass.order.domain;

import com.wpss.wordpresssass.catalog.domain.Product;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartItem implements Serializable {

    private final Long productId;
    private final String sku;
    private final String title;
    private final String coverImage;
    private final BigDecimal unitPrice;
    private final int quantity;

    public CartItem(Long productId,
                    String sku,
                    String title,
                    String coverImage,
                    BigDecimal unitPrice,
                    int quantity) {
        this.productId = productId;
        this.sku = sku;
        this.title = title;
        this.coverImage = coverImage;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public static CartItem fromProduct(Product product, int quantity) {
        return new CartItem(
                product.getId(),
                product.getSku(),
                product.getTitle(),
                product.getCoverImage(),
                product.getPrice(),
                quantity
        );
    }

    public CartItem increment(int delta) {
        return new CartItem(productId, sku, title, coverImage, unitPrice, quantity + delta);
    }

    public CartItem withQuantity(int newQuantity) {
        return new CartItem(productId, sku, title, coverImage, unitPrice, newQuantity);
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public String getTitle() {
        return title;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }
}
