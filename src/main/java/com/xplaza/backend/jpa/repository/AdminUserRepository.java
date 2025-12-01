/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.jpa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.jpa.dao.AdminUserDao;

public interface AdminUserRepository extends JpaRepository<AdminUserDao, Long> {

  // JPQL query - more portable than native SQL
  @Query("SELECT u.userName FROM AdminUserDao u WHERE u.adminUserId = :id")
  String getName(@Param("id") Long id);

  // Use Spring Data derived query with Optional
  Optional<AdminUserDao> findByUserName(String userName);

  // Legacy method for backward compatibility
  default AdminUserDao findUserByUsername(String username) {
    return findByUserName(username).orElse(null);
  }

  @Modifying
  @Transactional
  @Query("UPDATE AdminUserDao u SET u.role.roleId = :roleId, u.fullName = :fullName WHERE u.adminUserId = :adminUserId")
  void update(@Param("roleId") Long roleId, @Param("fullName") String fullName, @Param("adminUserId") Long adminUserId);

  @Modifying
  @Transactional
  @Query("UPDATE AdminUserDao u SET u.password = :newPassword, u.salt = :salt WHERE u.userName = :userName")
  void changePassword(@Param("newPassword") String newPassword, @Param("salt") String salt,
      @Param("userName") String userName);

  // JPQL query with join through shopLinks
  @Query("SELECT u.userName FROM AdminUserDao u JOIN u.shopLinks sl WHERE sl.shop.shopId = :shopId")
  List<String> getEmailListByShopId(@Param("shopId") Long shopId);
}
