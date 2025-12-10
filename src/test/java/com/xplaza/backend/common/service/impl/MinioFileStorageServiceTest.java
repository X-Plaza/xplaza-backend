/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.common.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.xplaza.backend.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
class MinioFileStorageServiceTest {

  @Mock
  private MinioClient minioClient;

  @InjectMocks
  private MinioFileStorageService minioFileStorageService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(minioFileStorageService, "bucketName", "test-bucket");
    ReflectionTestUtils.setField(minioFileStorageService, "minioUrl", "http://localhost:9000");
  }

  @Test
  void uploadFile_ValidFile_Success() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getSize()).thenReturn(1024L);
    when(file.getContentType()).thenReturn("image/jpeg");
    when(file.getOriginalFilename()).thenReturn("test.jpg");
    when(file.getInputStream()).thenReturn(mock(InputStream.class));

    when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

    minioFileStorageService.uploadFile(file);
  }

  @Test
  void uploadFile_EmptyFile_ThrowsValidationException() {
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(true);

    assertThrows(ValidationException.class, () -> minioFileStorageService.uploadFile(file));
  }

  @Test
  void uploadFile_FileTooLarge_ThrowsValidationException() {
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getSize()).thenReturn(6 * 1024 * 1024L); // 6MB

    assertThrows(ValidationException.class, () -> minioFileStorageService.uploadFile(file));
  }

  @Test
  void uploadFile_InvalidContentType_ThrowsValidationException() {
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getSize()).thenReturn(1024L);
    when(file.getContentType()).thenReturn("application/pdf");

    assertThrows(ValidationException.class, () -> minioFileStorageService.uploadFile(file));
  }

  @Test
  void uploadFile_PathTraversal_ThrowsValidationException() {
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getSize()).thenReturn(1024L);
    when(file.getContentType()).thenReturn("image/jpeg");
    when(file.getOriginalFilename()).thenReturn("../test.jpg");

    assertThrows(ValidationException.class, () -> minioFileStorageService.uploadFile(file));
  }

  @Test
  void uploadFile_SpecialCharacters_Sanitized() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getSize()).thenReturn(1024L);
    when(file.getContentType()).thenReturn("image/jpeg");
    when(file.getOriginalFilename()).thenReturn("test@#$%.jpg");
    when(file.getInputStream()).thenReturn(mock(InputStream.class));

    when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

    String fileUrl = minioFileStorageService.uploadFile(file);

    assertTrue(fileUrl.contains("test____.jpg"));
  }
}
