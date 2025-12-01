/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.domain.Category;
import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.jpa.dao.CategoryDao;
import com.xplaza.backend.jpa.repository.CategoryRepository;
import com.xplaza.backend.mapper.CategoryMapper;

@Service
@RequiredArgsConstructor
public class CategoryService {
  private final CategoryRepository categoryRepo;
  private final CategoryMapper categoryMapper;

  @Transactional
  public Category addCategory(Category category) {
    CategoryDao categoryDao = categoryMapper.toDao(category);
    CategoryDao savedCategoryDao = categoryRepo.save(categoryDao);
    return categoryMapper.toEntityFromDao(savedCategoryDao);
  }

  @Transactional
  public Category updateCategory(Long id, Category category) {
    // Check if category exists
    categoryRepo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

    category.setCategoryId(id);
    CategoryDao categoryDao = categoryMapper.toDao(category);
    CategoryDao updatedCategoryDao = categoryRepo.save(categoryDao);
    return categoryMapper.toEntityFromDao(updatedCategoryDao);
  }

  @Transactional
  public void deleteCategory(Long id) {
    if (!categoryRepo.existsById(id)) {
      throw new ResourceNotFoundException("Category not found with id: " + id);
    }
    categoryRepo.deleteById(id);
  }

  public List<Category> listCategories() {
    List<CategoryDao> categoryDaos = categoryRepo.findAll();
    return categoryDaos.stream()
        .map(categoryMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }

  public Category listCategory(Long id) {
    CategoryDao categoryDao = categoryRepo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    return categoryMapper.toEntityFromDao(categoryDao);
  }

  public String getCategoryNameByID(Long id) {
    return categoryRepo.getName(id);
  }

  // ===== Paginated Methods =====

  public Page<Category> listCategoriesPaginated(Pageable pageable) {
    return categoryRepo.findAll(pageable)
        .map(categoryMapper::toEntityFromDao);
  }

  public Page<Category> searchCategories(String searchTerm, Pageable pageable) {
    return categoryRepo.findByCategoryNameContainingIgnoreCase(searchTerm, pageable)
        .map(categoryMapper::toEntityFromDao);
  }

  public Page<Category> listCategoriesByParent(Long parentId, Pageable pageable) {
    return categoryRepo.findByParentCategoryCategoryId(parentId, pageable)
        .map(categoryMapper::toEntityFromDao);
  }
}
