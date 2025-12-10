/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.notification.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Customer notification.
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_customer", columnList = "customer_id"),
    @Index(name = "idx_notification_read", columnList = "is_read"),
    @Index(name = "idx_notification_type", columnList = "type"),
    @Index(name = "idx_notification_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

  @Id
  @Column(name = "notification_id")
  @Builder.Default
  private UUID notificationId = UUID.randomUUID();

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 50)
  private NotificationType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "channel", nullable = false, length = 20)
  @Builder.Default
  private NotificationChannel channel = NotificationChannel.IN_APP;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "message", nullable = false, columnDefinition = "TEXT")
  private String message;

  // Rich content
  @Column(name = "image_url", length = 500)
  private String imageUrl;

  @Column(name = "action_url", length = 500)
  private String actionUrl;

  @Column(name = "action_text", length = 100)
  private String actionText;

  // Reference
  @Enumerated(EnumType.STRING)
  @Column(name = "reference_type", length = 30)
  private ReferenceType referenceType;

  @Column(name = "reference_id", length = 50)
  private String referenceId;

  // Status
  @Column(name = "is_read")
  @Builder.Default
  private Boolean isRead = false;

  @Column(name = "read_at")
  private Instant readAt;

  @Column(name = "is_sent")
  @Builder.Default
  private Boolean isSent = false;

  @Column(name = "sent_at")
  private Instant sentAt;

  @Column(name = "error_message", length = 500)
  private String errorMessage;

  // Scheduling
  @Column(name = "scheduled_at")
  private Instant scheduledAt;

  @Column(name = "expires_at")
  private Instant expiresAt;

  // Priority
  @Enumerated(EnumType.STRING)
  @Column(name = "priority", length = 10)
  @Builder.Default
  private Priority priority = Priority.NORMAL;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  @Builder.Default
  private Instant updatedAt = Instant.now();

  public enum NotificationType {
    // Order
    ORDER_PLACED,
    ORDER_CONFIRMED,
    ORDER_SHIPPED,
    ORDER_DELIVERED,
    ORDER_CANCELLED,
    ORDER_REFUNDED,

    // Payment
    PAYMENT_RECEIVED,
    PAYMENT_FAILED,
    PAYMENT_REFUNDED,

    // Shipping
    SHIPMENT_CREATED,
    SHIPMENT_IN_TRANSIT,
    SHIPMENT_OUT_FOR_DELIVERY,
    SHIPMENT_DELIVERED,

    // Return
    RETURN_APPROVED,
    RETURN_RECEIVED,
    RETURN_COMPLETED,
    RETURN_REJECTED,

    // Product
    PRODUCT_BACK_IN_STOCK,
    PRICE_DROP,
    WISHLIST_ITEM_ON_SALE,
    NEW_PRODUCT_IN_CATEGORY,

    // Review
    REVIEW_APPROVED,
    REVIEW_REJECTED,
    REVIEW_RESPONSE,

    // Promotion
    PROMOTION_ACTIVE,
    COUPON_EXPIRING,
    LOYALTY_POINTS_EARNED,
    LOYALTY_POINTS_EXPIRING,

    // Account
    WELCOME,
    PASSWORD_CHANGED,
    EMAIL_VERIFIED,
    ACCOUNT_LOCKED,

    // Marketing
    MARKETING_CAMPAIGN,
    PERSONALIZED_RECOMMENDATION
  }

  public enum NotificationChannel {
    IN_APP,
    EMAIL,
    SMS,
    PUSH
  }

  public enum ReferenceType {
    ORDER,
    PRODUCT,
    REVIEW,
    SHIPMENT,
    RETURN_REQUEST,
    COUPON,
    CAMPAIGN
  }

  public enum Priority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  /**
   * Mark notification as read.
   */
  public void markAsRead() {
    this.isRead = true;
    this.readAt = Instant.now();
  }

  /**
   * Mark notification as sent.
   */
  public void markAsSent() {
    this.isSent = true;
    this.sentAt = Instant.now();
  }

  /**
   * Mark as failed.
   */
  public void markAsFailed(String error) {
    this.errorMessage = error;
  }

  /**
   * Check if notification is expired.
   */
  public boolean isExpired() {
    return expiresAt != null && Instant.now().isAfter(expiresAt);
  }

  /**
   * Check if notification should be sent now.
   */
  public boolean isReadyToSend() {
    if (isSent) {
      return false;
    }
    if (scheduledAt != null && Instant.now().isBefore(scheduledAt)) {
      return false;
    }
    return !isExpired();
  }
}
