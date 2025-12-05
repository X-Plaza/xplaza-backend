/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.catalog.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.catalog.domain.entity.Currency;
import com.xplaza.backend.catalog.domain.repository.CurrencyRepository;
import com.xplaza.backend.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class CurrencyService {

  private final CurrencyRepository currencyRepository;

  @Transactional(readOnly = true)
  public List<Currency> listCurrencies() {
    return currencyRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Currency listCurrency(Long id) {
    return currencyRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Currency not found with id: " + id));
  }

  @Transactional
  public Currency addCurrency(Currency currency) {
    return currencyRepository.save(currency);
  }

  @Transactional
  public Currency updateCurrency(Long id, Currency currency) {
    Currency existing = listCurrency(id);
    existing.setCurrencyName(currency.getCurrencyName());
    existing.setCurrencySign(currency.getCurrencySign());
    return currencyRepository.save(existing);
  }

  @Transactional
  public void deleteCurrency(Long id) {
    Currency existing = listCurrency(id);
    currencyRepository.delete(existing);
  }
}
