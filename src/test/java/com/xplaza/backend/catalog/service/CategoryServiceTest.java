/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.catalog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.xplaza.backend.catalog.domain.entity.Category;
import com.xplaza.backend.catalog.domain.repository.CategoryRepository;
import com.xplaza.backend.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepo;

  @InjectMocks
  private CategoryService categoryService;

  @Test
  void addCategory_ShouldSaveAndReturnCategory() {
    Category category = new Category();
    category.setCategoryName("Electronics");

    when(categoryRepo.save(category)).thenReturn(category);

    Category savedCategory = categoryService.addCategory(category);

    assertNotNull(savedCategory);
    assertEquals("Electronics", savedCategory.getCategoryName());
    verify(categoryRepo, times(1)).save(category);
  }

  @Test
  void updateCategory_ShouldUpdateAndReturnCategory_WhenCategoryExists() {
    Long id = 1L;
    Category existingCategory = new Category();
    existingCategory.setCategoryId(id);
    existingCategory.setCategoryName("Old Name");

    Category updateRequest = new Category();
    updateRequest.setCategoryName("New Name");

    when(categoryRepo.findById(id)).thenReturn(Optional.of(existingCategory));
    when(categoryRepo.save(updateRequest)).thenReturn(updateRequest);

    Category updatedCategory = categoryService.updateCategory(id, updateRequest);

    assertNotNull(updatedCategory);
    assertEquals("New Name", updatedCategory.getCategoryName());
    assertEquals(id, updatedCategory.getCategoryId());
    verify(categoryRepo, times(1)).findById(id);
    verify(categoryRepo, times(1)).save(updateRequest);
  }

  @Test
  void updateCategory_ShouldThrowException_WhenCategoryDoesNotExist() {
    Long id = 1L;
    Category updateRequest = new Category();

    when(categoryRepo.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(id, updateRequest));
    verify(categoryRepo, never()).save(any());
  }

  @Test
  void deleteCategory_ShouldDelete_WhenCategoryExists() {
    Long id = 1L;

    when(categoryRepo.existsById(id)).thenReturn(true);
    doNothing().when(categoryRepo).deleteById(id);

    categoryService.deleteCategory(id);

    verify(categoryRepo, times(1)).existsById(id);
    verify(categoryRepo, times(1)).deleteById(id);
  }

  @Test
  void deleteCategory_ShouldThrowException_WhenCategoryDoesNotExist() {
    Long id = 1L;

    when(categoryRepo.existsById(id)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(id));
    verify(categoryRepo, never()).deleteById(any());
  }

  @Test
  void listCategories_ShouldReturnList() {
    Category category = new Category();
    when(categoryRepo.findAll()).thenReturn(List.of(category));

    List<Category> categories = categoryService.listCategories();

    assertFalse(categories.isEmpty());
    assertEquals(1, categories.size());
  }

  @Test
  void listCategory_ShouldReturnCategory_WhenExists() {
    Long id = 1L;
    Category category = new Category();
    category.setCategoryId(id);

    when(categoryRepo.findById(id)).thenReturn(Optional.of(category));

    Category foundCategory = categoryService.listCategory(id);

    assertNotNull(foundCategory);
    assertEquals(id, foundCategory.getCategoryId());
  }

  @Test
  void listCategory_ShouldThrowException_WhenDoesNotExist() {
    Long id = 1L;
    when(categoryRepo.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> categoryService.listCategory(id));
  }

  @Test
  void listCategoriesPaginated_ShouldReturnPage() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Category> page = new PageImpl<>(Collections.emptyList());

    when(categoryRepo.findAll(pageable)).thenReturn(page);

    Page<Category> result = categoryService.listCategoriesPaginated(pageable);

    assertNotNull(result);
    verify(categoryRepo).findAll(pageable);
  }
}
