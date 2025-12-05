/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.promotion.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.exception.ResourceAlreadyExistsException;
import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.promotion.domain.entity.Coupon;
import com.xplaza.backend.promotion.domain.entity.DiscountType;
import com.xplaza.backend.promotion.repository.CouponRepository;
import com.xplaza.backend.promotion.repository.DiscountTypeRepository;

@Service
@RequiredArgsConstructor
public class CouponService {
  private final CouponRepository couponRepository;
  private final DiscountTypeRepository discountTypeRepository;

  public List<Coupon> listCoupons() {
    return couponRepository.findAll();
  }

  public List<Coupon> listActiveCoupons() {
    return couponRepository.findByIsActiveTrue();
  }

  public Coupon getCoupon(Long id) {
    return couponRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + id));
  }

  public Coupon getCouponByCode(String code) {
    return couponRepository.findByCouponCode(code)
        .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + code));
  }

  @Transactional
  public Coupon createCoupon(Coupon coupon) {
    if (couponRepository.existsByCouponCode(coupon.getCouponCode())) {
      throw new ResourceAlreadyExistsException("Coupon code already exists: " + coupon.getCouponCode());
    }
    return couponRepository.save(coupon);
  }

  @Transactional
  public Coupon updateCoupon(Long id, Coupon details) {
    Coupon existing = getCoupon(id);
    existing.setCouponDescription(details.getCouponDescription());
    existing.setDiscountValue(details.getDiscountValue());
    existing.setMinimumOrderAmount(details.getMinimumOrderAmount());
    existing.setMaximumDiscountAmount(details.getMaximumDiscountAmount());
    existing.setStartDate(details.getStartDate());
    existing.setEndDate(details.getEndDate());
    existing.setUsageLimit(details.getUsageLimit());
    existing.setIsActive(details.getIsActive());
    if (details.getDiscountType() != null && details.getDiscountType().getDiscountTypeId() != null) {
      DiscountType discountType = discountTypeRepository.findById(details.getDiscountType().getDiscountTypeId())
          .orElseThrow(() -> new ResourceNotFoundException("DiscountType not found"));
      existing.setDiscountType(discountType);
    }
    return couponRepository.save(existing);
  }

  @Transactional
  public void deleteCoupon(Long id) {
    if (!couponRepository.existsById(id)) {
      throw new ResourceNotFoundException("Coupon not found: " + id);
    }
    couponRepository.deleteById(id);
  }
}
