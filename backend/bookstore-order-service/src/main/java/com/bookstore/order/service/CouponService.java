package com.bookstore.order.service;

import com.bookstore.common.exception.BusinessException;
import com.bookstore.common.utils.UserContext;
import com.bookstore.order.entity.Coupon;
import com.bookstore.order.entity.UserCoupon;
import com.bookstore.order.mapper.CouponMapper;
import com.bookstore.order.mapper.UserCouponMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    public List<Coupon> listAvailable() {
        return couponMapper.selectList(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getStatus, 1)
                .gt(Coupon::getTotal, 0)
                .apply("claimed < total"));
    }

    public List<UserCoupon> listMyCoupons() {
        Long userId = UserContext.getUserId();
        return userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .orderByDesc(UserCoupon::getClaimTime));
    }

    @Transactional
    public UserCoupon claim(Long couponId) {
        Long userId = UserContext.getUserId();
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            throw new BusinessException("优惠券不存在或已下架");
        }
        if (coupon.getClaimed() >= coupon.getTotal()) {
            throw new BusinessException("优惠券已被领完");
        }

        // 检查是否已领取
        Long count = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId));
        if (count > 0) throw new BusinessException("已领取过该优惠券");

        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus("UNUSED");
        userCouponMapper.insert(uc);

        coupon.setClaimed(coupon.getClaimed() + 1);
        couponMapper.updateById(coupon);

        log.info("用户 {} 领取优惠券: {}", userId, coupon.getName());
        return uc;
    }

    @Transactional
    public BigDecimal useCoupon(Long userCouponId, Long orderId, BigDecimal orderAmount) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc == null || !"UNUSED".equals(uc.getStatus())) {
            throw new BusinessException("优惠券不可用");
        }
        Coupon coupon = couponMapper.selectById(uc.getCouponId());
        if (coupon == null) throw new BusinessException("优惠券不存在");

        BigDecimal discount = BigDecimal.ZERO;
        switch (coupon.getType()) {
            case "FULL_REDUCTION":
                if (orderAmount.compareTo(coupon.getThreshold()) < 0) {
                    throw new BusinessException("未达到满减门槛");
                }
                discount = coupon.getDiscount();
                break;
            case "DISCOUNT":
                // discount存储的是折扣率，如0.80表示8折
                discount = orderAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscount()));
                if (discount.compareTo(new BigDecimal("30")) > 0) {
                    discount = new BigDecimal("30"); // 最高减30
                }
                break;
            case "FREE_SHIPPING":
                discount = new BigDecimal("8"); // 免运费
                break;
        }

        uc.setStatus("USED");
        uc.setUseTime(LocalDateTime.now());
        uc.setOrderId(orderId);
        userCouponMapper.updateById(uc);

        return discount;
    }
}
