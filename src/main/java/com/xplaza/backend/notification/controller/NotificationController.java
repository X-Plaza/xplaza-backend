/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.notification.controller;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.notification.domain.entity.Notification;
import com.xplaza.backend.notification.service.NotificationService;

/**
 * REST controller for notification operations.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Customer notification management APIs")
public class NotificationController {

  private final NotificationService notificationService;

  @Operation(summary = "Get notifications for customer")
  @GetMapping("/customers/{customerId}")
  public ResponseEntity<Page<Notification>> getNotifications(
      @PathVariable Long customerId,
      Pageable pageable) {
    return ResponseEntity.ok(notificationService.getNotifications(customerId, pageable));
  }

  @Operation(summary = "Get unread notifications for customer")
  @GetMapping("/customers/{customerId}/unread")
  public ResponseEntity<Page<Notification>> getUnreadNotifications(
      @PathVariable Long customerId,
      Pageable pageable) {
    return ResponseEntity.ok(notificationService.getUnreadNotifications(customerId, pageable));
  }

  @Operation(summary = "Count unread notifications")
  @GetMapping("/customers/{customerId}/unread/count")
  public ResponseEntity<Long> countUnread(@PathVariable Long customerId) {
    return ResponseEntity.ok(notificationService.countUnread(customerId));
  }

  @Operation(summary = "Create in-app notification")
  @PostMapping
  public ResponseEntity<Notification> createNotification(@RequestBody CreateNotificationRequest request) {
    Notification notification = notificationService.createInAppNotification(
        request.customerId(),
        request.type(),
        request.title(),
        request.message());
    return ResponseEntity.ok(notification);
  }

  @Operation(summary = "Mark notification as read")
  @PostMapping("/{notificationId}/read")
  public ResponseEntity<Notification> markAsRead(@PathVariable UUID notificationId) {
    Notification notification = notificationService.markAsRead(notificationId);
    return ResponseEntity.ok(notification);
  }

  @Operation(summary = "Mark all notifications as read for customer")
  @PostMapping("/customers/{customerId}/read-all")
  public ResponseEntity<MarkAllResult> markAllAsRead(@PathVariable Long customerId) {
    int count = notificationService.markAllAsRead(customerId);
    return ResponseEntity.ok(new MarkAllResult(count));
  }

  @Operation(summary = "Delete notification")
  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> deleteNotification(@PathVariable UUID notificationId) {
    notificationService.deleteNotification(notificationId);
    return ResponseEntity.ok().build();
  }

  public record CreateNotificationRequest(
      Long customerId,
      Notification.NotificationType type,
      String title,
      String message
  ) {
  }

  public record MarkAllResult(int markedCount) {
  }
}
