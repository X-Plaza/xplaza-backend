/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.catalog.domain.entity;

import java.time.Instant;

import jakarta.persistence.*;

import lombok.*;

/**
 * Represents a possible value for an attribute.
 * 
 * Examples: - For "Color" attribute: Red, Blue, Green, Black, White - For
 * "Size" attribute: XS, S, M, L, XL, XXL
 */
@Entity
@Table(name = "attribute_values", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "attribute_id", "code" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributeValue {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "value_id")
  private Long valueId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attribute_id", nullable = false)
  private Attribute attribute;

  /**
   * Display value shown to customers. Examples: "Red", "Extra Large", "Cotton"
   */
  @Column(name = "display_value", nullable = false, length = 255)
  private String displayValue;

  /**
   * URL-safe code for the value. Examples: "red", "xl", "cotton"
   */
  @Column(name = "code", nullable = false, length = 100)
  private String code;

  /**
   * Additional metadata stored as JSON.
   * 
   * Examples: - For colors: {"hex": "#FF0000", "rgb": "255,0,0"} - For sizes:
   * {"measurements": {"chest": 42, "waist": 32}}
   */
  @Column(name = "metadata", columnDefinition = "TEXT")
  private String metadata;

  /**
   * Sort order for display.
   */
  @Column(name = "position")
  @Builder.Default
  private Integer position = 0;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  /**
   * Returns the attribute ID for this value.
   */
  public Long getAttributeId() {
    return attribute != null ? attribute.getAttributeId() : null;
  }

  /**
   * Returns the attribute name for display.
   */
  public String getAttributeName() {
    return attribute != null ? attribute.getName() : null;
  }
}
