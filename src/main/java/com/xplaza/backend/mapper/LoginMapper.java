/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;

import com.xplaza.backend.domain.Login;
import com.xplaza.backend.jpa.dao.LoginDao;

@Mapper(componentModel = "spring")
public interface LoginMapper {
  LoginDao toDao(Login entity);

  Login toEntityFromDao(LoginDao dao);
}
