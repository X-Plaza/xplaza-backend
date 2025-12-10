/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.catalog.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.xplaza.backend.catalog.domain.entity.Product;
import com.xplaza.backend.catalog.domain.entity.ProductImage;
import com.xplaza.backend.catalog.domain.entity.ProductVariant;
import com.xplaza.backend.catalog.domain.entity.VariantImage;
import com.xplaza.backend.catalog.domain.repository.ProductImageRepository;
import com.xplaza.backend.catalog.domain.repository.ProductRepository;
import com.xplaza.backend.catalog.domain.repository.ProductVariantRepository;
import com.xplaza.backend.catalog.domain.repository.VariantImageRepository;
import com.xplaza.backend.common.service.FileStorageService;
import com.xplaza.backend.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ProductService {
  private final ProductRepository productRepository;
  private final ProductImageRepository productImageRepository;
  private final ProductVariantRepository productVariantRepository;
  private final VariantImageRepository variantImageRepository;
  private final FileStorageService fileStorageService;

  @Transactional
  public Product addProduct(Product product) {
    return productRepository.save(product);
  }

  @Transactional
  public Product updateProduct(Product product) {
    productRepository.findById(product.getProductId())
        .orElseThrow(
            () -> new ResourceNotFoundException("Product not found with id: " + product.getProductId()));
    return productRepository.save(product);
  }

  @Transactional
  public void deleteProduct(Long id) {
    if (!productRepository.existsById(id)) {
      throw new ResourceNotFoundException("Product not found with id: " + id);
    }
    productRepository.deleteById(id);
  }

  public List<Product> listProducts() {
    return productRepository.findAll();
  }

  public Product listProduct(Long id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
  }

  public List<Product> listProductsByShop(Long shopId) {
    return productRepository.findByShopId(shopId);
  }

  public List<Product> listProductsByCategory(Long categoryId) {
    return productRepository.findByCategoryId(categoryId);
  }

  public List<Product> listProductsByBrand(Long brandId) {
    return productRepository.findByBrandId(brandId);
  }

  public Page<Product> findProducts(Pageable pageable) {
    return productRepository.findAll(pageable);
  }

  public Page<Product> findProductsByShop(Long shopId, Pageable pageable) {
    return productRepository.findByShopShopId(shopId, pageable);
  }

  public Page<Product> findProductsByCategory(Long categoryId, Pageable pageable) {
    return productRepository.findByCategoryCategoryId(categoryId, pageable);
  }

  public Page<Product> findProductsByBrand(Long brandId, Pageable pageable) {
    return productRepository.findByBrandBrandId(brandId, pageable);
  }

  public Page<Product> searchProductsByName(String name, Pageable pageable) {
    return productRepository.findByProductNameContainingIgnoreCase(name, pageable);
  }

  public Page<Product> findProductsByShopAndCategory(Long shopId, Long categoryId, Pageable pageable) {
    return productRepository.findByShopShopIdAndCategoryCategoryId(shopId, categoryId, pageable);
  }

  public String getProductNameByID(Long id) {
    return productRepository.getName(id);
  }

  @Transactional
  public void updateProductInventory(Long id, int quantity) {
    if (!productRepository.existsById(id)) {
      throw new ResourceNotFoundException("Product not found with id: " + id);
    }
    productRepository.updateInventory(id, quantity);
  }

  public boolean exists(Long id) {
    return productRepository.existsById(id);
  }

  @Transactional
  public List<String> uploadProductImages(Long productId, UUID variantId, List<MultipartFile> files) {
    Product product = listProduct(productId);
    List<String> uploadedUrls = new java.util.ArrayList<>();

    if (variantId != null) {
      ProductVariant variant = productVariantRepository.findById(variantId)
          .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));

      if (!variant.getProductId().equals(productId)) {
        throw new com.xplaza.backend.exception.ValidationException("Variant does not belong to the specified product");
      }

      long currentImageCount = variantImageRepository.countByVariantVariantId(variantId);
      if (currentImageCount + files.size() > 10) {
        throw new com.xplaza.backend.exception.ValidationException("Variant cannot have more than 10 images. Current: "
            + currentImageCount + ", Attempting to add: " + files.size());
      }

      for (MultipartFile file : files) {
        String imageUrl = fileStorageService.uploadFile(file);
        VariantImage variantImage = new VariantImage();
        variantImage.setVariant(variant);
        variantImage.setUrl(imageUrl);
        variantImage.setAltText(file.getOriginalFilename());
        variantImageRepository.save(variantImage);
        uploadedUrls.add(imageUrl);
      }
    } else {
      long currentImageCount = productImageRepository.countByProductProductId(productId);

      if (currentImageCount + files.size() > 10) {
        throw new com.xplaza.backend.exception.ValidationException("Product cannot have more than 10 images. Current: "
            + currentImageCount + ", Attempting to add: " + files.size());
      }

      for (MultipartFile file : files) {
        String imageUrl = fileStorageService.uploadFile(file);
        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setProductImageName(file.getOriginalFilename());
        productImage.setProductImagePath(imageUrl);
        productImage.setCreatedAt(new Date());
        productImageRepository.save(productImage);
        uploadedUrls.add(imageUrl);
      }
    }
    return uploadedUrls;
  }
}
