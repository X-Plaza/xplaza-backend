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
public class DeliverySchedule {
  private Long deliveryScheduleId;
  private Day deliveryDay;
  private String deliveryTime;
  private Time deliveryScheduleStart;
  private Time deliveryScheduleEnd;
  private DeliveryScheduleList deliveryScheduleList;
}
