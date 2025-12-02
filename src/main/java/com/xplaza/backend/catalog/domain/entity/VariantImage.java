/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.catalog.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Images specific to a product variant.
 * 
 * For example, a red t-shirt variant might have different images than a blue
 * t-shirt variant of the same product.
 */
@Entity
@Table(name = "variant_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantImage {

  @Id
  @Column(name = "image_id")
  @Builder.Default
  private UUID imageId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "variant_id", nullable = false)
  private ProductVariant variant;

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

  public UUID getVariantId() {
    return variant != null ? variant.getVariantId() : null;
  }
}
