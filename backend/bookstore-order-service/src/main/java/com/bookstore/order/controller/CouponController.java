package com.bookstore.order.controller;

import com.bookstore.common.entity.Result;
import com.bookstore.order.entity.Coupon;
import com.bookstore.order.entity.UserCoupon;
import com.bookstore.order.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/available")
    public Result<List<Coupon>> listAvailable() {
        return Result.success(couponService.listAvailable());
    }

    @GetMapping("/my")
    public Result<List<UserCoupon>> listMy() {
        return Result.success(couponService.listMyCoupons());
    }

    @PostMapping("/{id}/claim")
    public Result<UserCoupon> claim(@PathVariable Long id) {
        return Result.success("领取成功", couponService.claim(id));
    }
}
