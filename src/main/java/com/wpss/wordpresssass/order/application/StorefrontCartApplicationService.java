package com.wpss.wordpresssass.order.application;

import com.wpss.wordpresssass.catalog.domain.Product;
import com.wpss.wordpresssass.catalog.domain.ProductRepository;
import com.wpss.wordpresssass.order.domain.Cart;
import com.wpss.wordpresssass.order.domain.CartItem;
import com.wpss.wordpresssass.order.infrastructure.SessionCartStore;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.storefront.application.HostDomainResolver;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StorefrontCartApplicationService {

    private final HostDomainResolver hostDomainResolver;
    private final ProductRepository productRepository;
    private final SessionCartStore sessionCartStore;

    public StorefrontCartApplicationService(HostDomainResolver hostDomainResolver,
                                            ProductRepository productRepository,
                                            SessionCartStore sessionCartStore) {
        this.hostDomainResolver = hostDomainResolver;
        this.productRepository = productRepository;
        this.sessionCartStore = sessionCartStore;
    }

    public Optional<StorefrontCartView> resolveCart(String hostHeader, HttpSession session) {
        return hostDomainResolver.resolve(hostHeader)
                .map(site -> toView(site, loadCart(site, session)));
    }

    public boolean addItem(String hostHeader, Long productId, Integer quantity, HttpSession session) {
        return hostDomainResolver.resolve(hostHeader)
                .flatMap(site -> productRepository.findVisibleBySiteAndId(site.getTenantId(), site.getId(), productId)
                        .map(product -> {
                            Cart updatedCart = loadCart(site, session).addItem(product, normalizeAddQuantity(quantity));
                            sessionCartStore.save(updatedCart, session);
                            return true;
                        }))
                .orElse(false);
    }

    public boolean updateQuantity(String hostHeader, Long productId, Integer quantity, HttpSession session) {
        return hostDomainResolver.resolve(hostHeader)
                .map(site -> {
                    Cart updatedCart = loadCart(site, session).updateQuantity(productId, normalizeSetQuantity(quantity));
                    sessionCartStore.save(updatedCart, session);
                    return true;
                })
                .orElse(false);
    }

    public boolean removeItem(String hostHeader, Long productId, HttpSession session) {
        return hostDomainResolver.resolve(hostHeader)
                .map(site -> {
                    Cart updatedCart = loadCart(site, session).removeItem(productId);
                    sessionCartStore.save(updatedCart, session);
                    return true;
                })
                .orElse(false);
    }

    private Cart loadCart(Site site, HttpSession session) {
        return sessionCartStore.load(site.getId(), session)
                .orElseGet(() -> Cart.empty(site.getId()));
    }

    private StorefrontCartView toView(Site site, Cart cart) {
        return new StorefrontCartView(
                site.getId(),
                site.getName(),
                site.getSiteCode(),
                site.getThemeColor() == null ? "#2563EB" : site.getThemeColor(),
                cart.getItems().stream()
                        .map(this::toLineItem)
                        .toList(),
                cart.totalQuantity(),
                cart.subtotal()
        );
    }

    private StorefrontCartView.LineItem toLineItem(CartItem item) {
        return new StorefrontCartView.LineItem(
                item.getProductId(),
                item.getSku(),
                item.getTitle(),
                item.getCoverImage(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.lineTotal()
        );
    }

    private int normalizeAddQuantity(Integer quantity) {
        return quantity == null || quantity < 1 ? 1 : quantity;
    }

    private int normalizeSetQuantity(Integer quantity) {
        return quantity == null || quantity < 0 ? 0 : quantity;
    }
}
