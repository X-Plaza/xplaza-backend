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
 * Video attached to a review.
 */
@Entity
@Table(name = "review_videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewVideo {

  @Id
  @Column(name = "video_id")
  @Builder.Default
  private UUID videoId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id", nullable = false)
  private Review review;

  @Column(name = "url", nullable = false, length = 500)
  private String url;

  @Column(name = "thumbnail_url", length = 500)
  private String thumbnailUrl;

  @Column(name = "duration_seconds")
  private Integer durationSeconds;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  public UUID getReviewId() {
    return review != null ? review.getReviewId() : null;
  }
}
