/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.common.util;

import lombok.NoArgsConstructor;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Provides static methods about the current request
 */
@NoArgsConstructor
public class PathProvider {
  /**
   * returns the current path of the request
   */
  public static String getCurrentPath() {
    return ServletUriComponentsBuilder.fromCurrentRequest().build().getPath();
  }

}
