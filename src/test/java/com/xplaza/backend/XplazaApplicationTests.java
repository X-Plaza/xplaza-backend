/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.xplaza.backend.config.TestConfig;

@SpringBootTest
@Import(TestConfig.class)
class XplazaApplicationTests {

  @Test
  void contextLoads() {
  }

}
