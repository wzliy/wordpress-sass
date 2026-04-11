package com.wpss.wordpresssass.payment.application;

import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.order.domain.Order;
import com.wpss.wordpresssass.order.domain.OrderRepository;
import com.wpss.wordpresssass.order.domain.PaymentStatus;
import com.wpss.wordpresssass.payment.domain.PaymentInitiation;
import com.wpss.wordpresssass.payment.domain.PaymentProvider;
import com.wpss.wordpresssass.payment.domain.PaymentRecord;
import com.wpss.wordpresssass.payment.domain.PaymentRecordRepository;
import com.wpss.wordpresssass.payment.domain.PaymentRecordStatus;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StorefrontPaymentApplicationService {

    private final OrderRepository orderRepository;
    private final SiteRepository siteRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final Map<String, PaymentProvider> paymentProviders;

    public StorefrontPaymentApplicationService(OrderRepository orderRepository,
                                               SiteRepository siteRepository,
                                               PaymentRecordRepository paymentRecordRepository,
                                               List<PaymentProvider> paymentProviders) {
        this.orderRepository = orderRepository;
        this.siteRepository = siteRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.paymentProviders = paymentProviders.stream()
                .collect(Collectors.toMap(PaymentProvider::providerCode, Function.identity()));
    }

    @Transactional
    public Optional<String> initiatePayment(Long tenantId, Long siteId, String orderNo, String providerCode) {
        Order order = orderRepository.findByOrderNo(tenantId, siteId, orderNo)
                .orElseThrow(() -> new BusinessException("Order not found"));
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return Optional.of("/order/" + order.getOrderNo() + "/success");
        }
        PaymentProvider paymentProvider = requireProvider(providerCode);
        PaymentRecord paymentRecord = paymentRecordRepository.save(PaymentRecord.create(
                order.getTenantId(),
                order.getId(),
                paymentProvider.providerCode(),
                order.getTotalAmount(),
                order.getCurrency()
        ));
        PaymentInitiation initiation = paymentProvider.initiate(order, paymentRecord);
        return Optional.of(initiation.redirectPath());
    }

    public Optional<StorefrontMockPaymentView> resolveMockPayment(String paymentNo) {
        return paymentRecordRepository.findByPaymentNo(paymentNo)
                .filter(record -> "MOCK".equals(record.getProviderCode()))
                .flatMap(record -> orderRepository.findByIdAndTenantId(record.getTenantId(), record.getOrderId())
                        .flatMap(order -> siteRepository.findByIdAndTenantId(order.getSiteId(), order.getTenantId())
                                .map(site -> toMockView(site, order, record))));
    }

    @Transactional
    public Optional<String> mockCallback(String paymentNo) {
        return paymentRecordRepository.findByPaymentNo(paymentNo)
                .map(record -> {
                    Order order = orderRepository.findByIdAndTenantId(record.getTenantId(), record.getOrderId())
                            .orElseThrow(() -> new BusinessException("Order not found"));
                    PaymentProvider paymentProvider = requireProvider(record.getProviderCode());
                    var callbackResult = paymentProvider.handleCallback(order, record, null);
                    paymentRecordRepository.updateCallbackResult(
                            record.getTenantId(),
                            record.getId(),
                            callbackResult.status(),
                            callbackResult.callbackPayload()
                    );
                    if (callbackResult.status() == PaymentRecordStatus.SUCCEEDED) {
                        orderRepository.updatePaymentStatus(order.getTenantId(), order.getId(), PaymentStatus.PAID);
                    }
                    return "/order/" + order.getOrderNo() + "/success";
                });
    }

    private StorefrontMockPaymentView toMockView(Site site, Order order, PaymentRecord paymentRecord) {
        return new StorefrontMockPaymentView(
                site.getThemeColor() == null ? "#2563EB" : site.getThemeColor(),
                site.getSiteCode(),
                site.getName(),
                paymentRecord.getPaymentNo(),
                order.getOrderNo(),
                paymentRecord.getAmount(),
                paymentRecord.getCurrency(),
                paymentRecord.getStatus().name()
        );
    }

    private PaymentProvider requireProvider(String providerCode) {
        String normalized = providerCode == null || providerCode.isBlank()
                ? "MOCK"
                : providerCode.trim().toUpperCase();
        PaymentProvider provider = paymentProviders.get(normalized);
        if (provider == null) {
            throw new BusinessException("Payment provider not found");
        }
        return provider;
    }
}
