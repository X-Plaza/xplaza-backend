/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.delivery.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.delivery.domain.entity.DeliveryCost;
import com.xplaza.backend.delivery.repository.DeliveryCostRepository;
import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.geography.domain.entity.City;
import com.xplaza.backend.geography.repository.CityRepository;
import com.xplaza.backend.shop.domain.entity.Shop;
import com.xplaza.backend.shop.domain.repository.ShopRepository;

@Service
@RequiredArgsConstructor
public class DeliveryCostService {
  private final DeliveryCostRepository deliveryCostRepository;
  private final ShopRepository shopRepository;
  private final CityRepository cityRepository;

  public List<DeliveryCost> listDeliveryCosts() {
    return deliveryCostRepository.findAll();
  }

  public List<DeliveryCost> listDeliveryCostsByShop(Long shopId) {
    return deliveryCostRepository.findByShopShopId(shopId);
  }

  public List<DeliveryCost> listDeliveryCostsByCity(Long cityId) {
    return deliveryCostRepository.findByCityCityId(cityId);
  }

  public DeliveryCost getDeliveryCost(Long id) {
    return deliveryCostRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("DeliveryCost not found: " + id));
  }

  @Transactional
  public DeliveryCost createDeliveryCost(DeliveryCost cost, Long shopId, Long cityId) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + shopId));
    City city = cityRepository.findById(cityId)
        .orElseThrow(() -> new ResourceNotFoundException("City not found: " + cityId));
    cost.setShop(shop);
    cost.setCity(city);
    return deliveryCostRepository.save(cost);
  }

  @Transactional
  public DeliveryCost updateDeliveryCost(Long id, DeliveryCost details) {
    DeliveryCost cost = getDeliveryCost(id);
    cost.setDeliveryFee(details.getDeliveryFee());
    cost.setMinimumOrderAmount(details.getMinimumOrderAmount());
    cost.setFreeDeliveryThreshold(details.getFreeDeliveryThreshold());
    cost.setEstimatedDeliveryMinutes(details.getEstimatedDeliveryMinutes());
    cost.setIsActive(details.getIsActive());
    return deliveryCostRepository.save(cost);
  }

  @Transactional
  public void deleteDeliveryCost(Long id) {
    if (!deliveryCostRepository.existsById(id)) {
      throw new ResourceNotFoundException("DeliveryCost not found: " + id);
    }
    deliveryCostRepository.deleteById(id);
  }
}
