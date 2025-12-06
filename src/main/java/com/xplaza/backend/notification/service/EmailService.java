/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;

  @Async
  public void sendEmail(String to, String subject, String text) {
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

      Context context = new Context();
      context.setVariable("title", subject);
      context.setVariable("message", text);

      String htmlContent = templateEngine.process("email-template", context);

      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlContent, true); // true = isHtml
      helper.setFrom("noreply@xplaza.com");

      mailSender.send(mimeMessage);
      log.info("HTML Email sent to {}", to);
    } catch (MessagingException e) {
      log.error("Failed to send HTML email to {}", to, e);
    }
  }
}
