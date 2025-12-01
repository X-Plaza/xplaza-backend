/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.common.util.ValidationUtil;
import com.xplaza.backend.domain.Shop;
import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.jpa.dao.ShopDao;
import com.xplaza.backend.jpa.repository.ShopRepository;
import com.xplaza.backend.mapper.ShopMapper;

@Service
@RequiredArgsConstructor
public class ShopService {
  private final ShopRepository shopRepo;
  private final ShopMapper shopMapper;

  @Transactional
  public Shop addShop(Shop shop) {
    // Validate input
    ValidationUtil.validateNotNull(shop, "Shop");
    ValidationUtil.validateNotEmpty(shop.getShopName(), "Shop name");
    ValidationUtil.validateNotEmpty(shop.getShopAddress(), "Shop address");
    ValidationUtil.validateNotEmpty(shop.getShopOwner(), "Shop owner");
    ShopDao shopDao = shopMapper.toDao(shop);
    ShopDao savedShopDao = shopRepo.save(shopDao);
    return shopMapper.toEntityFromDao(savedShopDao);
  }

  @Transactional
  public Shop updateShop(Long id, Shop shop) {
    // Validate input
    ValidationUtil.validateId(id, "Shop ID");
    ValidationUtil.validateNotNull(shop, "Shop");
    ValidationUtil.validateNotEmpty(shop.getShopName(), "Shop name");
    ValidationUtil.validateNotEmpty(shop.getShopAddress(), "Shop address");
    ValidationUtil.validateNotEmpty(shop.getShopOwner(), "Shop owner");
    // Check if shop exists
    shopRepo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + id));
    shop.setShopId(id);
    ShopDao shopDao = shopMapper.toDao(shop);
    ShopDao updatedShopDao = shopRepo.save(shopDao);
    return shopMapper.toEntityFromDao(updatedShopDao);
  }

  @Transactional
  public void deleteShop(Long id) {
    ValidationUtil.validateId(id, "Shop ID");
    if (!shopRepo.existsById(id)) {
      throw new ResourceNotFoundException("Shop not found with id: " + id);
    }
    shopRepo.deleteById(id);
  }

  public List<Shop> listShops() {
    List<ShopDao> shopDaos = shopRepo.findAll();
    return shopDaos.stream()
        .map(shopMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }

  public Shop listShop(Long id) {
    ShopDao shopDao = shopRepo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + id));
    return shopMapper.toEntityFromDao(shopDao);
  }

  public List<Shop> listShopsByLocation(Long locationId) {
    List<ShopDao> shopDaos = shopRepo.findByLocationId(locationId);
    return shopDaos.stream()
        .map(shopMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }

  public List<Shop> listShopsByOwner(Long ownerId) {
    List<ShopDao> shopDaos = shopRepo.findByShopOwner(ownerId);
    return shopDaos.stream()
        .map(shopMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }

  // ===== Paginated Methods =====

  public Page<Shop> listShopsPaginated(Pageable pageable) {
    return shopRepo.findAll(pageable)
        .map(shopMapper::toEntityFromDao);
  }

  public Page<Shop> searchShops(String searchTerm, Pageable pageable) {
    return shopRepo.findByShopNameContainingIgnoreCase(searchTerm, pageable)
        .map(shopMapper::toEntityFromDao);
  }

  public Page<Shop> listShopsByLocationPaginated(Long locationId, Pageable pageable) {
    return shopRepo.findByLocationLocationId(locationId, pageable)
        .map(shopMapper::toEntityFromDao);
  }

  public Page<Shop> listShopsByOwnerPaginated(Long ownerId, Pageable pageable) {
    return shopRepo.findByShopOwnerIdPaginated(ownerId, pageable)
        .map(shopMapper::toEntityFromDao);
  }
}
