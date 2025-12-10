/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.common.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
  String uploadFile(MultipartFile file);

  void deleteFile(String fileUrl);
}
