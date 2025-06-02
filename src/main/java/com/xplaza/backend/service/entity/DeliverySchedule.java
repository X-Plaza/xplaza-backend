/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service.entity;

import java.sql.Time;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliverySchedule {
  private Long id;
  private Time deliveryScheduleStart;
  private Time deliveryScheduleEnd;
  private Long dayId;
  private String dayName;
  // add other fields as needed
}
