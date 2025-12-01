/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.domain.Role;
import com.xplaza.backend.jpa.dao.RoleDao;
import com.xplaza.backend.jpa.repository.RoleRepository;
import com.xplaza.backend.mapper.RoleMapper;

@Service
@RequiredArgsConstructor
public class RoleService {
  private final RoleRepository roleRepo;
  private final RoleMapper roleMapper;

  @Transactional
  public void addRole(Role entity) {
    RoleDao dao = roleMapper.toDao(entity);
    roleRepo.save(dao);
  }

  @Transactional
  public void updateRole(Role entity) {
    RoleDao dao = roleMapper.toDao(entity);
    roleRepo.save(dao);
  }

  public List<Role> listRoles() {
    return roleRepo.findAll().stream().map(roleMapper::toEntityFromDao).collect(Collectors.toList());
  }

  @Transactional
  public void deleteRole(Long id) {
    roleRepo.deleteById(id);
  }

  public Role listRole(Long id) {
    return roleRepo.findById(id)
        .map(roleMapper::toEntityFromDao).orElse(null);
  }

  public String getRoleNameByID(Long id) {
    // Implement if needed in DAO repo
    return null;
  }

  public String getRoleNameByUserID(Long id) {
    // Implement if needed in DAO repo
    return null;
  }
}
