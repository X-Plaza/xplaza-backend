/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xplaza.backend.jpa.dao.DeliveryScheduleDao;
import com.xplaza.backend.jpa.repository.DeliveryScheduleRepository;
import com.xplaza.backend.mapper.DeliveryScheduleMapper;
import com.xplaza.backend.service.entity.DeliverySchedule;

@Service
public class DeliveryScheduleService {
  @Autowired
  private DeliveryScheduleRepository deliveryScheduleRepo;
  @Autowired
  private DeliveryScheduleMapper deliveryScheduleMapper;

  public void addSchedule(DeliverySchedule entity) {
    DeliveryScheduleDao dao = deliveryScheduleMapper.toDao(entity);
    deliveryScheduleRepo.save(dao);
  }

  public void updateSchedule(DeliverySchedule entity) {
    DeliveryScheduleDao dao = deliveryScheduleMapper.toDao(entity);
    deliveryScheduleRepo.save(dao);
  }

  public List<DeliverySchedule> listDeliverySchedules() {
    return deliveryScheduleRepo.findAll().stream().map(deliveryScheduleMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }

  public DeliverySchedule listDeliverySchedule(Long id) {
    return deliveryScheduleRepo.findById(id).map(deliveryScheduleMapper::toEntityFromDao).orElse(null);
  }

  public void deleteSchedule(Long id) {
    deliveryScheduleRepo.deleteById(id);
  }
}
