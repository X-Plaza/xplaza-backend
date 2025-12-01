/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class DateConverter {
  public Date convertDateToStartOfTheDay(Date date) {
    Instant inst = date.toInstant();
    LocalDate localDate = inst.atZone(ZoneId.systemDefault()).toLocalDate();
    Instant dayInst = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    date = Date.from(dayInst);
    return date;
  }

  public Date convertDateToEndOfTheDay(Date date) {
    Instant inst = date.toInstant();
    LocalDate localDate = inst.atZone(ZoneId.systemDefault()).toLocalDate();
    Instant dayInst = LocalTime.MAX.atDate(localDate).toInstant(ZoneId.systemDefault().getRules().getOffset(inst));
    date = Date.from(dayInst);
    return date;
  }
}
