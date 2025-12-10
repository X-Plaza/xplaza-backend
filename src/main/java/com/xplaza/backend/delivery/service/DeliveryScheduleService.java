/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.delivery.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.delivery.domain.entity.Day;
import com.xplaza.backend.delivery.domain.entity.DeliverySchedule;
import com.xplaza.backend.delivery.repository.DayRepository;
import com.xplaza.backend.delivery.repository.DeliveryScheduleRepository;
import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.shop.domain.entity.Shop;
import com.xplaza.backend.shop.domain.repository.ShopRepository;

@Service
@RequiredArgsConstructor
public class DeliveryScheduleService {
  private final DeliveryScheduleRepository deliveryScheduleRepository;
  private final ShopRepository shopRepository;
  private final DayRepository dayRepository;

  public List<DeliverySchedule> listDeliverySchedules() {
    return deliveryScheduleRepository.findAll();
  }

  public List<DeliverySchedule> listDeliverySchedulesByShop(Long shopId) {
    return deliveryScheduleRepository.findByShopShopId(shopId);
  }

  public List<DeliverySchedule> listDeliverySchedulesByDay(Long dayId) {
    return deliveryScheduleRepository.findByDayDayId(dayId);
  }

  public DeliverySchedule getDeliverySchedule(Long id) {
    return deliveryScheduleRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("DeliverySchedule not found: " + id));
  }

  @Transactional
  public DeliverySchedule createDeliverySchedule(DeliverySchedule schedule, Long shopId, Long dayId) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + shopId));
    Day day = dayRepository.findById(dayId)
        .orElseThrow(() -> new ResourceNotFoundException("Day not found: " + dayId));
    schedule.setShop(shop);
    schedule.setDay(day);
    return deliveryScheduleRepository.save(schedule);
  }

  @Transactional
  public DeliverySchedule updateDeliverySchedule(Long id, DeliverySchedule details) {
    DeliverySchedule schedule = getDeliverySchedule(id);
    schedule.setStartTime(details.getStartTime());
    schedule.setEndTime(details.getEndTime());
    schedule.setIsAvailable(details.getIsAvailable());
    schedule.setMaxDeliveries(details.getMaxDeliveries());
    return deliveryScheduleRepository.save(schedule);
  }

  @Transactional
  public void deleteDeliverySchedule(Long id) {
    if (!deliveryScheduleRepository.existsById(id)) {
      throw new ResourceNotFoundException("DeliverySchedule not found: " + id);
    }
    deliveryScheduleRepository.deleteById(id);
  }
}
