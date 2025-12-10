/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.promotion.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xplaza.backend.exception.ResourceAlreadyExistsException;
import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.promotion.domain.entity.Coupon;
import com.xplaza.backend.promotion.domain.entity.DiscountType;
import com.xplaza.backend.promotion.repository.CouponRepository;
import com.xplaza.backend.promotion.repository.DiscountTypeRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService Unit Tests")
class CouponServiceTest {

  @Mock
  private CouponRepository couponRepository;

  @Mock
  private DiscountTypeRepository discountTypeRepository;

  @InjectMocks
  private CouponService couponService;

  private Coupon testCoupon;
  private DiscountType testDiscountType;

  @BeforeEach
  void setUp() {
    testDiscountType = new DiscountType();
    testDiscountType.setDiscountTypeId(1L);
    testDiscountType.setDiscountTypeName("Percentage");

    testCoupon = new Coupon();
    testCoupon.setCouponId(1L);
    testCoupon.setCouponCode("SAVE10");
    testCoupon.setCouponDescription("Save 10%");
    testCoupon.setDiscountValue(10.0);
    testCoupon.setDiscountType(testDiscountType);
    testCoupon.setIsActive(true);
  }

  @Nested
  @DisplayName("listCoupons Tests")
  class ListCouponsTests {

    @Test
    @DisplayName("Should return list of all coupons")
    void shouldReturnAllCoupons() {
      when(couponRepository.findAll()).thenReturn(Arrays.asList(testCoupon));

      List<Coupon> result = couponService.listCoupons();

      assertNotNull(result);
      assertEquals(1, result.size());
      verify(couponRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return list of active coupons")
    void shouldReturnActiveCoupons() {
      when(couponRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(testCoupon));

      List<Coupon> result = couponService.listActiveCoupons();

      assertNotNull(result);
      assertEquals(1, result.size());
      verify(couponRepository, times(1)).findByIsActiveTrue();
    }
  }

  @Nested
  @DisplayName("getCoupon Tests")
  class GetCouponTests {

    @Test
    @DisplayName("Should return coupon by id")
    void shouldReturnCouponById() {
      when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));

      Coupon result = couponService.getCoupon(1L);

      assertNotNull(result);
      assertEquals(1L, result.getCouponId());
      assertEquals("SAVE10", result.getCouponCode());
    }

    @Test
    @DisplayName("Should return coupon by code")
    void shouldReturnCouponByCode() {
      when(couponRepository.findByCouponCode("SAVE10")).thenReturn(Optional.of(testCoupon));

      Coupon result = couponService.getCouponByCode("SAVE10");

      assertNotNull(result);
      assertEquals("SAVE10", result.getCouponCode());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when coupon not found")
    void shouldThrowExceptionWhenCouponNotFound() {
      when(couponRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> couponService.getCoupon(999L));
    }
  }

  @Nested
  @DisplayName("createCoupon Tests")
  class CreateCouponTests {

    @Test
    @DisplayName("Should successfully create a new coupon")
    void shouldCreateCoupon() {
      when(couponRepository.existsByCouponCode("SAVE10")).thenReturn(false);
      when(couponRepository.save(testCoupon)).thenReturn(testCoupon);

      Coupon result = couponService.createCoupon(testCoupon);

      assertNotNull(result);
      assertEquals("SAVE10", result.getCouponCode());
      verify(couponRepository, times(1)).save(testCoupon);
    }

    @Test
    @DisplayName("Should throw exception when coupon code already exists")
    void shouldThrowExceptionWhenCouponCodeExists() {
      when(couponRepository.existsByCouponCode("SAVE10")).thenReturn(true);

      assertThrows(ResourceAlreadyExistsException.class, () -> couponService.createCoupon(testCoupon));
      verify(couponRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("deleteCoupon Tests")
  class DeleteCouponTests {

    @Test
    @DisplayName("Should delete coupon by id")
    void shouldDeleteCoupon() {
      when(couponRepository.existsById(1L)).thenReturn(true);
      doNothing().when(couponRepository).deleteById(1L);

      couponService.deleteCoupon(1L);

      verify(couponRepository, times(1)).existsById(1L);
      verify(couponRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when coupon does not exist")
    void shouldThrowExceptionWhenCouponDoesNotExist() {
      when(couponRepository.existsById(999L)).thenReturn(false);

      assertThrows(ResourceNotFoundException.class, () -> couponService.deleteCoupon(999L));
    }
  }
}
