/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.jpa.dao.AdminUserShopLinkDao;
import com.xplaza.backend.jpa.dao.AdminUserShopLinkIdDao;

public interface AdminUserShopLinkRepository extends JpaRepository<AdminUserShopLinkDao, AdminUserShopLinkIdDao> {
  @Modifying
  @Transactional
  @Query(value = "insert into admin_user_shop_link values(?1, ?2)", nativeQuery = true)
  void insert(Long admin_user_id, Long shop_id);

  @Modifying
  @Transactional
  @Query(value = "delete from admin_user_shop_link where admin_user_id = ?1", nativeQuery = true)
  void deleteByAdminUserID(Long id);

  /**
   * Check if a user has access to a specific shop. Used for shop-level
   * authorization.
   */
  @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM admin_user_shop_link WHERE admin_user_id = ?1 AND shop_id = ?2", nativeQuery = true)
  boolean existsByAdminUserIdAndShopId(Long adminUserId, Long shopId);

  /**
   * Count the number of shops a user has access to. Used to identify Master Admin
   * (who typically has access to all shops or special role).
   */
  @Query(value = "SELECT COUNT(*) FROM admin_user_shop_link WHERE admin_user_id = ?1", nativeQuery = true)
  long countShopsByUserId(Long userId);
}
