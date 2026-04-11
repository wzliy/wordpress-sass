package com.wpss.wordpresssass.order.application;

import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.email.application.OrderEmailApplicationService;
import com.wpss.wordpresssass.order.application.command.CheckoutFormCommand;
import com.wpss.wordpresssass.order.domain.Cart;
import com.wpss.wordpresssass.order.domain.Order;
import com.wpss.wordpresssass.order.domain.OrderItem;
import com.wpss.wordpresssass.order.domain.OrderItemRepository;
import com.wpss.wordpresssass.order.domain.OrderRepository;
import com.wpss.wordpresssass.order.infrastructure.SessionCartStore;
import com.wpss.wordpresssass.shipping.domain.ShipmentRecord;
import com.wpss.wordpresssass.shipping.domain.ShipmentRecordRepository;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.storefront.application.HostDomainResolver;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class StorefrontCheckoutApplicationService {

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("100.00");
    private static final BigDecimal STANDARD_SHIPPING = new BigDecimal("9.90");
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final HostDomainResolver hostDomainResolver;
    private final SessionCartStore sessionCartStore;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderEmailApplicationService orderEmailApplicationService;
    private final ShipmentRecordRepository shipmentRecordRepository;

    public StorefrontCheckoutApplicationService(HostDomainResolver hostDomainResolver,
                                                SessionCartStore sessionCartStore,
                                                OrderRepository orderRepository,
                                                OrderItemRepository orderItemRepository,
                                                OrderEmailApplicationService orderEmailApplicationService,
                                                ShipmentRecordRepository shipmentRecordRepository) {
        this.hostDomainResolver = hostDomainResolver;
        this.sessionCartStore = sessionCartStore;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderEmailApplicationService = orderEmailApplicationService;
        this.shipmentRecordRepository = shipmentRecordRepository;
    }

    @Transactional
    public Optional<String> checkout(String hostHeader, CheckoutFormCommand command, HttpSession session) {
        return hostDomainResolver.resolve(hostHeader)
                .flatMap(site -> sessionCartStore.load(site.getId(), session)
                        .filter(cart -> !cart.isEmpty())
                        .map(cart -> createOrder(site, cart, command, session)));
    }

    public Optional<StorefrontOrderSuccessView> resolveSuccess(String hostHeader, String orderNo) {
        return hostDomainResolver.resolve(hostHeader)
                .flatMap(site -> orderRepository.findByOrderNo(site.getTenantId(), site.getId(), orderNo)
                        .map(order -> toSuccessView(site, order, orderItemRepository.findByOrderId(site.getTenantId(), order.getId()))));
    }

    private String createOrder(Site site, Cart cart, CheckoutFormCommand command, HttpSession session) {
        BigDecimal subtotal = cart.subtotal();
        BigDecimal shipping = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0 ? ZERO : STANDARD_SHIPPING;
        BigDecimal tax = ZERO;
        BigDecimal total = subtotal.add(shipping).add(tax);

        Order order = orderRepository.save(Order.create(
                site.getTenantId(),
                site.getId(),
                requireText(command.getFirstName(), "firstName"),
                requireText(command.getLastName(), "lastName"),
                requireText(command.getEmail(), "email"),
                trimToNull(command.getPhone()),
                requireText(command.getCountry(), "country"),
                trimToNull(command.getState()),
                requireText(command.getCity(), "city"),
                requireText(command.getAddressLine1(), "addressLine1"),
                requireText(command.getPostalCode(), "postalCode"),
                site.getCurrencyCode() == null || site.getCurrencyCode().isBlank() ? "USD" : site.getCurrencyCode(),
                subtotal,
                shipping,
                tax,
                total
        ));

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(item -> OrderItem.fromCartItem(site.getTenantId(), order.getId(), item))
                .toList();
        orderItemRepository.saveBatch(orderItems);
        shipmentRecordRepository.save(ShipmentRecord.createDefault(order.getTenantId(), order.getId()));
        orderEmailApplicationService.dispatchOrderPlacedEmail(order);
        sessionCartStore.save(Cart.empty(site.getId()), session);
        return order.getOrderNo();
    }

    private StorefrontOrderSuccessView toSuccessView(Site site, Order order, List<OrderItem> items) {
        return new StorefrontOrderSuccessView(
                site.getId(),
                site.getName(),
                site.getSiteCode(),
                site.getThemeColor() == null ? "#2563EB" : site.getThemeColor(),
                order.getOrderNo(),
                order.getCustomerFirstName() + " " + order.getCustomerLastName(),
                order.getCustomerEmail(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getOrderStatus().name(),
                order.getPaymentStatus().name(),
                order.getCreatedAt(),
                items.stream()
                        .map(this::toLineItem)
                        .toList()
        );
    }

    private StorefrontOrderSuccessView.LineItem toLineItem(OrderItem item) {
        return new StorefrontOrderSuccessView.LineItem(
                item.getProductTitle(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }

    private String requireText(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BusinessException("Checkout field " + fieldName + " is required");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
