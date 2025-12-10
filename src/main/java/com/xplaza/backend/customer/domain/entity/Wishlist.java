/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.customer.domain.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Customer wishlist for saving products they're interested in.
 * 
 * Features: - Multiple wishlists per customer (e.g., "Birthday", "Christmas") -
 * Shareable wishlists - Price drop notifications - Back-in-stock notifications
 */
@Entity
@Table(name = "wishlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist {

  @Id
  @Column(name = "wishlist_id")
  @Builder.Default
  private UUID wishlistId = UUID.randomUUID();

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(name = "name", length = 100)
  @Builder.Default
  private String name = "My Wishlist";

  @Column(name = "is_default")
  @Builder.Default
  private Boolean isDefault = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "visibility", length = 20)
  @Builder.Default
  private WishlistVisibility visibility = WishlistVisibility.PRIVATE;

  /**
   * Token for sharing the wishlist via URL. Only used when visibility is SHARED.
   */
  @Column(name = "share_token", length = 64)
  private String shareToken;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  @Builder.Default
  private Instant updatedAt = Instant.now();

  @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private List<WishlistItem> items = new ArrayList<>();

  public enum WishlistVisibility {
    /** Only visible to the owner */
    PRIVATE,
    /** Accessible via share link */
    SHARED,
    /** Visible to anyone */
    PUBLIC
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  public void addItem(WishlistItem item) {
    items.add(item);
    item.setWishlist(this);
  }

  public void removeItem(WishlistItem item) {
    items.remove(item);
    item.setWishlist(null);
  }

  public int getItemCount() {
    return items != null ? items.size() : 0;
  }

  /**
   * Generate a share token if the wishlist is shareable.
   */
  public void makeShareable() {
    if (this.shareToken == null) {
      this.shareToken = UUID.randomUUID().toString().replace("-", "");
    }
    this.visibility = WishlistVisibility.SHARED;
  }

  /**
   * Revoke sharing access.
   */
  public void makePrivate() {
    this.visibility = WishlistVisibility.PRIVATE;
    // Keep the token in case they want to re-share
  }

  /**
   * Check if wishlist is public.
   */
  public boolean isPublic() {
    return this.visibility == WishlistVisibility.PUBLIC;
  }
}
