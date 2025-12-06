/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.customer.service;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.auth.dto.request.AuthenticationRequest;
import com.xplaza.backend.auth.dto.response.AuthenticationResponse;
import com.xplaza.backend.common.util.JwtUtil;
import com.xplaza.backend.customer.domain.entity.Customer;
import com.xplaza.backend.customer.domain.repository.CustomerRepository;
import com.xplaza.backend.customer.dto.CustomerRequest;
import com.xplaza.backend.exception.ResourceAlreadyExistsException;
import com.xplaza.backend.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class CustomerService {

  private final CustomerRepository customerRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  @Transactional
  public AuthenticationResponse register(CustomerRequest request) {
    if (customerRepository.existsByEmail(request.getEmail())) {
      throw new ResourceAlreadyExistsException("Email already exists: " + request.getEmail());
    }

    Customer customer = Customer.builder()
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .phoneNumber(request.getPhoneNumber())
        .role("CUSTOMER")
        .enabled(true)
        .build();

    customer = customerRepository.save(customer);

    String jwtToken = jwtUtil.generateJwtToken(customer);
    String refreshToken = jwtUtil.generateRefreshToken(customer);

    return new AuthenticationResponse(jwtToken, refreshToken);
  }

  @Transactional
  public AuthenticationResponse login(AuthenticationRequest request) {
    Customer customer = customerRepository.findByEmail(request.getUsername())
        .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

    if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
      throw new BadCredentialsException("Invalid email or password");
    }

    customer.setLastLoginAt(LocalDateTime.now());
    customerRepository.save(customer);

    String jwtToken = jwtUtil.generateJwtToken(customer);
    String refreshToken = jwtUtil.generateRefreshToken(customer);

    return new AuthenticationResponse(jwtToken, refreshToken);
  }

  @Transactional(readOnly = true)
  public Customer getCustomer(Long id) {
    return customerRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
  }

  @Transactional
  public Customer updateProfile(Long id, CustomerRequest request) {
    Customer customer = getCustomer(id);
    customer.setFirstName(request.getFirstName());
    customer.setLastName(request.getLastName());
    customer.setPhoneNumber(request.getPhoneNumber());
    // Email update might require verification, skipping for now
    return customerRepository.save(customer);
  }
}
