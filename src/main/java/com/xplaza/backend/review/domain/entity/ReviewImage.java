/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.review.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Image attached to a review.
 */
@Entity
@Table(name = "review_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewImage {

  @Id
  @Column(name = "image_id")
  @Builder.Default
  private UUID imageId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id", nullable = false)
  private Review review;

  @Column(name = "url", nullable = false, length = 500)
  private String url;

  @Column(name = "alt_text", length = 255)
  private String altText;

  @Column(name = "position")
  @Builder.Default
  private Integer position = 0;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  public UUID getReviewId() {
    return review != null ? review.getReviewId() : null;
  }
}
