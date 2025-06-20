/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.jpa.dao.ConfirmationTokenDao;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationTokenDao, Long> {
  @Query(value = "select * from confirmation_tokens where confirmation_token = ?1", nativeQuery = true)
  ConfirmationTokenDao findByConfirmationToken(String confirmation_token);

  @Modifying
  @Transactional
  @Query(value = "delete from confirmation_tokens where admin_user_id=?1", nativeQuery = true)
  void deleteByUserID(Long id);
}
