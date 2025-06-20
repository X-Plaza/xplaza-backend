/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;

import com.xplaza.backend.jpa.dao.UserDao;
import com.xplaza.backend.service.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserDao toDao(User entity);

  User toEntityFromDao(UserDao dao);
}