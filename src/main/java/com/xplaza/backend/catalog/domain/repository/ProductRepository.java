/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.catalog.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.catalog.domain.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  @Override
  @EntityGraph(attributePaths = { "brand", "category", "productVariationType", "currency", "shop", "images" })
  List<Product> findAll();

  @Override
  @EntityGraph(attributePaths = { "brand", "category", "productVariationType", "currency", "shop", "images" })
  Optional<Product> findById(Long id);

  @Query("SELECT p.productName FROM Product p WHERE p.productId = :id")
  String getName(@Param("id") Long id);

  @EntityGraph(attributePaths = { "brand", "category", "productVariationType", "currency", "shop", "images" })
  Optional<Product> findByProductId(Long productId);

  default Product findProductById(Long productId) {
    return findByProductId(productId).orElse(null);
  }

  boolean existsByProductId(Long productId);

  @Modifying
  @Transactional
  @Query("UPDATE Product p SET p.quantity = :quantity WHERE p.productId = :id")
  void updateInventory(@Param("id") Long id, @Param("quantity") int quantity);

  @Modifying
  @Transactional
  @Query("UPDATE Product p SET p.quantity = p.quantity - :decrement WHERE p.productId = :id AND p.quantity >= :decrement")
  int decrementInventory(@Param("id") Long id, @Param("decrement") Long decrement);

  @Modifying
  @Transactional
  @Query("UPDATE Product p SET p.quantity = p.quantity + :increment WHERE p.productId = :id")
  int incrementInventory(@Param("id") Long id, @Param("increment") Long increment);

  @Query("SELECT p.shop.shopId FROM Product p WHERE p.productId = :productId")
  Long findShopIdByProductId(@Param("productId") Long productId);

  @Query(value = "SELECT p.* FROM products p " +
      "JOIN shops s ON p.fk_shop_id = s.shop_id " +
      "JOIN admin_user_shop_link ausl ON s.shop_id = ausl.shop_id " +
      "WHERE ausl.admin_user_id = :userId", nativeQuery = true)
  Page<Product> findByUserId(@Param("userId") Long userId, Pageable pageable);

  @Query(value = "SELECT p.* FROM products p " +
      "JOIN shops s ON p.fk_shop_id = s.shop_id " +
      "JOIN admin_user_shop_link ausl ON s.shop_id = ausl.shop_id " +
      "WHERE ausl.admin_user_id = :userId", nativeQuery = true)
  List<Product> findByUserId(@Param("userId") Long userId);

  Page<Product> findByShopShopId(Long shopId, Pageable pageable);

  List<Product> findByShopShopId(Long shopId);

  Page<Product> findByCategoryCategoryId(Long categoryId, Pageable pageable);

  List<Product> findByCategoryCategoryId(Long categoryId);

  Page<Product> findByBrandBrandId(Long brandId, Pageable pageable);

  List<Product> findByBrandBrandId(Long brandId);

  Page<Product> findByShopShopIdAndCategoryCategoryId(Long shopId, Long categoryId, Pageable pageable);

  List<Product> findByShopShopIdAndCategoryCategoryId(Long shopId, Long categoryId);

  Page<Product> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);

  List<Product> findByProductNameContainingIgnoreCase(String productName);

  default List<Product> findByShopId(Long shopId) {
    return findByShopShopId(shopId);
  }

  default List<Product> findByCategoryId(Long categoryId) {
    return findByCategoryCategoryId(categoryId);
  }

  default List<Product> findByBrandId(Long brandId) {
    return findByBrandBrandId(brandId);
  }

  default List<Product> findByShopIdAndCategoryId(Long shopId, Long categoryId) {
    return findByShopShopIdAndCategoryCategoryId(shopId, categoryId);
  }

  long countByQuantityLessThanEqual(int quantity);

  @Query("SELECT p FROM Product p WHERE p.shop.shopId = :shopId")
  List<Product> findTrendingByShopId(@Param("shopId") Long shopId);
}
