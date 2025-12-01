/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;

import com.xplaza.backend.domain.Customer;
import com.xplaza.backend.http.dto.request.CustomerUserRequest;
import com.xplaza.backend.http.dto.response.CustomerUserResponse;
import com.xplaza.backend.jpa.dao.CustomerDao;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
  Customer toEntity(CustomerUserRequest request);

  CustomerUserResponse toResponse(Customer entity);

  CustomerDao toDao(Customer entity);

  Customer toEntityFromDao(CustomerDao dao);
}