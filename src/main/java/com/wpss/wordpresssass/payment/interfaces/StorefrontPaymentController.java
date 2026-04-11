package com.wpss.wordpresssass.payment.interfaces;

import com.wpss.wordpresssass.order.domain.Order;
import com.wpss.wordpresssass.order.domain.OrderRepository;
import com.wpss.wordpresssass.payment.application.StorefrontPaymentApplicationService;
import com.wpss.wordpresssass.storefront.application.HostDomainResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class StorefrontPaymentController {

    private final StorefrontPaymentApplicationService storefrontPaymentApplicationService;
    private final HostDomainResolver hostDomainResolver;
    private final OrderRepository orderRepository;

    public StorefrontPaymentController(StorefrontPaymentApplicationService storefrontPaymentApplicationService,
                                       HostDomainResolver hostDomainResolver,
                                       OrderRepository orderRepository) {
        this.storefrontPaymentApplicationService = storefrontPaymentApplicationService;
        this.hostDomainResolver = hostDomainResolver;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/payments/orders/{orderNo}/initiate")
    public ModelAndView initiate(@RequestHeader(value = "Host", required = false) String hostHeader,
                                 @PathVariable("orderNo") String orderNo,
                                 @RequestParam(value = "providerCode", required = false) String providerCode) {
        return resolveOrderContext(hostHeader, orderNo)
                .flatMap(context -> storefrontPaymentApplicationService.initiatePayment(
                        context.order().getTenantId(),
                        context.order().getSiteId(),
                        orderNo,
                        providerCode
                ))
                .map(path -> new ModelAndView("redirect:" + path))
                .orElseGet(() -> fallback(hostHeader));
    }

    @GetMapping("/payments/mock/{paymentNo}")
    public ModelAndView mockPayment(@RequestHeader(value = "Host", required = false) String hostHeader,
                                    @PathVariable("paymentNo") String paymentNo) {
        return storefrontPaymentApplicationService.resolveMockPayment(paymentNo)
                .map(page -> new ModelAndView("storefront/mock-payment")
                        .addObject("page", page))
                .orElseGet(() -> fallback(hostHeader));
    }

    @PostMapping("/payments/mock/{paymentNo}/callback")
    public ModelAndView mockCallback(@RequestHeader(value = "Host", required = false) String hostHeader,
                                     @PathVariable("paymentNo") String paymentNo) {
        return storefrontPaymentApplicationService.mockCallback(paymentNo)
                .map(path -> new ModelAndView("redirect:" + path))
                .orElseGet(() -> fallback(hostHeader));
    }

    private java.util.Optional<OrderContext> resolveOrderContext(String hostHeader, String orderNo) {
        return hostDomainResolver.resolve(hostHeader)
                .flatMap(site -> orderRepository.findByOrderNo(site.getTenantId(), site.getId(), orderNo)
                        .map(OrderContext::new));
    }

    private ModelAndView fallback(String hostHeader) {
        ModelAndView modelAndView = new ModelAndView("storefront/fallback");
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        modelAndView.addObject("requestedHost", hostHeader == null ? "" : hostHeader);
        return modelAndView;
    }

    private record OrderContext(Order order) {
    }
}
