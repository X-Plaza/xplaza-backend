/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.review.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.order.domain.entity.CustomerOrder;
import com.xplaza.backend.order.service.CustomerOrderService;
import com.xplaza.backend.review.domain.entity.Review;
import com.xplaza.backend.review.domain.entity.ReviewImage;
import com.xplaza.backend.review.domain.entity.ReviewResponse;
import com.xplaza.backend.review.domain.entity.ReviewVideo;
import com.xplaza.backend.review.domain.repository.ReviewRepository;

/**
 * Service for review operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final CustomerOrderService customerOrderService;

  /**
   * Create a new review.
   */
  public Review createReview(Long productId, Long customerId, UUID orderId, Long shopId,
      String title, String body, Integer ratingOverall,
      Integer ratingQuality, Integer ratingValue, Integer ratingShipping) {
    // Check if customer already reviewed this product
    if (reviewRepository.existsByProductIdAndCustomerId(productId, customerId)) {
      throw new IllegalStateException("Customer has already reviewed this product");
    }

    if (orderId != null) {
      CustomerOrder order = customerOrderService.getOrder(orderId)
          .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

      if (!order.getCustomerId().equals(customerId)) {
        throw new IllegalArgumentException("Order does not belong to customer");
      }

      boolean hasProduct = order.getItems().stream()
          .anyMatch(item -> item.getProductId().equals(productId));

      if (!hasProduct) {
        throw new IllegalArgumentException("Product not found in order");
      }
    }

    Review review = Review.builder()
        .productId(productId)
        .customerId(customerId)
        .orderId(orderId)
        .shopId(shopId)
        .title(title)
        .body(body)
        .ratingOverall(ratingOverall)
        .ratingQuality(ratingQuality)
        .ratingValue(ratingValue)
        .ratingShipping(ratingShipping)
        .isVerifiedPurchase(orderId != null)
        .build();

    review = reviewRepository.save(review);
    log.info("Created review for product {}: reviewId={}", productId, review.getReviewId());
    return review;
  }

  /**
   * Add image to review.
   */
  public void addImage(UUID reviewId, String url, String altText) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

    ReviewImage image = ReviewImage.builder()
        .review(review)
        .url(url)
        .altText(altText)
        .position(review.getImages().size())
        .build();

    review.addImage(image);
    reviewRepository.save(review);
  }

  /**
   * Add video to review.
   */
  public void addVideo(UUID reviewId, String url, String thumbnailUrl, Integer durationSeconds) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

    ReviewVideo video = ReviewVideo.builder()
        .review(review)
        .url(url)
        .thumbnailUrl(thumbnailUrl)
        .durationSeconds(durationSeconds)
        .build();

    review.addVideo(video);
    reviewRepository.save(review);
  }

  /**
   * Get product reviews (approved only).
   */
  @Transactional(readOnly = true)
  public Page<Review> getProductReviews(Long productId, Pageable pageable) {
    return reviewRepository.findApprovedByProductId(productId, pageable);
  }

  /**
   * Get verified product reviews.
   */
  @Transactional(readOnly = true)
  public Page<Review> getVerifiedProductReviews(Long productId, Pageable pageable) {
    return reviewRepository.findVerifiedByProductId(productId, pageable);
  }

  /**
   * Get customer reviews.
   */
  @Transactional(readOnly = true)
  public Page<Review> getCustomerReviews(Long customerId, Pageable pageable) {
    return reviewRepository.findByCustomerId(customerId, pageable);
  }

  /**
   * Get pending reviews for moderation.
   */
  @Transactional(readOnly = true)
  public Page<Review> getPendingReviews(Pageable pageable) {
    return reviewRepository.findPendingReviews(pageable);
  }

  /**
   * Approve a review.
   */
  public Review approveReview(UUID reviewId, Long moderatorId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

    review.approve(moderatorId);
    review = reviewRepository.save(review);

    log.info("Approved review: {}", reviewId);
    return review;
  }

  /**
   * Reject a review.
   */
  public Review rejectReview(UUID reviewId, Long moderatorId, String reason) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

    review.reject(moderatorId, reason);
    review = reviewRepository.save(review);

    log.info("Rejected review {}: {}", reviewId, reason);
    return review;
  }

  /**
   * Flag a review for further inspection.
   */
  public Review flagReview(UUID reviewId, String reason) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

    review.flag(reason);
    review = reviewRepository.save(review);

    log.info("Flagged review {}: {}", reviewId, reason);
    return review;
  }

  /**
   * Mark review as helpful.
   */
  public void markHelpful(UUID reviewId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

    review.addHelpfulVote();
    reviewRepository.save(review);
  }

  /**
   * Mark review as not helpful.
   */
  public void markNotHelpful(UUID reviewId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

    review.addUnhelpfulVote();
    reviewRepository.save(review);
  }

  /**
   * Add vendor response to review.
   */
  public ReviewResponse addVendorResponse(UUID reviewId, Long respondedBy, String body) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

    // Check if response already exists
    if (review.getVendorResponse() != null) {
      throw new IllegalStateException("Review already has a response");
    }

    ReviewResponse reviewResponse = ReviewResponse.builder()
        .review(review)
        .respondedBy(respondedBy)
        .body(body)
        .build();

    review.setVendorResponse(reviewResponse);
    reviewRepository.save(review);

    log.info("Added vendor response to review: {}", reviewId);
    return reviewResponse;
  }

  /**
   * Get product rating summary.
   */
  @Transactional(readOnly = true)
  public ProductRatingSummary getProductRatingSummary(Long productId) {
    Double averageRating = reviewRepository.getAverageRatingByProductId(productId);
    long totalReviews = reviewRepository.countApprovedByProductId(productId);
    List<Object[]> distribution = reviewRepository.getRatingDistributionByProductId(productId);
    Object[] dimensionalAverages = reviewRepository.getDimensionalRatingsAverage(productId);

    return new ProductRatingSummary(
        averageRating != null ? averageRating : 0.0,
        totalReviews,
        distribution,
        dimensionalAverages);
  }

  /**
   * Get review by ID.
   */
  @Transactional(readOnly = true)
  public Optional<Review> getReview(UUID reviewId) {
    return reviewRepository.findById(reviewId);
  }

  /**
   * Check if customer has reviewed a product.
   */
  @Transactional(readOnly = true)
  public boolean hasCustomerReviewed(Long productId, Long customerId) {
    return reviewRepository.existsByProductIdAndCustomerId(productId, customerId);
  }

  /**
   * Product rating summary DTO.
   */
  public record ProductRatingSummary(
      double averageRating,
      long totalReviews,
      List<Object[]> ratingDistribution,
      Object[] dimensionalAverages
  ) {
  }
}
