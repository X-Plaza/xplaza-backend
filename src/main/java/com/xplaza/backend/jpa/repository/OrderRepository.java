/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.jpa.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.domain.OrderDetails;
import com.xplaza.backend.domain.OrderList;
import com.xplaza.backend.jpa.dao.OrderDao;

public interface OrderRepository extends JpaRepository<OrderDao, Long> {
  @Modifying
  @Transactional
  @Query(value = "update orders set fk_status_id = ?3, remarks=?2 where order_id = ?1", nativeQuery = true)
  void updateOrderStatus(Long order_id, String remarks, Long status);

  @Query(value = "select o.* from orders o where o.order_id = ?1", nativeQuery = true)
  OrderDao findOrderById(Long order_id);

  /**
   * Find the shop ID for an order. Used for authorization checks.
   */
  @Query(value = "SELECT shop_id FROM orders WHERE order_id = ?1", nativeQuery = true)
  Long findShopIdByOrderId(Long orderId);

  @Query(value = "select o.order_id, o.total_price, o.discount_amount, o.net_total, o.grand_total_price, " +
      "o.delivery_address, o.customer_id, null as invoice_number," +
      "o.date_to_deliver, o.customer_name, " +
      "o.mobile_no, o.received_time, o.shop_id, o.shop_name, o.fk_status_id, st.status_name, " +
      "concat(o.delivery_schedule_start, '-' , o.delivery_schedule_end) as allotted_time, " +
      "o.fk_currency_id, cur.currency_name, cur.currency_sign " +
      "from orders o " +
      "left join currencies cur on cur.currency_id = o.fk_currency_id " +
      "left join status_catalogues st on o.fk_status_id = st.status_id " +
      "order by order_id desc", nativeQuery = true)
  List<OrderList> findAllOrdersByAdmin();

  @Query(value = "select o.order_id, o.total_price, o.discount_amount, o.net_total, o.grand_total_price, o.delivery_address, o.customer_id, "
      +
      "o.date_to_deliver, o.customer_name, null as invoice_number," +
      "o.mobile_no, o.received_time, o.shop_id, o.shop_name, o.fk_status_id, st.status_name, " +
      "concat(o.delivery_schedule_start, '-' , o.delivery_schedule_end) as allotted_time, " +
      "o.fk_currency_id, cur.currency_name, cur.currency_sign " +
      "from orders o " +
      "left join currencies cur on cur.currency_id = o.fk_currency_id " +
      "left join status_catalogues st on o.fk_status_id = st.status_id " +
      "where st.status_name= ?1 order by order_id desc", nativeQuery = true)
  List<OrderList> findAllOrdersByStatusByAdmin(String status);

  @Query(value = "select o.order_id, o.total_price, o.discount_amount, o.net_total, o.grand_total_price, o.delivery_address, o.customer_id, "
      +
      "o.date_to_deliver, o.customer_name, null as invoice_number," +
      "o.mobile_no, o.received_time, o.shop_id, o.shop_name, o.fk_status_id, st.status_name, " +
      "concat(o.delivery_schedule_start, '-' , o.delivery_schedule_end) as allotted_time, " +
      "o.fk_currency_id, cur.currency_name, cur.currency_sign " +
      "from orders o " +
      "left join currencies cur on cur.currency_id = o.fk_currency_id " +
      "left join status_catalogues st on o.fk_status_id = st.status_id " +
      "where st.status_name= ?1 and date(o.received_time) = ?2 order by order_id desc", nativeQuery = true)
  List<OrderList> findAllOrdersByFilterByAdmin(String status, Date order_date);

  @Query(value = "select o.order_id, o.total_price, o.discount_amount, o.net_total, o.grand_total_price, o.delivery_address, o.customer_id, "
      +
      "o.date_to_deliver, o.customer_name, null as invoice_number," +
      "o.mobile_no, o.received_time, o.shop_id, o.shop_name, o.fk_status_id, st.status_name, " +
      "concat(o.delivery_schedule_start, '-' , o.delivery_schedule_end) as allotted_time, " +
      "o.fk_currency_id, cur.currency_name, cur.currency_sign " +
      "from orders o " +
      "left join currencies cur on cur.currency_id = o.fk_currency_id " +
      "left join status_catalogues st on o.fk_status_id = st.status_id " +
      "left join admin_user_shop_link ausl on ausl.shop_id = o.shop_id " +
      "where ausl.admin_user_id = ?1 order by order_id desc", nativeQuery = true)
  List<OrderList> findAllOrdersAdminUserID(Long user_id);

  @Query(value = "select o.order_id, o.total_price, o.discount_amount, o.net_total, o.grand_total_price, o.delivery_address, o.customer_id, "
      +
      "o.date_to_deliver, o.customer_name, null as invoice_number," +
      "o.mobile_no, o.received_time, o.shop_id, o.shop_name, o.fk_status_id, st.status_name, " +
      "concat(o.delivery_schedule_start, '-' , o.delivery_schedule_end) as allotted_time, " +
      "o.fk_currency_id, cur.currency_name, cur.currency_sign " +
      "from orders o " +
      "left join currencies cur on cur.currency_id = o.fk_currency_id " +
      "left join status_catalogues st on o.fk_status_id = st.status_id " +
      "left join admin_user_shop_link ausl on ausl.shop_id = o.shop_id " +
      "where st.status_name= ?1 and ausl.admin_user_id = ?2 order by order_id desc", nativeQuery = true)
  List<OrderList> findAllOrdersByStatusAndAdminUserID(String status, Long user_id);

  @Query(value = "select o.order_id, o.total_price, o.discount_amount, o.net_total, o.grand_total_price, o.delivery_address, o.customer_id, "
      +
      "o.date_to_deliver, o.customer_name, null as invoice_number," +
      "o.mobile_no, o.received_time, o.shop_id, o.shop_name, o.fk_status_id, st.status_name, " +
      "concat(o.delivery_schedule_start, '-' , o.delivery_schedule_end) as allotted_time, " +
      "o.fk_currency_id, cur.currency_name, cur.currency_sign " +
      "from orders o " +
      "left join currencies cur on cur.currency_id = o.fk_currency_id " +
      "left join status_catalogues st on o.fk_status_id = st.status_id " +
      "left join admin_user_shop_link ausl on ausl.shop_id = o.shop_id " +
      "where st.status_name= ?1 and  date(o.received_time) = ?2 and ausl.admin_user_id = ?3 order by order_id desc", nativeQuery = true)
  List<OrderList> findAllOrdersByFilterAndAdminUserID(String status, Date order_date, Long user_id);

  @Query(value = "select od.*, null as invoice_number from order_details od " +
      "where od.order_id = ?1", nativeQuery = true)
  OrderDetails findOrderDetails(Long id);
}
