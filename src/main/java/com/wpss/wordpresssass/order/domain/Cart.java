package com.wpss.wordpresssass.order.domain;

import com.wpss.wordpresssass.catalog.domain.Product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Cart implements Serializable {

    private final Long siteId;
    private final List<CartItem> items;

    public Cart(Long siteId, List<CartItem> items) {
        this.siteId = siteId;
        this.items = List.copyOf(items);
    }

    public static Cart empty(Long siteId) {
        return new Cart(siteId, List.of());
    }

    public Cart addItem(Product product, int quantity) {
        List<CartItem> updated = new ArrayList<>(items);
        for (int index = 0; index < updated.size(); index++) {
            CartItem existing = updated.get(index);
            if (existing.getProductId().equals(product.getId())) {
                updated.set(index, existing.increment(quantity));
                return new Cart(siteId, updated);
            }
        }
        updated.add(CartItem.fromProduct(product, quantity));
        return new Cart(siteId, updated);
    }

    public Cart updateQuantity(Long productId, int quantity) {
        List<CartItem> updated = new ArrayList<>();
        for (CartItem item : items) {
            if (!item.getProductId().equals(productId)) {
                updated.add(item);
                continue;
            }
            if (quantity > 0) {
                updated.add(item.withQuantity(quantity));
            }
        }
        return new Cart(siteId, updated);
    }

    public Cart removeItem(Long productId) {
        return updateQuantity(productId, 0);
    }

    public int totalQuantity() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public BigDecimal subtotal() {
        return items.stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Long getSiteId() {
        return siteId;
    }

    public List<CartItem> getItems() {
        return items;
    }
}
