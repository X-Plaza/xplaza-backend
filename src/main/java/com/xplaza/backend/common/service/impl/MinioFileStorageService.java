/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.common.service.impl;

import java.io.InputStream;
import java.util.UUID;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.xplaza.backend.common.service.FileStorageService;
import com.xplaza.backend.exception.FileStorageException;

@Service
public class MinioFileStorageService implements FileStorageService {

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
    try {
      boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
      if (!found) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
      }

      String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
      InputStream inputStream = file.getInputStream();

      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(bucketName)
              .object(fileName)
              .stream(inputStream, file.getSize(), -1)
              .contentType(file.getContentType())
              .build());

      return minioUrl + "/" + bucketName + "/" + fileName;
    } catch (Exception e) {
      throw new FileStorageException("Error uploading file to MinIO", e);
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
      throw new FileStorageException("Error deleting file from MinIO", e);
    }
  }
}
