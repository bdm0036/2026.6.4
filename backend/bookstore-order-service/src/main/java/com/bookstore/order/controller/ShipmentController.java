package com.bookstore.order.controller;

import com.bookstore.common.entity.Result;
import com.bookstore.order.entity.Shipment;
import com.bookstore.order.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping("/{id}/ship")
    public Result<Shipment> ship(@PathVariable Long id,
                                  @RequestParam String company,
                                  @RequestParam String trackingNo) {
        return Result.success("发货成功", shipmentService.shipOrder(id, company, trackingNo));
    }

    @GetMapping("/{id}/shipment")
    public Result<Shipment> getShipment(@PathVariable Long id) {
        Shipment s = shipmentService.getByOrder(id);
        return Result.success(s);
    }

    @GetMapping("/{id}/tracking")
    public Result<List<Map<String, Object>>> getTracking(@PathVariable Long id) {
        return Result.success(shipmentService.getTracking(id));
    }
}
