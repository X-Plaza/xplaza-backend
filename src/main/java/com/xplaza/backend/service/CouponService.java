/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.domain.Coupon;
import com.xplaza.backend.jpa.dao.CouponDao;
import com.xplaza.backend.jpa.repository.CouponRepository;
import com.xplaza.backend.mapper.CouponMapper;

@Service
@RequiredArgsConstructor
public class CouponService {
  private final CouponRepository couponRepo;
  private final CouponMapper couponMapper;

  @Transactional
  public void addCoupon(Coupon entity) {
    CouponDao dao = couponMapper.toDao(entity);
    couponRepo.save(dao);
    // handle shop links if needed
  }

  @Transactional
  public void updateCoupon(Coupon entity) {
    CouponDao dao = couponMapper.toDao(entity);
    couponRepo.save(dao);
    // handle shop links if needed
  }

  public List<Coupon> listCoupons() {
    return couponRepo.findAll().stream().map(couponMapper::toEntityFromDao).collect(Collectors.toList());
  }

  public Coupon listCoupon(Long id) {
    return couponRepo.findById(id).map(couponMapper::toEntityFromDao).orElse(null);
  }

  public void deleteCoupon(Long id) {
    couponRepo.deleteById(id);
  }

  public boolean checkCouponValidity(String couponCode, Double netOrderAmount, Long shopId) {
    Coupon coupon = findCouponByCode(couponCode);
    if (coupon == null) {
      return false;
    }

    return isDateValid(coupon) &&
        isActiveValid(coupon) &&
        isMinAmountValid(coupon, netOrderAmount) &&
        isShopValid(coupon, shopId);
  }

  public Double calculateCouponAmount(String couponCode, Double netOrderAmount) {
    Coupon coupon = findCouponByCode(couponCode);
    if (coupon == null) {
      return 0.0;
    }
    Double amount = coupon.getAmount();
    Double maxAmount = coupon.getMaxAmount();
    Long discountTypeId = coupon.getDiscountTypeId();
    Double calculated = 0.0;
    // Assume discountTypeId==2 means percentage, else fixed
    if (discountTypeId != null && discountTypeId == 2) {
      calculated = (netOrderAmount * amount) / 100.0;
      if (maxAmount != null && calculated > maxAmount)
        calculated = maxAmount;
    } else {
      calculated = amount;
    }
    return calculated;
  }

  private Coupon findCouponByCode(String couponCode) {
    Optional<CouponDao> daoOpt = couponRepo.findAll().stream()
        .filter(dao -> couponCode.equals(dao.getCouponCode()))
        .findFirst();
    return daoOpt.map(couponMapper::toEntityFromDao).orElse(null);
  }

  private boolean isDateValid(Coupon coupon) {
    if (coupon.getStartDate() != null && coupon.getEndDate() != null) {
      Date now = new Date();
      return !now.before(coupon.getStartDate()) && !now.after(coupon.getEndDate());
    }
    return true;
  }

  private boolean isActiveValid(Coupon coupon) {
    return coupon.getIsActive() == null || coupon.getIsActive();
  }

  private boolean isMinAmountValid(Coupon coupon, Double netOrderAmount) {
    return coupon.getMinShoppingAmount() == null || netOrderAmount >= coupon.getMinShoppingAmount();
  }

  private boolean isShopValid(Coupon coupon, Long shopId) {
    if (coupon.getCouponShopLinks() == null || coupon.getCouponShopLinks().isEmpty()) {
      return true; // No shop restriction
    }
    return coupon.getCouponShopLinks().stream()
        .anyMatch(link -> shopId.equals(link.getShop().getShopId()));
  }
}
