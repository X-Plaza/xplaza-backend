/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.review.controller;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.review.domain.entity.Review;
import com.xplaza.backend.review.domain.entity.ReviewResponse;
import com.xplaza.backend.review.service.ReviewService;
import com.xplaza.backend.review.service.ReviewService.ProductRatingSummary;

/**
 * REST controller for product review operations.
 */
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product review management")
public class ReviewController {

  private final ReviewService reviewService;

  @Operation(summary = "Get product reviews")
  @GetMapping("/product/{productId}")
  public ResponseEntity<Page<Review>> getProductReviews(
      @PathVariable Long productId,
      Pageable pageable) {
    Page<Review> reviews = reviewService.getProductReviews(productId, pageable);
    return ResponseEntity.ok(reviews);
  }

  @Operation(summary = "Get verified product reviews")
  @GetMapping("/product/{productId}/verified")
  public ResponseEntity<Page<Review>> getVerifiedProductReviews(
      @PathVariable Long productId,
      Pageable pageable) {
    Page<Review> reviews = reviewService.getVerifiedProductReviews(productId, pageable);
    return ResponseEntity.ok(reviews);
  }

  @Operation(summary = "Get product rating summary")
  @GetMapping("/product/{productId}/summary")
  public ResponseEntity<ProductRatingSummary> getProductRatingSummary(@PathVariable Long productId) {
    ProductRatingSummary summary = reviewService.getProductRatingSummary(productId);
    return ResponseEntity.ok(summary);
  }

  @Operation(summary = "Get customer reviews")
  @GetMapping("/customer/{customerId}")
  public ResponseEntity<Page<Review>> getCustomerReviews(
      @PathVariable Long customerId,
      Pageable pageable) {
    Page<Review> reviews = reviewService.getCustomerReviews(customerId, pageable);
    return ResponseEntity.ok(reviews);
  }

  @Operation(summary = "Get review by ID")
  @GetMapping("/{reviewId}")
  public ResponseEntity<Review> getReview(@PathVariable UUID reviewId) {
    return reviewService.getReview(reviewId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Create a new review")
  @PostMapping
  public ResponseEntity<Review> createReview(@Valid @RequestBody CreateReviewRequest request) {
    Review review = reviewService.createReview(
        request.productId(),
        request.customerId(),
        request.orderId(),
        request.shopId(),
        request.title(),
        request.body(),
        request.ratingOverall(),
        request.ratingQuality(),
        request.ratingValue(),
        request.ratingShipping());
    return ResponseEntity.ok(review);
  }

  @Operation(summary = "Add image to review")
  @PostMapping("/{reviewId}/images")
  public ResponseEntity<Void> addImage(
      @PathVariable UUID reviewId,
      @RequestBody AddImageRequest request) {
    reviewService.addImage(reviewId, request.url(), request.altText());
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Add video to review")
  @PostMapping("/{reviewId}/videos")
  public ResponseEntity<Void> addVideo(
      @PathVariable UUID reviewId,
      @RequestBody AddVideoRequest request) {
    reviewService.addVideo(reviewId, request.url(), request.thumbnailUrl(), request.durationSeconds());
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Mark review as helpful")
  @PostMapping("/{reviewId}/helpful")
  public ResponseEntity<Void> markHelpful(@PathVariable UUID reviewId) {
    reviewService.markHelpful(reviewId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Mark review as not helpful")
  @PostMapping("/{reviewId}/not-helpful")
  public ResponseEntity<Void> markNotHelpful(@PathVariable UUID reviewId) {
    reviewService.markNotHelpful(reviewId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Flag a review for moderation")
  @PostMapping("/{reviewId}/flag")
  public ResponseEntity<Review> flagReview(
      @PathVariable UUID reviewId,
      @RequestBody FlagRequest request) {
    Review review = reviewService.flagReview(reviewId, request.reason());
    return ResponseEntity.ok(review);
  }

  @Operation(summary = "Check if customer has reviewed a product")
  @GetMapping("/product/{productId}/customer/{customerId}/exists")
  public ResponseEntity<Boolean> hasCustomerReviewed(
      @PathVariable Long productId,
      @PathVariable Long customerId) {
    boolean hasReviewed = reviewService.hasCustomerReviewed(productId, customerId);
    return ResponseEntity.ok(hasReviewed);
  }

  // Admin endpoints
  @Operation(summary = "Get pending reviews for moderation")
  @GetMapping("/pending")
  public ResponseEntity<Page<Review>> getPendingReviews(Pageable pageable) {
    Page<Review> reviews = reviewService.getPendingReviews(pageable);
    return ResponseEntity.ok(reviews);
  }

  @Operation(summary = "Approve a review")
  @PostMapping("/{reviewId}/approve")
  public ResponseEntity<Review> approveReview(
      @PathVariable UUID reviewId,
      @RequestParam Long moderatorId) {
    Review review = reviewService.approveReview(reviewId, moderatorId);
    return ResponseEntity.ok(review);
  }

  @Operation(summary = "Reject a review")
  @PostMapping("/{reviewId}/reject")
  public ResponseEntity<Review> rejectReview(
      @PathVariable UUID reviewId,
      @RequestParam Long moderatorId,
      @RequestBody RejectRequest request) {
    Review review = reviewService.rejectReview(reviewId, moderatorId, request.reason());
    return ResponseEntity.ok(review);
  }

  @Operation(summary = "Add vendor response to review")
  @PostMapping("/{reviewId}/response")
  public ResponseEntity<ReviewResponse> addVendorResponse(
      @PathVariable UUID reviewId,
      @RequestBody AddResponseRequest request) {
    ReviewResponse response = reviewService.addVendorResponse(
        reviewId, request.respondedBy(), request.body());
    return ResponseEntity.ok(response);
  }

  // Request DTOs
  public record CreateReviewRequest(
      Long productId,
      Long customerId,
      UUID orderId,
      Long shopId,
      String title,
      String body,
      @Min(1) @Max(5) Integer ratingOverall,
      @Min(1) @Max(5) Integer ratingQuality,
      @Min(1) @Max(5) Integer ratingValue,
      @Min(1) @Max(5) Integer ratingShipping
  ) {
  }

  public record AddImageRequest(
      String url,
      String altText
  ) {
  }

  public record AddVideoRequest(
      String url,
      String thumbnailUrl,
      Integer durationSeconds
  ) {
  }

  public record FlagRequest(String reason) {
  }

  public record RejectRequest(String reason) {
  }

  public record AddResponseRequest(
      Long respondedBy,
      String body
  ) {
  }
}
