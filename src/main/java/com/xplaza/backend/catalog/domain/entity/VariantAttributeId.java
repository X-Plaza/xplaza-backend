/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.catalog.domain.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import lombok.*;

/**
 * Composite primary key for VariantAttribute.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VariantAttributeId implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID variantId;
  private Long attributeId;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    VariantAttributeId that = (VariantAttributeId) o;
    return Objects.equals(variantId, that.variantId) &&
        Objects.equals(attributeId, that.attributeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(variantId, attributeId);
  }
}
