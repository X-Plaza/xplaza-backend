/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import lombok.RequiredArgsConstructor;

import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.xplaza.backend.common.util.ValidationUtil;
import com.xplaza.backend.domain.Customer;
import com.xplaza.backend.domain.PlatformInfo;
import com.xplaza.backend.jpa.dao.CustomerDao;
import com.xplaza.backend.jpa.repository.CustomerSignupRepository;
import com.xplaza.backend.mapper.CustomerMapper;

@Service
@RequiredArgsConstructor
public class CustomerSignupService {
  private final CustomerSignupRepository customerSignupRepo;
  private final CustomerMapper customerMapper;
  private final EmailSenderService emailSenderService;
  private final PlatformInfoService platformInfoService;
  private final Environment env;

  public void signupCustomer(Customer customer) {
    // Validate input
    ValidationUtil.validateNotNull(customer, "Customer");
    ValidationUtil.validateNotEmpty(customer.getFirstName(), "First name");
    ValidationUtil.validateNotEmpty(customer.getLastName(), "Last name");
    ValidationUtil.validateNotEmpty(customer.getMobileNo(), "Mobile number");
    ValidationUtil.validatePhoneNumber(customer.getMobileNo());

    if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
      ValidationUtil.validateEmail(customer.getEmail());
    }

    CustomerDao customerDao = customerMapper.toDao(customer);
    customerSignupRepo.save(customerDao);
  }

  public void sendLoginDetails(String email, String password) {
    // Get platform info
    PlatformInfo platformInfo = platformInfoService.listPlatform();
    // Send email
    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setFrom(env.getProperty("spring.mail.username"));
    mailMessage.setTo(email);
    mailMessage.setSubject("Your " + platformInfo.getName() + ".com Login Details!");
    mailMessage.setText(
        "Congratulations! Your " + platformInfo.getName() + ".com Customer account has been created successfully.\n\n" +
            "Please use the following login details to login to the " + platformInfo.getName() + " account.\n\n" +
            "Email : " + email + "\n" + "Password : " + password + "\n\n" +
            "With Regards,\n" + "Team " + platformInfo.getName());
    emailSenderService.sendEmail(mailMessage);
  }
}
