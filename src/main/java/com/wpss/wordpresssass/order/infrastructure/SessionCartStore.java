package com.wpss.wordpresssass.order.infrastructure;

import com.wpss.wordpresssass.order.domain.Cart;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class SessionCartStore {

    private static final String SESSION_KEY = "storefront.cart.by.site";

    public Optional<Cart> load(Long siteId, HttpSession session) {
        return Optional.ofNullable(carts(session).get(siteId));
    }

    public void save(Cart cart, HttpSession session) {
        Map<Long, Cart> carts = carts(session);
        if (cart.isEmpty()) {
            carts.remove(cart.getSiteId());
        } else {
            carts.put(cart.getSiteId(), cart);
        }
        if (carts.isEmpty()) {
            session.removeAttribute(SESSION_KEY);
        } else {
            session.setAttribute(SESSION_KEY, carts);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Cart> carts(HttpSession session) {
        Object stored = session.getAttribute(SESSION_KEY);
        if (stored instanceof Map<?, ?> map) {
            return (Map<Long, Cart>) map;
        }
        Map<Long, Cart> carts = new LinkedHashMap<>();
        session.setAttribute(SESSION_KEY, carts);
        return carts;
    }
}
