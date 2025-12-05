/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.catalog.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.catalog.domain.entity.Category;
import com.xplaza.backend.catalog.domain.repository.CategoryRepository;
import com.xplaza.backend.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepo;

  @Transactional
  public Category addCategory(Category category) {
    return categoryRepo.save(category);
  }

  @Transactional
  public Category updateCategory(Long id, Category category) {
    Category existingCategory = categoryRepo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

    category.setCategoryId(id);
    // If parent category is being updated, we might need to handle it, but simple
    // save usually works if object is set up right
    return categoryRepo.save(category);
  }

  @Transactional
  public void deleteCategory(Long id) {
    if (!categoryRepo.existsById(id)) {
      throw new ResourceNotFoundException("Category not found with id: " + id);
    }
    categoryRepo.deleteById(id);
  }

  public List<Category> listCategories() {
    return categoryRepo.findAll();
  }

  public Category listCategory(Long id) {
    return categoryRepo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
  }

  public String getCategoryNameByID(Long id) {
    return categoryRepo.getName(id);
  }

  public Page<Category> listCategoriesPaginated(Pageable pageable) {
    return categoryRepo.findAll(pageable);
  }

  public Page<Category> searchCategories(String searchTerm, Pageable pageable) {
    return categoryRepo.findByCategoryNameContainingIgnoreCase(searchTerm, pageable);
  }

  public Page<Category> listCategoriesByParent(Long parentId, Pageable pageable) {
    return categoryRepo.findByParentCategoryCategoryId(parentId, pageable);
  }
}
