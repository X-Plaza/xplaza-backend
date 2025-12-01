/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.domain;

import java.sql.Time;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryScheduleDetails {
  private Long deliveryScheduleId;
  private Time deliveryScheduleStart;
  private Time deliveryScheduleEnd;
  private Day deliveryScheduleDay;
  private Long deliveryScheduleListId;
}
