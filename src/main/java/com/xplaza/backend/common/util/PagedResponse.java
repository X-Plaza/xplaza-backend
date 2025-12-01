/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.common.util;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Wrapper for paginated API responses.
 *
 * @param <T> The type of content being paginated
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {
  private List<T> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
  private boolean first;
  private boolean last;

  public PagedResponse(Page<T> page) {
    this.content = page.getContent();
    this.page = page.getNumber();
    this.size = page.getSize();
    this.totalElements = page.getTotalElements();
    this.totalPages = page.getTotalPages();
    this.first = page.isFirst();
    this.last = page.isLast();
  }

  public PagedResponse(List<T> content, int page, int size, long totalElements) {
    this.content = content;
    this.page = page;
    this.size = size;
    this.totalElements = totalElements;
    this.totalPages = (int) Math.ceil((double) totalElements / size);
    this.first = page == 0;
    this.last = page >= totalPages - 1;
  }
}
