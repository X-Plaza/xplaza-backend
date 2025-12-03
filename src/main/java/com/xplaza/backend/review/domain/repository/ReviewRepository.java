/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.review.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.review.domain.entity.Review;

/**
 * Repository for Review entity.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

  Page<Review> findByProductIdAndStatus(Long productId, Review.ReviewStatus status, Pageable pageable);

  Page<Review> findByProductId(Long productId, Pageable pageable);

  Page<Review> findByCustomerId(Long customerId, Pageable pageable);

  Page<Review> findByShopId(Long shopId, Pageable pageable);

  @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.status = 'APPROVED' ORDER BY r.createdAt DESC")
  Page<Review> findApprovedByProductId(@Param("productId") Long productId, Pageable pageable);

  @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.isVerifiedPurchase = true AND r.status = 'APPROVED' ORDER BY r.createdAt DESC")
  Page<Review> findVerifiedByProductId(@Param("productId") Long productId, Pageable pageable);

  @Query("SELECT r FROM Review r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC")
  Page<Review> findPendingReviews(Pageable pageable);

  @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.customerId = :customerId")
  Optional<Review> findByProductIdAndCustomerId(@Param("productId") Long productId,
      @Param("customerId") Long customerId);

  @Query("SELECT AVG(r.ratingOverall) FROM Review r WHERE r.productId = :productId AND r.status = 'APPROVED'")
  Double getAverageRatingByProductId(@Param("productId") Long productId);

  @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = 'APPROVED'")
  long countApprovedByProductId(@Param("productId") Long productId);

  @Query("SELECT r.ratingOverall, COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = 'APPROVED' GROUP BY r.ratingOverall")
  List<Object[]> getRatingDistributionByProductId(@Param("productId") Long productId);

  @Query("SELECT AVG(r.ratingQuality), AVG(r.ratingValue), AVG(r.ratingShipping) FROM Review r WHERE r.productId = :productId AND r.status = 'APPROVED'")
  Object[] getDimensionalRatingsAverage(@Param("productId") Long productId);

  @Query("SELECT r FROM Review r WHERE r.shopId = :shopId AND r.status = :status ORDER BY r.createdAt DESC")
  Page<Review> findByShopIdAndStatus(@Param("shopId") Long shopId, @Param("status") Review.ReviewStatus status,
      Pageable pageable);

  boolean existsByProductIdAndCustomerId(Long productId, Long customerId);
}
