package com.bookstore.order.service;

import com.bookstore.common.exception.BusinessException;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.Shipment;
import com.bookstore.order.mapper.OrderMapper;
import com.bookstore.order.mapper.ShipmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final OrderMapper orderMapper;
    private final ShipmentMapper shipmentMapper;

    private static final String[] COMPANIES = {"顺丰速运", "中通快递", "圆通速递", "韵达快递", "京东物流", "EMS"};
    private static final Random RND = new Random();

    @Transactional
    public Shipment shipOrder(Long orderId, String company, String trackingNo) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (order.getStatus() != 1) throw new BusinessException("只有已支付订单才能发货");

        Shipment shipment = new Shipment();
        shipment.setOrderId(orderId);
        shipment.setOrderNo(order.getOrderNo());
        shipment.setCompany(company);
        shipment.setTrackingNo(trackingNo);
        shipment.setStatus("PICKED");
        shipment.setShipTime(LocalDateTime.now());
        shipmentMapper.insert(shipment);

        order.setStatus(2);
        orderMapper.updateById(order);

        log.info("订单发货: {} 物流: {} {}", order.getOrderNo(), company, trackingNo);
        return shipment;
    }

    public Shipment getByOrder(Long orderId) {
        return shipmentMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Shipment>()
                .eq(Shipment::getOrderId, orderId)
                .orderByDesc(Shipment::getCreateTime)
                .last("LIMIT 1"));
    }

    /**
     * 模拟物流轨迹（按时间节点自动生成）
     */
    public List<Map<String, Object>> getTracking(Long orderId) {
        Shipment shipment = getByOrder(orderId);
        if (shipment == null) return Collections.emptyList();

        Order order = orderMapper.selectById(orderId);
        String[] nodes = {"商家已拣货", "快递已揽收", "运输中", "到达目的地", "派送中", "已签收"};
        String[] statuses = {"PICKED", "PICKED", "TRANSIT", "TRANSIT", "DELIVERING", "SIGNED"};

        List<Map<String, Object>> tracking = new ArrayList<>();
        LocalDateTime base = order.getPayTime() != null ? order.getPayTime() : LocalDateTime.now().minusHours(24);

        for (int i = 0; i < nodes.length; i++) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("time", base.plusHours(i * 6 + RND.nextInt(4)).toString().replace("T", " "));
            node.put("status", nodes[i]);
            node.put("location", "物流中心分拣-" + (i + 1));
            tracking.add(node);
        }
        return tracking;
    }

    @Transactional
    public Shipment autoShip(Long orderId) {
        String company = COMPANIES[RND.nextInt(COMPANIES.length)];
        String trackingNo = "SF" + System.currentTimeMillis() % 10000000000L;
        return shipOrder(orderId, company, trackingNo);
    }
}
