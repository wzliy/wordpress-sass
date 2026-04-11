package com.wpss.wordpresssass.shipping.interfaces;

import com.wpss.wordpresssass.common.api.ApiResponse;
import com.wpss.wordpresssass.shipping.application.SupplyShipmentApplicationService;
import com.wpss.wordpresssass.shipping.application.command.UpdateSupplyShipmentCommand;
import com.wpss.wordpresssass.shipping.application.dto.SupplyShipmentDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/supply/shipments")
public class SupplyShipmentController {

    private final SupplyShipmentApplicationService supplyShipmentApplicationService;

    public SupplyShipmentController(SupplyShipmentApplicationService supplyShipmentApplicationService) {
        this.supplyShipmentApplicationService = supplyShipmentApplicationService;
    }

    @GetMapping
    public ApiResponse<List<SupplyShipmentDto>> search(@RequestParam(value = "orderNo", required = false) String orderNo,
                                                       @RequestParam(value = "trackingNo", required = false) String trackingNo,
                                                       @RequestParam(value = "customerEmail", required = false) String customerEmail) {
        return ApiResponse.success(supplyShipmentApplicationService.search(orderNo, trackingNo, customerEmail));
    }

    @PutMapping("/{orderNo}")
    public ApiResponse<SupplyShipmentDto> update(@PathVariable("orderNo") String orderNo,
                                                 @RequestBody UpdateSupplyShipmentCommand command) {
        return ApiResponse.success(supplyShipmentApplicationService.update(orderNo, command));
    }
}
