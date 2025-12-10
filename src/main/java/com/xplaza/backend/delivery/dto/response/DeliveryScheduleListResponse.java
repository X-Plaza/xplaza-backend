/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.delivery.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class DeliveryScheduleListResponse {
  private Long dayId;
  private String dayName;
  private List<DeliveryScheduleResponse> deliverySchedules;
  // add other fields as needed
}
