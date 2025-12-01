/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.http.dto.response.OrderResponse;
import com.xplaza.backend.jpa.dao.*;
import com.xplaza.backend.jpa.repository.*;
import com.xplaza.backend.mapper.CouponMapper;
import com.xplaza.backend.mapper.OrderMapper;
import com.xplaza.backend.service.entity.*;

@Service
public class OrderService {
  private final OrderRepository orderRepo;
  private final OrderMapper orderMapper;
  private final ProductRepository productRepo;
  private final ProductDiscountRepository productDiscountRepo;
  private final CouponRepository couponRepo;
  private final DeliveryCostRepository deliveryCostRepo;
  private final EmailSenderService emailSenderService;
  private final CustomerUserRepository customerUserRepo;
  private final AdminUserRepository adminUserRepo;
  private final CurrencyRepository currencyRepo;
  private final Environment env;
  private final CouponMapper couponMapper;

  @Autowired
  public OrderService(OrderRepository orderRepo, OrderMapper orderMapper, ProductRepository productRepo,
      ProductDiscountRepository productDiscountRepo, CouponRepository couponRepo,
      DeliveryCostRepository deliveryCostRepo, EmailSenderService emailSenderService,
      CustomerUserRepository customerUserRepo, AdminUserRepository adminUserRepo, CurrencyRepository currencyRepo,
      Environment env, CouponMapper couponMapper) {
    this.orderRepo = orderRepo;
    this.orderMapper = orderMapper;
    this.productRepo = productRepo;
    this.productDiscountRepo = productDiscountRepo;
    this.couponRepo = couponRepo;
    this.deliveryCostRepo = deliveryCostRepo;
    this.emailSenderService = emailSenderService;
    this.customerUserRepo = customerUserRepo;
    this.adminUserRepo = adminUserRepo;
    this.currencyRepo = currencyRepo;
    this.env = env;
    this.couponMapper = couponMapper;
  }

  public ProductInventory validateProductAvailability(Order order) {
    ProductInventory productInventory = new ProductInventory();
    productInventory.setIsAvailable(true);

    for (OrderItem item : order.getOrderItemList()) {
      ProductDao product = productRepo.findProductById(item.getProductId());
      if (product == null) {
        throw new ResourceNotFoundException("Product not found with id: " + item.getProductId());
      }

      if (product.getQuantity() < item.getQuantity()) {
        productInventory.setProductId(product.getProductId());
        productInventory.setProductName(product.getProductName());
        productInventory.setMaxAvailableQuantity(product.getQuantity());
        productInventory.setIsAvailable(false);
        break;
      }
    }
    return productInventory;
  }

  public Order calculateOrderPrices(Order order) {
    Double totalPrice = 0.0;
    Double totalDiscount = 0.0;
    Double netTotal = 0.0;

    for (OrderItem item : order.getOrderItemList()) {
      ProductDao product = productRepo.findProductById(item.getProductId());
      if (product == null) {
        throw new ResourceNotFoundException("Product not found with id: " + item.getProductId());
      }

      Double originalSellingPrice = product.getProductSellingPrice();
      Double buyingPrice = product.getProductBuyingPrice();
      Double discountAmount = calculateProductDiscount(item.getProductId(), originalSellingPrice);

      Double unitPrice = originalSellingPrice - discountAmount;
      item.setUnitPrice(unitPrice);
      item.setProductSellingPrice(originalSellingPrice);
      item.setProductBuyingPrice(buyingPrice);
      item.setItemTotalPrice(unitPrice * item.getQuantity());
      item.setQuantityType("pc");

      totalPrice += originalSellingPrice * item.getQuantity();
      netTotal += item.getItemTotalPrice();
    }

    totalDiscount = totalPrice - netTotal;
    order.setTotalPrice(totalPrice);
    order.setDiscountAmount(totalDiscount);
    order.setNetTotal(netTotal);

    // Calculate delivery cost
    Double deliveryCost = calculateDeliveryCost(order);
    order.setDeliveryCost(deliveryCost);

    // Calculate coupon discount
    Double couponAmount = calculateCouponDiscount(order);
    order.setCouponAmount(couponAmount);

    // Set final total
    order.setGrandTotalPrice(netTotal + deliveryCost - couponAmount);
    order.setPaymentTypeId(1L);

    return order;
  }

  private Double calculateProductDiscount(Long productId, Double originalPrice) {
    ProductDiscountDao productDiscount = productDiscountRepo.findByProductId(productId);
    if (productDiscount == null) {
      return 0.0;
    }

    Double discountAmount = productDiscount.getDiscountAmount();
    if (productDiscount.getDiscountType().getDiscountTypeId() == 2) { // Percentage discount
      return originalPrice * (discountAmount / 100);
    }
    return discountAmount;
  }

  private Double calculateDeliveryCost(Order order) {
    if (order.getDeliveryCostId() == null) {
      return 0.0;
    }
    DeliveryCostDao dc = deliveryCostRepo.findDeliveryCostById(order.getDeliveryCostId());
    return dc != null ? dc.getDeliveryCost() : 0.0;
  }

  private Double calculateCouponDiscount(Order order) {
    CouponDao couponDetails = null;
    if (order.getCouponId() != null) {
      couponDetails = couponRepo.findCouponDetailsById(order.getCouponId());
    } else if (order.getCouponCode() != null) {
      couponDetails = couponRepo.findCouponDetailsByCode(order.getCouponCode());
    }

    if (couponDetails == null) {
      return 0.0;
    }

    Double couponAmount;
    if (couponDetails.getDiscountType().getDiscountTypeName().equals("Fixed Amount")) {
      couponAmount = couponDetails.getDiscountValue();
    } else {
      couponAmount = (order.getNetTotal() * couponDetails.getDiscountValue()) / 100;
      if (couponAmount > couponDetails.getDiscountValue()) {
        couponAmount = couponDetails.getDiscountValue();
      }
    }
    return couponAmount;
  }

  public boolean validateCoupon(Order order, String type) throws ParseException {
    CouponDao couponDao = type.equals("Code")
        ? couponRepo.findCouponDetailsByCode(order.getCouponCode())
        : couponRepo.findCouponDetailsById(order.getCouponId());

    if (couponDao == null) {
      return false;
    }

    Coupon coupon = couponMapper.toEntityFromDao(couponDao);
    Date receivedTime = order.getReceivedTime();

    // Check validity period
    if (!(receivedTime.compareTo(coupon.getStartDate()) >= 0
        && receivedTime.compareTo(coupon.getEndDate()) <= 0)) {
      return false;
    }

    // Check if coupon is active
    if (!coupon.getIsActive()) {
      return false;
    }

    // Check minimum shopping amount
    if (order.getNetTotal() < coupon.getMinShoppingAmount()) {
      return false;
    }

    // Check if coupon is valid for the shop
    return coupon.getCouponShopLinks().stream()
        .anyMatch(shopLink -> Objects.equals(shopLink.getShop().getShopId(), order.getShopId()));
  }

  @Transactional
  public Order createOrder(Order order) {
    OrderDao dao = orderMapper.toDao(order);
    dao = orderRepo.save(dao);
    return orderMapper.toEntityFromDao(dao);
  }

  @Transactional
  public void updateOrder(Order order) {
    OrderDao dao = orderMapper.toDao(order);
    orderRepo.save(dao);
  }

  @Transactional
  public void updateOrderStatus(Long orderId, String remarks, Long status) {
    orderRepo.updateOrderStatus(orderId, remarks, status);
  }

  @Transactional
  public void deleteOrder(Long id) {
    if (!orderRepo.existsById(id)) {
      throw new ResourceNotFoundException("Order not found with id: " + id);
    }
    orderRepo.deleteById(id);
  }

  public List<OrderList> getAllOrders() {
    return orderRepo.findAllOrdersByAdmin();
  }

  public List<OrderList> getOrdersByStatus(String status) {
    return orderRepo.findAllOrdersByStatusByAdmin(status);
  }

  public List<OrderList> getOrdersByFilter(String status, Date orderDate) {
    return orderRepo.findAllOrdersByFilterByAdmin(status, orderDate);
  }

  public List<OrderList> getOrdersByAdminUser(Long userId) {
    return orderRepo.findAllOrdersAdminUserID(userId);
  }

  public List<OrderList> getOrdersByStatusAndAdminUser(String status, Long userId) {
    return orderRepo.findAllOrdersByStatusAndAdminUserID(status, userId);
  }

  public List<OrderList> getOrdersByFilterAndAdminUser(String status, Date orderDate, Long userId) {
    return orderRepo.findAllOrdersByFilterAndAdminUserID(status, orderDate, userId);
  }

  public void sendOrderConfirmationToCustomer(OrderDetails order, PlatformInfo platformInfo) {
    String currencyName = currencyRepo.getName(order.getCurrencyId());
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy");

    String deliveryDate = dateFormatter.format(order.getDateToDeliver());
    String deliverySchedule = order.getAllottedTime();

    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(2);

    SimpleMailMessage mailMessage = new SimpleMailMessage();
    String email = customerUserRepo.getUsername(order.getCustomerId());
    mailMessage.setFrom(env.getProperty("spring.mail.username"));
    mailMessage.setTo(email);
    mailMessage.setSubject("Your " + platformInfo.getName() + ".com Order.");
    mailMessage.setText(generateCustomerEmailContent(order, platformInfo, currencyName,
        deliveryDate, deliverySchedule, nf));

    emailSenderService.sendEmail(mailMessage);
  }

  public void sendOrderNotificationToShopAdmin(Order order, OrderResponse dto, PlatformInfo platformInfo) {
    String currencyName = currencyRepo.getName(order.getCurrencyId());
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy");
    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");

    String deliveryDate = dateFormatter.format(order.getDateToDeliver());
    String deliverySchedule = timeFormatter.format(order.getDeliveryScheduleStart()) + " - " +
        timeFormatter.format(order.getDeliveryScheduleEnd());

    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(2);

    List<String> emailList = adminUserRepo.getEmailListByShopId(order.getShopId());
    for (String email : emailList) {
      if (email.equals("admin@gmail.com")) {
        continue;
      }

      SimpleMailMessage mailMessage = new SimpleMailMessage();
      mailMessage.setFrom(env.getProperty("spring.mail.username"));
      mailMessage.setTo(email);
      mailMessage.setSubject(platformInfo.getName() + ".com Customer Order.");
      mailMessage.setText(generateShopAdminEmailContent(order, dto, platformInfo,
          currencyName, deliveryDate, deliverySchedule, nf));

      emailSenderService.sendEmail(mailMessage);
    }
  }

  private String generateCustomerEmailContent(OrderDetails order, PlatformInfo platformInfo,
      String currencyName, String deliveryDate, String deliverySchedule, NumberFormat nf) {
    return "Hello " + order.getCustomerName() + ",\n\n" +
        "Thank you for your order. Here are your order details:\n\n" +
        "Order No: " + order.getOrderId() + "\n" +
        "Total Amount: " + nf.format(order.getGrandTotalPrice()) + " " + currencyName + "\n" +
        "Delivery Date: " + deliveryDate + "\n" +
        "Delivery Time: " + deliverySchedule + "\n" +
        "Delivery Address: " + order.getDeliveryAddress() + "\n\n" +
        "Thank you for shopping with us!\n" +
        "Team " + platformInfo.getName();
  }

  private String generateShopAdminEmailContent(Order order, OrderResponse dto, PlatformInfo platformInfo,
      String currencyName, String deliveryDate, String deliverySchedule, NumberFormat nf) {
    return "Hello,\n\n" +
        "The following order has been placed by the customer: " + order.getCustomerName() + ".\n\n" +
        "You can view the order details by visiting Pending Orders on https://admin." +
        platformInfo.getName().toLowerCase() + ".com.\n\n" +
        "Order No: " + dto.getInvoiceNumber() + "\n" +
        "Grand Total: " + nf.format(dto.getGrandTotalPrice()) + " " + currencyName + "\n" +
        "Delivery Date: " + deliveryDate + "\n" +
        "Delivery Schedule: " + deliverySchedule + "\n" +
        "Delivery Address: " + order.getDeliveryAddress() + "\n\n" +
        "With Regards,\n" +
        "Team " + platformInfo.getName();
  }

  public Order getOrderById(Long id) {
    return orderRepo.findById(id)
        .map(orderMapper::toEntityFromDao)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
  }

  // ===== V2 Paginated Methods =====
  // Note: These use in-memory pagination for now since the existing queries are
  // complex native SQL.
  // For production, consider rewriting with Spring Data JPA Specifications for
  // true DB-level pagination.

  public Page<OrderList> getAllOrdersPaginated(Pageable pageable) {
    List<OrderList> allOrders = getAllOrders();
    return paginateList(allOrders, pageable);
  }

  public Page<OrderList> getOrdersByStatusPaginated(String status, Pageable pageable) {
    List<OrderList> orders = getOrdersByStatus(status);
    return paginateList(orders, pageable);
  }

  public Page<OrderList> getOrdersByFilterPaginated(String status, Date orderDate, Pageable pageable) {
    List<OrderList> orders = getOrdersByFilter(status, orderDate);
    return paginateList(orders, pageable);
  }

  public Page<OrderList> getOrdersByAdminUserPaginated(Long userId, Pageable pageable) {
    List<OrderList> orders = getOrdersByAdminUser(userId);
    return paginateList(orders, pageable);
  }

  public Page<OrderList> getOrdersByStatusAndAdminUserPaginated(String status, Long userId, Pageable pageable) {
    List<OrderList> orders = getOrdersByStatusAndAdminUser(status, userId);
    return paginateList(orders, pageable);
  }

  public Page<OrderList> getOrdersByFilterAndAdminUserPaginated(String status, Date orderDate, Long userId,
      Pageable pageable) {
    List<OrderList> orders = getOrdersByFilterAndAdminUser(status, orderDate, userId);
    return paginateList(orders, pageable);
  }

  /**
   * Helper method to paginate a list in memory.
   */
  private <T> Page<T> paginateList(List<T> list, Pageable pageable) {
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), list.size());

    if (start >= list.size()) {
      return new PageImpl<>(List.of(), pageable, list.size());
    }

    List<T> subList = list.subList(start, end);
    return new PageImpl<>(subList, pageable, list.size());
  }
}
