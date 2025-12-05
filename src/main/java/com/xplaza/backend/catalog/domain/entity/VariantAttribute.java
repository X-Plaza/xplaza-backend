/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.catalog.domain.entity;

import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Links a ProductVariant to its defining Attributes.
 * 
 * For example, a variant might have: - Color: Red (attribute_id=1, value_id=5)
 * - Size: Large (attribute_id=2, value_id=12)
 */
@Entity
@Table(name = "variant_attributes")
@IdClass(VariantAttributeId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantAttribute {

  @Id
  @Column(name = "variant_id")
  private UUID variantId;

  @Id
  @Column(name = "attribute_id")
  private Long attributeId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "variant_id", insertable = false, updatable = false)
  private ProductVariant variant;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "attribute_id", insertable = false, updatable = false)
  private Attribute attribute;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "value_id", nullable = false)
  private AttributeValue attributeValue;

  /**
   * Create a VariantAttribute linking a variant to an attribute value.
   */
  public static VariantAttribute of(UUID variantId, AttributeValue attributeValue) {
    return VariantAttribute.builder()
        .variantId(variantId)
        .attributeId(attributeValue.getAttribute().getAttributeId())
        .attributeValue(attributeValue)
        .build();
  }

  public String getAttributeName() {
    return attribute != null ? attribute.getName() : null;
  }

  public String getValue() {
    return attributeValue != null ? attributeValue.getDisplayValue() : null;
  }

  public String getValueCode() {
    return attributeValue != null ? attributeValue.getCode() : null;
  }
}
