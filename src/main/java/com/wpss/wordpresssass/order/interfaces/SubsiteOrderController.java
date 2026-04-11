package com.wpss.wordpresssass.order.interfaces;

import com.wpss.wordpresssass.common.api.ApiResponse;
import com.wpss.wordpresssass.order.application.SubsiteOrderApplicationService;
import com.wpss.wordpresssass.order.application.dto.SubsiteOrderDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/subsite/orders")
public class SubsiteOrderController {

    private final SubsiteOrderApplicationService subsiteOrderApplicationService;

    public SubsiteOrderController(SubsiteOrderApplicationService subsiteOrderApplicationService) {
        this.subsiteOrderApplicationService = subsiteOrderApplicationService;
    }

    @GetMapping
    public ApiResponse<List<SubsiteOrderDto>> list(@RequestParam("siteId") Long siteId,
                                                   @RequestParam(value = "orderNo", required = false) String orderNo,
                                                   @RequestParam(value = "orderStatus", required = false) String orderStatus,
                                                   @RequestParam(value = "paymentStatus", required = false) String paymentStatus,
                                                   @RequestParam(value = "createdFrom", required = false)
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
                                                   @RequestParam(value = "createdTo", required = false)
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo) {
        return ApiResponse.success(subsiteOrderApplicationService.listOrders(
                siteId,
                orderNo,
                orderStatus,
                paymentStatus,
                createdFrom,
                createdTo
        ));
    }
}
