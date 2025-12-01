/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.jpa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.jpa.dao.ProductDao;

public interface ProductRepository extends JpaRepository<ProductDao, Long> {

  // JPQL query - more portable than native SQL
  @Query("SELECT p.productName FROM ProductDao p WHERE p.productId = :id")
  String getName(@Param("id") Long id);

  // Use Optional for better null handling
  Optional<ProductDao> findByProductId(Long productId);

  // Legacy method for backward compatibility - returns null instead of Optional
  default ProductDao findProductById(Long productId) {
    return findByProductId(productId).orElse(null);
  }

  // Check existence before operations
  boolean existsByProductId(Long productId);

  @Modifying
  @Transactional
  @Query("UPDATE ProductDao p SET p.quantity = :quantity WHERE p.productId = :id")
  void updateInventory(@Param("id") Long id, @Param("quantity") int quantity);

  /**
   * Atomically decrement inventory. Returns the number of rows affected. If 0
   * rows affected, it means insufficient stock. This prevents race conditions
   * during concurrent order placement.
   */
  @Modifying
  @Transactional
  @Query("UPDATE ProductDao p SET p.quantity = p.quantity - :decrement WHERE p.productId = :id AND p.quantity >= :decrement")
  int decrementInventory(@Param("id") Long id, @Param("decrement") Long decrement);

  /**
   * Atomically increment inventory (for order cancellations, returns,
   * restocking).
   */
  @Modifying
  @Transactional
  @Query("UPDATE ProductDao p SET p.quantity = p.quantity + :increment WHERE p.productId = :id")
  int incrementInventory(@Param("id") Long id, @Param("increment") Long increment);

  /**
   * Find the shop ID for a product. Used for authorization checks.
   */
  @Query("SELECT p.shop.shopId FROM ProductDao p WHERE p.productId = :productId")
  Long findShopIdByProductId(@Param("productId") Long productId);

  // Pagination support - using native query because of link table
  @Query(value = "SELECT p.* FROM products p " +
      "JOIN shops s ON p.shop_id = s.shop_id " +
      "JOIN admin_user_shop_link ausl ON s.shop_id = ausl.shop_id " +
      "WHERE ausl.admin_user_id = :userId", nativeQuery = true)
  Page<ProductDao> findByUserId(@Param("userId") Long userId, Pageable pageable);

  // Non-paginated version for backward compatibility - using native query
  @Query(value = "SELECT p.* FROM products p " +
      "JOIN shops s ON p.shop_id = s.shop_id " +
      "JOIN admin_user_shop_link ausl ON s.shop_id = ausl.shop_id " +
      "WHERE ausl.admin_user_id = :userId", nativeQuery = true)
  List<ProductDao> findByUserId(@Param("userId") Long userId);

  // Pagination support
  Page<ProductDao> findByShopShopId(Long shopId, Pageable pageable);

  // Non-paginated versions using Spring Data derived queries
  List<ProductDao> findByShopShopId(Long shopId);

  Page<ProductDao> findByCategoryCategoryId(Long categoryId, Pageable pageable);

  List<ProductDao> findByCategoryCategoryId(Long categoryId);

  Page<ProductDao> findByBrandBrandId(Long brandId, Pageable pageable);

  List<ProductDao> findByBrandBrandId(Long brandId);

  // Combined filters with pagination
  Page<ProductDao> findByShopShopIdAndCategoryCategoryId(Long shopId, Long categoryId, Pageable pageable);

  List<ProductDao> findByShopShopIdAndCategoryCategoryId(Long shopId, Long categoryId);

  // Search by name with pagination
  Page<ProductDao> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);

  List<ProductDao> findByProductNameContainingIgnoreCase(String productName);

  // Legacy method names for backward compatibility
  default List<ProductDao> findByShopId(Long shopId) {
    return findByShopShopId(shopId);
  }

  default List<ProductDao> findByCategoryId(Long categoryId) {
    return findByCategoryCategoryId(categoryId);
  }

  default List<ProductDao> findByBrandId(Long brandId) {
    return findByBrandBrandId(brandId);
  }

  default List<ProductDao> findByShopIdAndCategoryId(Long shopId, Long categoryId) {
    return findByShopShopIdAndCategoryCategoryId(shopId, categoryId);
  }

  // Deprecated - use findByShopShopId with isTrending filter instead
  @Query("SELECT p FROM ProductDao p WHERE p.shop.shopId = :shopId")
  List<ProductDao> findTrendingByShopId(@Param("shopId") Long shopId);
}
