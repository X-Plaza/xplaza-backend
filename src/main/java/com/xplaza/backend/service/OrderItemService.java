/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.common.util.ValidationUtil;
import com.xplaza.backend.domain.OrderItem;
import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.exception.ValidationException;
import com.xplaza.backend.jpa.dao.*;
import com.xplaza.backend.jpa.repository.*;
import com.xplaza.backend.mapper.OrderItemMapper;

@Service
@RequiredArgsConstructor
public class OrderItemService {
  private final OrderItemRepository orderItemRepo;
  private final OrderRepository orderRepo;
  private final ProductRepository productRepo;
  private final DeliveryCostRepository deliveryCostRepo;
  private final ProductDiscountRepository productDiscountRepo;
  private final OrderItemMapper orderItemMapper;
  private final CouponRepository couponRepo;

  @Transactional
  public OrderItem addOrderItem(OrderItem orderItem) {
    validateOrderItem(orderItem);
    OrderItemDao dao = orderItemMapper.toDao(orderItem);
    OrderDao order = getOrderOrThrow(dao.getOrder().getOrderId());
    ProductDao product = getProductOrThrow(dao.getProductId());

    Double originalSellingPrice = product.getProductSellingPrice();
    Double buyingPrice = product.getProductBuyingPrice();
    dao.setProductSellingPrice(originalSellingPrice);
    dao.setProductBuyingPrice(buyingPrice);

    Double discountAmount = calculateDiscountAmount(dao.getProductId(), originalSellingPrice);
    Double unitPrice = originalSellingPrice - discountAmount;
    dao.setUnitPrice(unitPrice);
    dao.setItemTotalPrice(unitPrice * dao.getQuantity());
    dao.setQuantityType("pc");

    Double itemTotalPrice = dao.getItemTotalPrice();
    Double netTotal = order.getNetTotal() + itemTotalPrice;
    Double totalPrice = order.getTotalPrice() + (originalSellingPrice * dao.getQuantity());
    Double deliveryCost = calculateDeliveryCost(netTotal);
    Double couponAmount = calculateCouponAmount(order, netTotal);
    updateOrderTotals(order, totalPrice, netTotal, deliveryCost, couponAmount);

    orderItemRepo.save(dao);
    orderRepo.save(order);
    updateProductInventory(product, -dao.getQuantity().intValue());
    return orderItemMapper.toEntityFromDao(dao);
  }

  public List<OrderItem> listOrderItems() {
    return orderItemRepo.findAll().stream().map(orderItemMapper::toEntityFromDao).toList();
  }

  public OrderItem listOrderItem(Long id) {
    OrderItemDao dao = orderItemRepo.findOrderItemById(id);
    return orderItemMapper.toEntityFromDao(dao);
  }

  public String getOrderItemNameByID(Long id) {
    return orderItemRepo.getName(id);
  }

  @Transactional
  public void deleteOrderItem(Long id) {
    if (id == null) {
      throw new ValidationException("Order item ID cannot be null");
    }
    OrderItemDao dao = orderItemRepo.findOrderItemById(id);
    if (dao == null) {
      throw new ResourceNotFoundException("Order item not found with id: " + id);
    }
    OrderItem entity = orderItemMapper.toEntityFromDao(dao);
    OrderDao order = getOrderOrThrow(entity.getOrderId());
    ProductDao product = getProductOrThrow(entity.getProductId());

    Double originalPrice = entity.getProductSellingPrice();
    Long oldQuantity = entity.getQuantity();
    Double unitPrice = entity.getUnitPrice();
    Double oldItemTotalPrice = entity.getItemTotalPrice();
    Double totalPrice = order.getTotalPrice() - (oldQuantity * originalPrice);
    Double netTotal = order.getNetTotal() - oldItemTotalPrice;
    Double deliveryCost = calculateDeliveryCost(netTotal);
    Double couponAmount = calculateCouponAmount(order, netTotal);
    updateOrderTotals(order, totalPrice, netTotal, deliveryCost, couponAmount);

    entity.setItemTotalPrice(0.0);
    entity.setQuantity(0L);
    orderItemRepo.save(orderItemMapper.toDao(entity));
    orderRepo.save(order);
    updateProductInventory(product, oldQuantity.intValue()); // Add back to inventory
  }

  @Transactional
  public OrderItem updateOrderItem(Long orderItemId, OrderItem updatedOrderItem) {
    ValidationUtil.validateId(orderItemId, "Order item ID");
    ValidationUtil.validatePositive(updatedOrderItem.getQuantity(), "Quantity");
    OrderItemDao dao = orderItemRepo.findOrderItemById(orderItemId);
    if (dao == null) {
      throw new ResourceNotFoundException("Order item not found with id: " + orderItemId);
    }
    OrderItem entity = orderItemMapper.toEntityFromDao(dao);
    Long oldQuantity = entity.getQuantity();
    entity.setQuantity(updatedOrderItem.getQuantity());
    entity.setQuantityType("pc");
    OrderDao order = getOrderOrThrow(entity.getOrderId());
    ProductDao product = getProductOrThrow(entity.getProductId());

    Double originalPrice = entity.getProductSellingPrice();
    Double unitPrice = entity.getUnitPrice();
    Double oldItemTotalPrice = entity.getItemTotalPrice();
    Double newItemTotalPrice = unitPrice * updatedOrderItem.getQuantity();
    Double netTotal = order.getNetTotal() - oldItemTotalPrice + newItemTotalPrice;
    Double totalPrice = order.getTotalPrice() - (oldQuantity * originalPrice)
        + (updatedOrderItem.getQuantity() * originalPrice);
    Double deliveryCost = calculateDeliveryCost(netTotal);
    Double couponAmount = calculateCouponAmount(order, netTotal);
    updateOrderTotals(order, totalPrice, netTotal, deliveryCost, couponAmount);

    entity.setItemTotalPrice(newItemTotalPrice);
    orderItemRepo.save(orderItemMapper.toDao(entity));
    orderRepo.save(order);
    updateProductInventory(product, oldQuantity.intValue() - updatedOrderItem.getQuantity().intValue());
    return entity;
  }

  // --- Helper Methods ---
  private void validateOrderItem(OrderItem orderItem) {
    if (orderItem == null) {
      throw new ValidationException("Order item cannot be null");
    }
    if (orderItem.getOrderId() == null) {
      throw new ValidationException("Order information is required");
    }
    if (orderItem.getProductId() == null) {
      throw new ValidationException("Product ID is required");
    }
    if (orderItem.getQuantity() == null || orderItem.getQuantity() <= 0) {
      throw new ValidationException("Quantity must be greater than zero");
    }
  }

  private OrderDao getOrderOrThrow(Long orderId) {
    OrderDao order = orderRepo.findOrderById(orderId);
    if (order == null) {
      throw new ResourceNotFoundException("Order not found with id: " + orderId);
    }
    return order;
  }

  private ProductDao getProductOrThrow(Long productId) {
    ProductDao product = productRepo.findProductById(productId);
    if (product == null) {
      throw new ResourceNotFoundException("Product not found with id: " + productId);
    }
    return product;
  }

  private Double calculateDiscountAmount(Long productId, Double originalPrice) {
    ProductDiscountDao productDiscount = productDiscountRepo.findByProductId(productId);
    if (productDiscount == null) {
      return 0.0;
    }
    Double discountAmount = productDiscount.getDiscountAmount();
    if (productDiscount.getDiscountType().getDiscountTypeId() == 2) {
      return originalPrice * (discountAmount / 100);
    }
    return discountAmount;
  }

  private Double calculateDeliveryCost(Double netTotal) {
    List<DeliveryCostDao> dcList = deliveryCostRepo.findAll();
    for (DeliveryCostDao dc : dcList) {
      if (netTotal >= dc.getDelivery_slab_start_range() && netTotal <= dc.getDelivery_slab_end_range()) {
        return dc.getDeliveryCost();
      }
    }
    return 0.0;
  }

  private Double calculateCouponAmount(OrderDao order, Double netTotal) {
    Double couponAmount = order.getCouponAmount();
    if (order.getCouponId() != null) {
      CouponDao couponDetails = couponRepo.findCouponDetailsById(order.getCouponId());
      if (Objects.equals(couponDetails.getDiscountType().getDiscountTypeName(), "Percentage")) {
        couponAmount = (netTotal * couponDetails.getDiscountValue()) / 100;
        if (couponAmount > couponDetails.getDiscountValue()) {
          couponAmount = couponDetails.getDiscountValue();
        }
      }
    } else if (order.getCouponCode() != null) {
      CouponDao couponDetails = couponRepo.findCouponDetailsByCode(order.getCouponCode());
      if (Objects.equals(couponDetails.getDiscountType().getDiscountTypeName(), "Percentage")) {
        couponAmount = (netTotal * couponDetails.getDiscountValue()) / 100;
        if (couponAmount > couponDetails.getDiscountValue()) {
          couponAmount = couponDetails.getDiscountValue();
        }
      }
    }
    return couponAmount;
  }

  private void updateOrderTotals(OrderDao order, Double totalPrice, Double netTotal, Double deliveryCost,
      Double couponAmount) {
    Double totalDiscount = totalPrice - netTotal;
    Double grandTotal = netTotal + deliveryCost - couponAmount;
    order.setTotalPrice(totalPrice);
    order.setNetTotal(netTotal);
    order.setDeliveryCost(deliveryCost);
    order.setDiscountAmount(totalDiscount);
    order.setCouponAmount(couponAmount);
    order.setGrandTotalPrice(grandTotal);
  }

  private void updateProductInventory(ProductDao product, int quantityChange) {
    Integer originalQuantity = product.getQuantity();
    Integer updatedQuantity = originalQuantity + quantityChange;
    productRepo.updateInventory(product.getProductId(), updatedQuantity);
  }
}
