/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.common.service.impl;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.xplaza.backend.common.service.FileStorageService;
import com.xplaza.backend.exception.FileStorageException;
import com.xplaza.backend.exception.ValidationException;

@Service
public class MinioFileStorageService implements FileStorageService {

  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
  private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
      "image/jpeg", "image/png", "image/gif", "image/jpg");

  private final MinioClient minioClient;

  @Value("${minio.bucket-name}")
  private String bucketName;

  @Value("${minio.url}")
  private String minioUrl;

  public MinioFileStorageService(MinioClient minioClient) {
    this.minioClient = minioClient;
  }

  @Override
  public String uploadFile(MultipartFile file) {
    validateFile(file);
    try {
      boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
      if (!found) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
      }

      String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
      if (originalFilename.contains("..")) {
        throw new ValidationException("Filename contains invalid path sequence " + originalFilename);
      }

      // Sanitize filename: remove special characters and limit length
      String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
      if (sanitizedFilename.length() > 50) {
        sanitizedFilename = sanitizedFilename.substring(0, 50);
      }

      String fileName = UUID.randomUUID() + "-" + sanitizedFilename;
      InputStream inputStream = file.getInputStream();

      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(bucketName)
              .object(fileName)
              .stream(inputStream, file.getSize(), -1)
              .contentType(file.getContentType())
              .build());

      return minioUrl + "/" + bucketName + "/" + fileName;
    } catch (ValidationException e) {
      throw e;
    } catch (Exception e) {
      throw new FileStorageException("Error uploading file to MinIO for file: " + file.getOriginalFilename(), e);
    }
  }

  private void validateFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new ValidationException("Failed to store empty file.");
    }
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new ValidationException("File size exceeds limit of 10MB.");
    }
    if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
      throw new ValidationException("Invalid file type. Allowed types: " + ALLOWED_CONTENT_TYPES);
    }
  }

  @Override
  public void deleteFile(String fileUrl) {
    try {
      String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
      minioClient.removeObject(
          RemoveObjectArgs.builder()
              .bucket(bucketName)
              .object(fileName)
              .build());
    } catch (Exception e) {
      throw new FileStorageException("Error deleting file from MinIO for file: " + fileUrl, e);
    }
  }
}
