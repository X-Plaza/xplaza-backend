# V2 API Refactoring Strategy

## Overview

This document outlines the V2 API design for the Xplaza e-commerce backend. The V2 API is designed as a clean, API-only product with modern REST best practices.

## Key Design Principles

### 1. Clean Response Structure (`ApiResponseV2<T>`)

All V2 endpoints use the `ApiResponseV2` wrapper:

```json
{
  "success": true,
  "data": { ... },
  "meta": {
    "pagination": {
      "page": 0,
      "size": 20,
      "totalElements": 100,
      "totalPages": 5,
      "hasNext": true,
      "hasPrevious": false
    },
    "message": "Optional message"
  }
}
```

Error responses:
```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "Resource not found",
    "details": null,
    "timestamp": "2025-01-01T12:00:00Z"
  }
}
```

### 2. Unified Endpoints with Query Parameters

Instead of separate endpoints for filtering, V2 uses query parameters:

```
GET /api/v2/products?shopId=1&categoryId=2&search=phone&page=0&size=20&sort=productId&direction=DESC
```

### 3. Consistent Pagination

All list endpoints support pagination with:
- `page` - Page number (0-indexed)
- `size` - Page size (default: 20, max: 100)
- `sort` - Sort field
- `direction` - ASC or DESC

### 4. Centralized Exception Handling

`GlobalExceptionHandler` provides consistent error responses:
- `ResourceNotFoundException` → 404
- `ValidationException` → 400  
- `AuthenticationException` → 401
- Generic exceptions → 500

### 5. Let Spring Handle Serialization

No manual `ObjectMapper.writeValueAsString()` - Spring automatically serializes response objects.

---

## V2 Controllers Implemented

| Controller | Base Path | Description |
|------------|-----------|-------------|
| `AdminUserControllerV2` | `/api/v2/admin-users` | Admin user CRUD + password change |
| `AuthControllerV2` | `/api/v2/auth` | Unified authentication (admin + customer login, password reset) |
| `BrandControllerV2` | `/api/v2/brands` | Brand CRUD with pagination and search |
| `CategoryControllerV2` | `/api/v2/categories` | Category CRUD with parent filtering |
| `CityControllerV2` | `/api/v2/cities` | City CRUD with state filtering |
| `CountryControllerV2` | `/api/v2/countries` | Country reference data |
| `CouponControllerV2` | `/api/v2/coupons` | Coupon CRUD + validation endpoint |
| `CurrencyControllerV2` | `/api/v2/currencies` | Currency CRUD |
| `CustomerControllerV2` | `/api/v2/customers` | Customer profile + password change |
| `DashboardControllerV2` | `/api/v2/dashboard` | Dashboard analytics (revenue, monthly stats) |
| `DayControllerV2` | `/api/v2/days` | Day reference data (read-only) |
| `DeliveryCostControllerV2` | `/api/v2/delivery-costs` | Delivery cost CRUD |
| `DeliveryScheduleControllerV2` | `/api/v2/delivery-schedules` | Delivery schedule CRUD |
| `DiscountTypeControllerV2` | `/api/v2/discount-types` | Discount type reference data (read-only) |
| `LocationControllerV2` | `/api/v2/locations` | Location CRUD |
| `ModuleControllerV2` | `/api/v2/modules` | Module CRUD (permissions) |
| `OrderControllerV2` | `/api/v2/orders` | Orders with status, date, shop filtering |
| `OrderItemControllerV2` | `/api/v2/order-items` | Order item CRUD |
| `PlatformInfoControllerV2` | `/api/v2/platform-info` | Platform configuration |
| `ProductControllerV2` | `/api/v2/products` | Products with multi-filter support + inventory updates |
| `ProductDiscountControllerV2` | `/api/v2/product-discounts` | Product discount CRUD with validation |
| `ProductImageControllerV2` | `/api/v2/product-images` | Product image CRUD |
| `ProductVariationTypeControllerV2` | `/api/v2/product-variation-types` | Product variation type CRUD |
| `RoleControllerV2` | `/api/v2/roles` | Role CRUD (excludes Master Admin) |
| `ShopControllerV2` | `/api/v2/shops` | Shops with location and owner filtering |
| `StateControllerV2` | `/api/v2/states` | State CRUD with country filtering |

---

## API Endpoint Reference

### Authentication (`/api/v2/auth`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/admin/login` | Admin user login |
| POST | `/customer/login` | Customer login |
| POST | `/password-reset/request` | Request OTP |
| POST | `/password-reset/verify` | Verify OTP |
| POST | `/password-reset/complete` | Set new password |

### Products (`/api/v2/products`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | List products (supports shopId, categoryId, brandId, search filters) |
| GET | `/{id}` | Get single product |
| POST | `/` | Create product |
| PUT | `/{id}` | Update product |
| DELETE | `/{id}` | Delete product |
| PATCH | `/{id}/inventory` | Update inventory quantity |

### Orders (`/api/v2/orders`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | List orders (supports status, shopId, customerId, adminUserId, orderDate) |
| GET | `/{id}` | Get order details |
| POST | `/` | Create order |
| PUT | `/{id}` | Update order |
| PATCH | `/{id}/status` | Update order status only |
| DELETE | `/{id}` | Delete order |

### Coupons (`/api/v2/coupons`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | List coupons (supports shopId, active filters) |
| GET | `/{id}` | Get coupon |
| POST | `/` | Create coupon |
| PUT | `/{id}` | Update coupon |
| DELETE | `/{id}` | Delete coupon |
| POST | `/validate` | Validate coupon code and get discount |

---

## Migration Notes

### Coexistence Strategy

V1 and V2 endpoints can run simultaneously:
- V1: `/api/v1/*` - Legacy endpoints (deprecated)
- V2: `/api/v2/*` - New clean API

### Breaking Changes in V2

1. **Response structure changed** - Use `ApiResponseV2` instead of `ApiResponse`
2. **No `data` as JSON string** - Data is properly typed, not a serialized string
3. **Pagination required** for list endpoints
4. **Query parameters** instead of separate endpoints
5. **Records for DTOs** where appropriate

### Files Added/Modified

**New V2 Files:**
- `ApiResponseV2.java` - Clean response wrapper
- `GlobalExceptionHandler.java` - Centralized exception handling  
- `AuthenticationException.java` - Custom auth exception
- 9 V2 Controllers (see table above)

**Repository Changes:**
- Added pagination support via Spring Data `Pageable`
- Added search methods with `ContainingIgnoreCase`

**Service Changes:**
- Added paginated versions of list methods
- In-memory pagination for complex queries (orders)

---

## Future Improvements

1. **Database-level pagination** for Orders (replace in-memory)
2. **OpenAPI/Swagger annotations** on all V2 endpoints
3. **Rate limiting** per endpoint
4. **API versioning header** support (Accept-Version)
5. **HATEOAS links** for resource relationships
6. **GraphQL endpoint** for complex queries
