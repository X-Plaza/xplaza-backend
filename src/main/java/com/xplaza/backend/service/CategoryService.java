/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.jpa.dao.CategoryDao;
import com.xplaza.backend.jpa.repository.CategoryRepository;
import com.xplaza.backend.mapper.CategoryMapper;
import com.xplaza.backend.service.entity.Category;

@Service
public class CategoryService {
  private final CategoryRepository categoryRepo;
  private final CategoryMapper categoryMapper;

  @Autowired
  public CategoryService(CategoryRepository categoryRepo, CategoryMapper categoryMapper) {
    this.categoryRepo = categoryRepo;
    this.categoryMapper = categoryMapper;
  }

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
}
