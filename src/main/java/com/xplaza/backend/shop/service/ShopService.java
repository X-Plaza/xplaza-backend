/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.shop.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.shop.domain.entity.Shop;
import com.xplaza.backend.shop.domain.repository.ShopRepository;

@Service
@RequiredArgsConstructor
public class ShopService {
  private final ShopRepository shopRepository;

  public List<Shop> listShops() {
    return shopRepository.findAll();
  }

  public Page<Shop> listShopsPaginated(Pageable pageable) {
    return shopRepository.findAll(pageable);
  }

  public Page<Shop> searchShops(String searchTerm, Pageable pageable) {
    return shopRepository.findByShopNameContainingIgnoreCase(searchTerm, pageable);
  }

  public Page<Shop> listShopsByLocationPaginated(Long locationId, Pageable pageable) {
    return shopRepository.findByLocationId(locationId, pageable);
  }

  public Page<Shop> listShopsByOwnerPaginated(Long ownerId, Pageable pageable) {
    return shopRepository.findByShopOwnerIdPaginated(ownerId, pageable);
  }

  public Shop listShop(Long id) {
    return shopRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + id));
  }

  @Transactional
  public Shop addShop(Shop shop) {
    return shopRepository.save(shop);
  }

  @Transactional
  public Shop updateShop(Long id, Shop shopDetails) {
    Shop existingShop = listShop(id);
    existingShop.setShopName(shopDetails.getShopName());
    existingShop.setShopDescription(shopDetails.getShopDescription());
    existingShop.setShopAddress(shopDetails.getShopAddress());
    existingShop.setLocationId(shopDetails.getLocationId());
    existingShop.setShopOwner(shopDetails.getShopOwner());
    return shopRepository.save(existingShop);
  }

  @Transactional
  public void deleteShop(Long id) {
    if (!shopRepository.existsById(id)) {
      throw new ResourceNotFoundException("Shop not found with id: " + id);
    }
    shopRepository.deleteById(id);
  }
}
