/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.jpa.dao.CustomerDao;
import com.xplaza.backend.jpa.repository.CustomerRepository;
import com.xplaza.backend.jpa.repository.CustomerUserRepository;
import com.xplaza.backend.mapper.CustomerMapper;
import com.xplaza.backend.service.entity.Customer;

@Service
public class CustomerUserService {
  private final CustomerUserRepository customerUserRepo;
  private final CustomerRepository customerRepo;
  private final CustomerMapper customerMapper;

  @Autowired
  public CustomerUserService(CustomerUserRepository customerUserRepo, CustomerRepository customerRepo,
      CustomerMapper customerMapper) {
    this.customerUserRepo = customerUserRepo;
    this.customerRepo = customerRepo;
    this.customerMapper = customerMapper;
  }

  @Transactional
  public void updateCustomer(Customer entity) {
    CustomerDao dao = customerMapper.toDao(entity);
    customerRepo.save(dao);
  }

  @Transactional
  public void deleteCustomer(Long id) {
    customerUserRepo.deleteById(id);
  }

  public List<Customer> listCustomers() {
    return customerUserRepo.findAll().stream().map(customerMapper::toEntityFromDao).toList();
  }

  public Customer listCustomer(String username) {
    return customerMapper.toEntityFromDao(customerUserRepo.findCustomerByUsername(username));
  }

  @Transactional
  public void changeCustomerPassword(String new_password, String salt, String user_name) {
    customerUserRepo.changePassword(new_password, salt, user_name);
  }

  public Customer getCustomer(Long id) {
    return customerMapper.toEntityFromDao(customerRepo.findById(id).orElseThrow(
        () -> new ResourceNotFoundException("Customer not found with id: " + id)));
  }
}
