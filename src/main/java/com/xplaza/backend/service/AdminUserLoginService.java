/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import java.util.Arrays;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.domain.AdminUser;
import com.xplaza.backend.domain.Login;
import com.xplaza.backend.jpa.repository.LoginRepository;
import com.xplaza.backend.mapper.LoginMapper;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminUserLoginService {
  private final AdminUserService adminUserService;
  private final SecurityService securityService;
  private final LoginRepository loginRepo;
  private final LoginMapper loginMapper;

  public boolean isValidAdminUser(String username, String password) {
    AdminUser adminUser = adminUserService.listAdminUser(username);
    if (adminUser == null) {
      return false;
    }
    String strOrgSalt = adminUser.getSalt();
    byte[] byteSalt = securityService.fromHex(strOrgSalt);
    byte[] loginPassword = securityService.getSaltedHashSHA512(password, byteSalt);
    byte[] storedPassword = securityService.fromHex(adminUser.getPassword());
    return Arrays.equals(loginPassword, storedPassword);
  }

  public Login getAdminUserDetails(String username) {
    return loginMapper.toEntityFromDao(loginRepo.findUserByUsername(username));
  }

  public boolean isValidMasterAdmin(String username, String password) {
    AdminUser adminUser = adminUserService.listAdminUser(username);
    if (adminUser == null) {
      return false;
    }
    String strOrgSalt = adminUser.getSalt();
    byte[] byteSalt = securityService.fromHex(strOrgSalt);
    byte[] loginPassword = securityService.getSaltedHashSHA512(password, byteSalt);
    byte[] storedPassword = securityService.fromHex(adminUser.getPassword());
    return Arrays.equals(loginPassword, storedPassword);
  }
}
