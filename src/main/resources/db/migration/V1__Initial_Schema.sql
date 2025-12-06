-- =====================================================
-- X-Plaza Enterprise E-Commerce Schema
-- Version: 1.0.0
-- Date: 2025-12-06
-- Description: Initial Schema
-- =====================================================

-- Create and switch to xplaza schema
CREATE SCHEMA IF NOT EXISTS xplaza;
SET search_path TO xplaza;

-- =====================================================
-- SEQUENCES
-- =====================================================

CREATE SEQUENCE IF NOT EXISTS order_number_seq START WITH 1 INCREMENT BY 1;

-- =====================================================
-- CATALOG CONTEXT
-- =====================================================

-- Categories
CREATE TABLE IF NOT EXISTS categories (
    category_id BIGSERIAL PRIMARY KEY,
    category_name VARCHAR(255),
    category_description VARCHAR(255),
    parent_category BIGINT REFERENCES categories(category_id)
);

-- Brands
CREATE TABLE IF NOT EXISTS brands (
    brand_id BIGSERIAL PRIMARY KEY,
    brand_name VARCHAR(255),
    brand_description VARCHAR(255)
);

-- Shops
CREATE TABLE IF NOT EXISTS shops (
    shop_id BIGSERIAL PRIMARY KEY,
    shop_name VARCHAR(255),
    shop_description VARCHAR(255),
    shop_address VARCHAR(255),
    shop_owner VARCHAR(255),
    fk_location_id BIGINT
);

-- Currencies
CREATE TABLE IF NOT EXISTS currencies (
    currency_id BIGSERIAL PRIMARY KEY,
    currency_name VARCHAR(255),
    currency_sign VARCHAR(255)
);

-- Product Variation Types
CREATE TABLE IF NOT EXISTS product_variation_types (
    product_var_type_id BIGSERIAL PRIMARY KEY,
    var_type_name VARCHAR(255),
    var_type_description VARCHAR(255)
);

-- Products
CREATE TABLE IF NOT EXISTS products (
    product_id BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(255),
    product_description VARCHAR(255),
    product_buying_price DOUBLE PRECISION,
    product_selling_price DOUBLE PRECISION,
    quantity INTEGER,
    product_var_type_value INTEGER,
    created_at TIMESTAMP,
    created_by INTEGER,
    last_updated_at TIMESTAMP,
    last_updated_by INTEGER,
    fk_brand_id BIGINT REFERENCES brands(brand_id),
    fk_category_id BIGINT REFERENCES categories(category_id),
    fk_currency_id BIGINT REFERENCES currencies(currency_id),
    fk_product_var_type_id BIGINT REFERENCES product_variation_types(product_var_type_id),
    fk_shop_id BIGINT REFERENCES shops(shop_id)
);

-- Product Attributes (defines what attributes exist)
CREATE TABLE IF NOT EXISTS attributes (
    attribute_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL DEFAULT 'SELECT', -- SELECT, MULTI_SELECT, TEXT, NUMBER, BOOLEAN
    is_variant_attribute BOOLEAN DEFAULT FALSE,
    is_filterable BOOLEAN DEFAULT TRUE,
    is_searchable BOOLEAN DEFAULT TRUE,
    position INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Attribute Values (possible values for each attribute)
CREATE TABLE IF NOT EXISTS attribute_values (
    value_id BIGSERIAL PRIMARY KEY,
    attribute_id BIGINT NOT NULL REFERENCES attributes(attribute_id) ON DELETE CASCADE,
    display_value VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    metadata TEXT, -- for hex colors, icons, etc.
    position INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(attribute_id, code)
);

-- Category-Attribute link (which attributes apply to which categories)
CREATE TABLE IF NOT EXISTS category_attributes (
    category_id BIGINT NOT NULL REFERENCES categories(category_id) ON DELETE CASCADE,
    attribute_id BIGINT NOT NULL REFERENCES attributes(attribute_id) ON DELETE CASCADE,
    is_required BOOLEAN DEFAULT FALSE,
    position INTEGER DEFAULT 0,
    PRIMARY KEY (category_id, attribute_id)
);

-- Enhanced Products table (add new columns)
ALTER TABLE products ADD COLUMN IF NOT EXISTS slug VARCHAR(255);
ALTER TABLE products ADD COLUMN IF NOT EXISTS short_description TEXT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'DRAFT';
ALTER TABLE products ADD COLUMN IF NOT EXISTS visibility VARCHAR(20) DEFAULT 'VISIBLE';
ALTER TABLE products ADD COLUMN IF NOT EXISTS seo_title VARCHAR(255);
ALTER TABLE products ADD COLUMN IF NOT EXISTS seo_description TEXT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS seo_keywords TEXT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS metadata TEXT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS published_at TIMESTAMP;

-- Product Variants (the purchasable items)
CREATE TABLE IF NOT EXISTS product_variants (
    variant_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255),
    price DECIMAL(15, 2) NOT NULL,
    compare_at_price DECIMAL(15, 2), -- original/MSRP price
    cost_price DECIMAL(15, 2),
    currency_id BIGINT REFERENCES currencies(currency_id),
    barcode VARCHAR(50), -- UPC/EAN
    weight_grams DECIMAL(10, 2),
    length_cm DECIMAL(10, 2),
    width_cm DECIMAL(10, 2),
    height_cm DECIMAL(10, 2),
    is_default BOOLEAN DEFAULT FALSE,
    position INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, DISCONTINUED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Variant Attributes (links variants to their attribute values)
CREATE TABLE IF NOT EXISTS variant_attributes (
    variant_id UUID NOT NULL REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    attribute_id BIGINT NOT NULL REFERENCES attributes(attribute_id),
    value_id BIGINT NOT NULL REFERENCES attribute_values(value_id),
    PRIMARY KEY (variant_id, attribute_id)
);

-- Variant Images (images specific to variants, e.g., color-specific images)
CREATE TABLE IF NOT EXISTS variant_images (
    image_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    variant_id UUID NOT NULL REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    position INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Product Attributes (non-variant attributes like material, features)
CREATE TABLE IF NOT EXISTS product_attributes (
    product_id BIGINT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    attribute_id BIGINT NOT NULL REFERENCES attributes(attribute_id),
    value_id BIGINT REFERENCES attribute_values(value_id),
    text_value VARCHAR(500), -- for TEXT type attributes
    PRIMARY KEY (product_id, attribute_id)
);

-- Product Tags
CREATE TABLE IF NOT EXISTS product_tags (
    product_id BIGINT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (product_id, tag)
);

-- =====================================================
-- CUSTOMER CONTEXT
-- =====================================================


-- Admin Users
CREATE TABLE IF NOT EXISTS admin_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    last_login_at TIMESTAMP
);

-- Customers
CREATE TABLE IF NOT EXISTS customers (
    customer_id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    role VARCHAR(255) NOT NULL DEFAULT 'CUSTOMER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    last_login_at TIMESTAMP
);

-- Customer Addresses (multiple addresses per customer)
CREATE TABLE IF NOT EXISTS customer_addresses (
    address_id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    type VARCHAR(20) DEFAULT 'BOTH', -- SHIPPING, BILLING, BOTH
    is_default BOOLEAN DEFAULT FALSE,
    label VARCHAR(50), -- Home, Office, etc.
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    company VARCHAR(255),
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20) NOT NULL,
    country_code VARCHAR(2) NOT NULL, -- ISO 3166-1 alpha-2
    phone VARCHAR(20),
    instructions TEXT, -- delivery instructions
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customer enhanced columns
ALTER TABLE customers ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';
ALTER TABLE customers ADD COLUMN IF NOT EXISTS display_name VARCHAR(255);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS avatar VARCHAR(500);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS gender VARCHAR(20);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS locale VARCHAR(10) DEFAULT 'en-US';
ALTER TABLE customers ADD COLUMN IF NOT EXISTS verified_email BOOLEAN DEFAULT FALSE;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS verified_phone BOOLEAN DEFAULT FALSE;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS mfa_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER DEFAULT 0;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS last_order_at TIMESTAMP;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS marketing_email BOOLEAN DEFAULT TRUE;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS marketing_sms BOOLEAN DEFAULT FALSE;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS loyalty_points INTEGER DEFAULT 0;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS loyalty_tier VARCHAR(20) DEFAULT 'BRONZE';
ALTER TABLE customers ADD COLUMN IF NOT EXISTS lifetime_spend DECIMAL(15, 2) DEFAULT 0;

-- Saved Payment Methods
CREATE TABLE IF NOT EXISTS customer_payment_methods (
    payment_method_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL, -- CARD, PAYPAL, BANK_ACCOUNT, WALLET
    is_default BOOLEAN DEFAULT FALSE,
    nickname VARCHAR(50),
    last4 VARCHAR(4),
    brand VARCHAR(20), -- visa, mastercard, amex
    expiry_month INTEGER,
    expiry_year INTEGER,
    cardholder_name VARCHAR(255),
    gateway VARCHAR(50), -- stripe, braintree
    gateway_token VARCHAR(255), -- tokenized reference
    billing_address_id BIGINT REFERENCES customer_addresses(address_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Wishlists
CREATE TABLE IF NOT EXISTS wishlists (
    wishlist_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    name VARCHAR(100) DEFAULT 'My Wishlist',
    visibility VARCHAR(20) DEFAULT 'PRIVATE', -- PRIVATE, SHARED, PUBLIC
    share_token VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Wishlist Items
CREATE TABLE IF NOT EXISTS wishlist_items (
    wishlist_item_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    wishlist_id UUID NOT NULL REFERENCES wishlists(wishlist_id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id UUID REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    note TEXT,
    price_at_add DECIMAL(15, 2),
    notify_price_drop BOOLEAN DEFAULT FALSE,
    notify_back_in_stock BOOLEAN DEFAULT FALSE,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(wishlist_id, product_id, variant_id)
);

-- =====================================================
-- PROMOTION CONTEXT
-- =====================================================

-- Discount Types
CREATE TABLE IF NOT EXISTS discount_types (
    discount_type_id BIGSERIAL PRIMARY KEY,
    discount_type_name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_by INTEGER,
    created_at TIMESTAMP,
    last_updated_by INTEGER,
    last_updated_at TIMESTAMP
);

-- Coupons
CREATE TABLE IF NOT EXISTS coupons (
    coupon_id BIGSERIAL PRIMARY KEY,
    coupon_code VARCHAR(255) NOT NULL UNIQUE,
    coupon_description VARCHAR(255),
    fk_discount_type_id BIGINT REFERENCES discount_types(discount_type_id),
    discount_value DOUBLE PRECISION,
    minimum_order_amount DOUBLE PRECISION,
    maximum_discount_amount DOUBLE PRECISION,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    usage_limit INTEGER DEFAULT 0,
    used_count INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_by INTEGER,
    created_at TIMESTAMP,
    last_updated_by INTEGER,
    last_updated_at TIMESTAMP
);

-- =====================================================
-- CART CONTEXT
-- =====================================================

-- Shopping Carts (proper implementation)
CREATE TABLE IF NOT EXISTS carts (
    cart_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    customer_id BIGINT REFERENCES customers(customer_id) ON DELETE SET NULL,
    session_id VARCHAR(100), -- for guest carts
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, MERGED, CONVERTED, ABANDONED
    currency VARCHAR(3) DEFAULT 'USD',
    subtotal DECIMAL(15, 2) DEFAULT 0,
    discount_total DECIMAL(15, 2) DEFAULT 0,
    shipping_estimate DECIMAL(15, 2) DEFAULT 0,
    tax_estimate DECIMAL(15, 2) DEFAULT 0,
    total_estimate DECIMAL(15, 2) DEFAULT 0,
    item_count INTEGER DEFAULT 0,
    converted_order_id UUID,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cart Items
CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES carts(cart_id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(product_id),
    variant_id UUID REFERENCES product_variants(variant_id),
    shop_id BIGINT NOT NULL REFERENCES shops(shop_id),
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(15, 2) NOT NULL,
    price_at_add DECIMAL(15, 2) NOT NULL, -- for price change detection
    discount_amount DECIMAL(15, 2) DEFAULT 0,
    total_price DECIMAL(15, 2) NOT NULL,
    saved_for_later BOOLEAN DEFAULT FALSE,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(cart_id, variant_id)
);

-- Cart Applied Coupons
CREATE TABLE IF NOT EXISTS cart_coupons (
    cart_id UUID NOT NULL REFERENCES carts(cart_id) ON DELETE CASCADE,
    coupon_id BIGINT NOT NULL REFERENCES coupons(coupon_id),
    code VARCHAR(50) NOT NULL,
    discount_amount DECIMAL(15, 2) DEFAULT 0,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (cart_id, coupon_id)
);


-- =====================================================
-- ORDER CONTEXT
-- =====================================================

-- Customer Orders
CREATE TABLE IF NOT EXISTS customer_orders (
    order_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(customer_id),
    shop_id BIGINT NOT NULL REFERENCES shops(shop_id),
    cart_id UUID,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    subtotal DECIMAL(15, 2) NOT NULL,
    discount_amount DECIMAL(15, 2) DEFAULT 0,
    shipping_cost DECIMAL(15, 2) DEFAULT 0,
    tax_amount DECIMAL(15, 2) DEFAULT 0,
    grand_total DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    shipping_address_id BIGINT,
    shipping_first_name VARCHAR(100),
    shipping_last_name VARCHAR(100),
    shipping_phone VARCHAR(20),
    shipping_address_line1 VARCHAR(255),
    shipping_address_line2 VARCHAR(255),
    shipping_city VARCHAR(100),
    shipping_state VARCHAR(100),
    shipping_postal_code VARCHAR(20),
    shipping_country_code VARCHAR(2),
    shipping_instructions TEXT,
    billing_address_id BIGINT,
    billing_same_as_shipping BOOLEAN DEFAULT TRUE,
    billing_first_name VARCHAR(100),
    billing_last_name VARCHAR(100),
    billing_company VARCHAR(255),
    billing_address_line1 VARCHAR(255),
    billing_address_line2 VARCHAR(255),
    billing_city VARCHAR(100),
    billing_state VARCHAR(100),
    billing_postal_code VARCHAR(20),
    billing_country VARCHAR(2),
    billing_phone VARCHAR(20),
    requested_delivery_date DATE,
    delivery_slot_start TIME,
    delivery_slot_end TIME,
    estimated_delivery_date DATE,
    actual_delivery_date DATE,
    payment_type_id BIGINT,
    payment_method VARCHAR(50),
    payment_status VARCHAR(30),
    payment_transaction_id UUID,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_notes TEXT,
    internal_notes TEXT,
    cancellation_reason VARCHAR(255),
    placed_at TIMESTAMP,
    confirmed_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tax_total DECIMAL(15, 2),
    shipping_total DECIMAL(15, 2),
    paid_amount DECIMAL(15, 2) DEFAULT 0,
    source VARCHAR(20) DEFAULT 'WEB',
    ip_address VARCHAR(45),
    user_agent TEXT,
    coupon_code VARCHAR(50),
    coupon_id BIGINT,
    coupon_discount_amount DECIMAL(15, 2)
);

-- Customer Order Items
CREATE TABLE IF NOT EXISTS customer_order_items (
    order_item_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES customer_orders(order_id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(product_id),
    variant_id UUID REFERENCES product_variants(variant_id),
    shop_id BIGINT NOT NULL REFERENCES shops(shop_id),
    sku VARCHAR(100),
    product_name VARCHAR(255) NOT NULL,
    variant_name VARCHAR(255),
    product_image_url VARCHAR(500),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(15, 2) NOT NULL,
    cost_price DECIMAL(15, 2),
    total_price DECIMAL(15, 2) NOT NULL,
    discount_amount DECIMAL(15, 2) DEFAULT 0,
    tax_amount DECIMAL(15, 2) DEFAULT 0,
    status VARCHAR(30) DEFAULT 'PENDING',
    fulfillment_status VARCHAR(30) DEFAULT 'PENDING',
    quantity_shipped INTEGER DEFAULT 0,
    quantity_returned INTEGER DEFAULT 0,
    quantity_refunded INTEGER DEFAULT 0,
    notes VARCHAR(500),
    metadata TEXT,
    category_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- REVIEW CONTEXT
-- =====================================================

-- Product Reviews
CREATE TABLE IF NOT EXISTS reviews (
    review_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id UUID REFERENCES product_variants(variant_id),
    order_id UUID REFERENCES customer_orders(order_id),
    order_item_id UUID, -- reference to the specific order item
    customer_id BIGINT NOT NULL REFERENCES customers(customer_id),
    shop_id BIGINT NOT NULL REFERENCES shops(shop_id),
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, FLAGGED
    
    -- Ratings
    rating_overall INTEGER NOT NULL CHECK (rating_overall BETWEEN 1 AND 5),
    rating_quality INTEGER CHECK (rating_quality BETWEEN 1 AND 5),
    rating_value INTEGER CHECK (rating_value BETWEEN 1 AND 5),
    rating_shipping INTEGER CHECK (rating_shipping BETWEEN 1 AND 5),
    
    -- Content
    title VARCHAR(255),
    body TEXT,
    pros VARCHAR ARRAY, -- array of pros
    cons VARCHAR ARRAY, -- array of cons
    
    -- Votes
    helpful_votes INTEGER DEFAULT 0,
    unhelpful_votes INTEGER DEFAULT 0,
    
    -- Verification
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    is_anonymous BOOLEAN DEFAULT FALSE,
    
    -- Moderation
    moderated_by BIGINT REFERENCES admin_users(id),
    moderated_at TIMESTAMP,
    reject_reason TEXT,
    flag_reason TEXT,
    
    -- Timestamps
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Review Images
CREATE TABLE IF NOT EXISTS review_images (
    image_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    review_id UUID NOT NULL REFERENCES reviews(review_id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    position INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Review Videos
CREATE TABLE IF NOT EXISTS review_videos (
    video_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    review_id UUID NOT NULL REFERENCES reviews(review_id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    duration_seconds INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Vendor Responses to Reviews
CREATE TABLE IF NOT EXISTS review_responses (
    response_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    review_id UUID NOT NULL UNIQUE REFERENCES reviews(review_id) ON DELETE CASCADE,
    body TEXT NOT NULL,
    responded_by BIGINT NOT NULL REFERENCES admin_users(id),
    responded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Review Votes (helpful/unhelpful tracking)
CREATE TABLE IF NOT EXISTS review_votes (
    vote_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    review_id UUID NOT NULL REFERENCES reviews(review_id) ON DELETE CASCADE,
    customer_id BIGINT NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    is_helpful BOOLEAN NOT NULL,
    voted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(review_id, customer_id)
);

-- Product Rating Summary (denormalized for performance)
CREATE TABLE IF NOT EXISTS product_ratings (
    product_id BIGINT PRIMARY KEY REFERENCES products(product_id) ON DELETE CASCADE,
    average_rating DECIMAL(3, 2) DEFAULT 0,
    total_reviews INTEGER DEFAULT 0,
    rating_1_count INTEGER DEFAULT 0,
    rating_2_count INTEGER DEFAULT 0,
    rating_3_count INTEGER DEFAULT 0,
    rating_4_count INTEGER DEFAULT 0,
    rating_5_count INTEGER DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Shop Rating Summary
CREATE TABLE IF NOT EXISTS shop_ratings (
    shop_id BIGINT PRIMARY KEY REFERENCES shops(shop_id) ON DELETE CASCADE,
    average_rating DECIMAL(3, 2) DEFAULT 0,
    total_reviews INTEGER DEFAULT 0,
    rating_1_count INTEGER DEFAULT 0,
    rating_2_count INTEGER DEFAULT 0,
    rating_3_count INTEGER DEFAULT 0,
    rating_4_count INTEGER DEFAULT 0,
    rating_5_count INTEGER DEFAULT 0,
    response_rate DECIMAL(5, 2) DEFAULT 0, -- percentage of reviews responded to
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- PAYMENT CONTEXT
-- =====================================================

-- Payment Transactions
CREATE TABLE IF NOT EXISTS payment_transactions (
    transaction_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    order_id UUID REFERENCES customer_orders(order_id),
    customer_id BIGINT REFERENCES customers(customer_id),
    type VARCHAR(20) NOT NULL, -- AUTHORIZATION, CAPTURE, SALE, REFUND, VOID
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, SUCCESS, FAILED, CANCELLED
    
    -- Amount
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    amount_in_cents BIGINT NOT NULL,
    
    -- Gateway Info
    gateway VARCHAR(50) NOT NULL, -- stripe, paypal, braintree
    gateway_transaction_id VARCHAR(255),
    authorization_code VARCHAR(100),
    response_code VARCHAR(50),
    response_message TEXT,
    
    -- Payment Method
    payment_method_type VARCHAR(20), -- CARD, PAYPAL, BANK_TRANSFER, WALLET
    card_brand VARCHAR(20),
    card_last4 VARCHAR(4),
    card_expiry_month INTEGER,
    card_expiry_year INTEGER,
    cardholder_name VARCHAR(255),
    
    -- Billing Address
    billing_address_line1 VARCHAR(255),
    billing_address_line2 VARCHAR(255),
    billing_city VARCHAR(100),
    billing_state VARCHAR(100),
    billing_postal_code VARCHAR(20),
    billing_country VARCHAR(2),
    
    -- Risk Assessment
    risk_score INTEGER,
    risk_level VARCHAR(20), -- LOW, MEDIUM, HIGH
    risk_factors VARCHAR ARRAY,
    
    -- Metadata
    ip_address VARCHAR(45),
    user_agent TEXT,
    device_fingerprint VARCHAR(255),
    metadata TEXT,
    
    -- References
    parent_transaction_id UUID REFERENCES payment_transactions(transaction_id),
    
    -- Timestamps
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Refunds
CREATE TABLE IF NOT EXISTS refunds (
    refund_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES customer_orders(order_id),
    transaction_id UUID REFERENCES payment_transactions(transaction_id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, PROCESSING, COMPLETED, REJECTED
    type VARCHAR(20) NOT NULL DEFAULT 'FULL', -- FULL, PARTIAL, EXCHANGE_CREDIT
    
    -- Amounts
    items_amount DECIMAL(15, 2) DEFAULT 0,
    shipping_amount DECIMAL(15, 2) DEFAULT 0,
    tax_amount DECIMAL(15, 2) DEFAULT 0,
    total_amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    
    -- Reason
    reason VARCHAR(50) NOT NULL, -- DAMAGED, WRONG_ITEM, NOT_AS_DESCRIBED, CHANGED_MIND, etc.
    reason_detail TEXT,
    internal_note TEXT,
    
    -- Actors
    requested_by BIGINT NOT NULL, -- customer or admin
    requested_by_type VARCHAR(20) NOT NULL, -- CUSTOMER, ADMIN
    approved_by BIGINT REFERENCES admin_users(id),
    
    -- Gateway
    gateway_refund_id VARCHAR(255),
    
    -- Timestamps
    approved_at TIMESTAMP,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Refund Items
CREATE TABLE IF NOT EXISTS refund_items (
    refund_item_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    refund_id UUID NOT NULL REFERENCES refunds(refund_id) ON DELETE CASCADE,
    order_item_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    reason VARCHAR(50),
    amount DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- FULFILLMENT CONTEXT
-- =====================================================

-- Carriers
CREATE TABLE IF NOT EXISTS carriers (
    carrier_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    tracking_url_template VARCHAR(500), -- e.g., https://dhl.com/track?id={tracking_number}
    logo_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Shipments
CREATE TABLE IF NOT EXISTS shipments (
    shipment_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES customer_orders(order_id),
    shop_id BIGINT NOT NULL REFERENCES shops(shop_id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    -- PENDING, LABEL_CREATED, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, EXCEPTION, RETURNED
    
    -- Carrier Info
    carrier_id BIGINT REFERENCES carriers(carrier_id),
    carrier_name VARCHAR(100),
    service_name VARCHAR(100), -- Express, Ground, etc.
    tracking_number VARCHAR(100),
    tracking_url VARCHAR(500),
    label_url VARCHAR(500),
    
    -- Shipping Cost
    shipping_cost DECIMAL(15, 2),
    currency VARCHAR(3),
    
    -- Package Info
    weight_grams DECIMAL(10, 2),
    length_cm DECIMAL(10, 2),
    width_cm DECIMAL(10, 2),
    height_cm DECIMAL(10, 2),
    
    -- Destination
    ship_to_name VARCHAR(200),
    ship_to_address_line1 VARCHAR(255),
    ship_to_address_line2 VARCHAR(255),
    ship_to_city VARCHAR(100),
    ship_to_state VARCHAR(100),
    ship_to_postal_code VARCHAR(20),
    ship_to_country VARCHAR(2),
    ship_to_phone VARCHAR(20),
    
    -- Dates
    estimated_delivery_date DATE,
    actual_delivery_date DATE,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Shipment Items
CREATE TABLE IF NOT EXISTS shipment_items (
    shipment_item_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    shipment_id UUID NOT NULL REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    order_item_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Shipment Tracking Events
CREATE TABLE IF NOT EXISTS shipment_tracking_events (
    event_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    shipment_id UUID NOT NULL REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(2),
    postal_code VARCHAR(20),
    occurred_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Returns
CREATE TABLE IF NOT EXISTS returns (
    return_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES customer_orders(order_id),
    shop_id BIGINT NOT NULL REFERENCES shops(shop_id),
    customer_id BIGINT NOT NULL REFERENCES customers(customer_id),
    status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
    -- REQUESTED, APPROVED, LABEL_SENT, IN_TRANSIT, RECEIVED, INSPECTED, COMPLETED, REJECTED
    
    -- Return Shipping
    return_shipping_method VARCHAR(30), -- PREPAID_LABEL, CUSTOMER_ARRANGED, DROP_OFF
    return_carrier VARCHAR(100),
    return_tracking_number VARCHAR(100),
    return_label_url VARCHAR(500),
    
    -- Resolution
    resolution_type VARCHAR(30), -- REFUND, EXCHANGE, STORE_CREDIT
    resolution_amount DECIMAL(15, 2),
    exchange_variant_id UUID REFERENCES product_variants(variant_id),
    store_credit_id UUID,
    
    -- Inspection
    inspected_by BIGINT REFERENCES admin_users(id),
    inspected_at TIMESTAMP,
    inspection_condition VARCHAR(30), -- UNOPENED, OPENED, DAMAGED, USED
    inspection_notes TEXT,
    
    -- Timestamps
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    received_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Return Items
CREATE TABLE IF NOT EXISTS return_items (
    return_item_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    return_id UUID NOT NULL REFERENCES returns(return_id) ON DELETE CASCADE,
    order_item_id UUID NOT NULL,
    variant_id UUID REFERENCES product_variants(variant_id),
    quantity INTEGER NOT NULL,
    reason VARCHAR(50) NOT NULL, -- DAMAGED, WRONG_ITEM, NOT_AS_DESCRIBED, DEFECTIVE, etc.
    condition VARCHAR(30), -- UNOPENED, OPENED, DAMAGED
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- INVENTORY CONTEXT
-- =====================================================

-- Warehouses
CREATE TABLE IF NOT EXISTS warehouses (
    warehouse_id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT REFERENCES shops(shop_id), -- NULL for platform warehouses
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country_code VARCHAR(2),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    is_active BOOLEAN DEFAULT TRUE,
    priority INTEGER DEFAULT 0, -- for multi-warehouse fulfillment
    
    contact_name VARCHAR(100),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    operating_hours TEXT,
    type VARCHAR(50) DEFAULT 'FULFILLMENT_CENTER',
    capacity_units INTEGER,
    current_utilization DECIMAL(5,2),
    accepts_returns BOOLEAN DEFAULT TRUE,
    accepts_inbound BOOLEAN DEFAULT TRUE,
    supported_carriers TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inventory Items (stock per variant per warehouse)
CREATE TABLE IF NOT EXISTS inventory_items (
    inventory_item_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    variant_id UUID NOT NULL REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(warehouse_id) ON DELETE CASCADE,
    sku VARCHAR(100) NOT NULL,
    
    -- Quantities
    on_hand INTEGER NOT NULL DEFAULT 0,
    reserved INTEGER NOT NULL DEFAULT 0,
    available INTEGER GENERATED ALWAYS AS (on_hand - reserved),
    incoming INTEGER DEFAULT 0, -- from purchase orders
    damaged INTEGER DEFAULT 0,
    
    -- Thresholds
    low_stock_threshold INTEGER DEFAULT 10,
    reorder_point INTEGER DEFAULT 5,
    reorder_quantity INTEGER DEFAULT 50,
    max_stock INTEGER,
    
    -- Tracking
    tracking_type VARCHAR(20) DEFAULT 'NONE', -- NONE, BATCH, SERIAL
    
    -- Location
    location_code VARCHAR(50), -- warehouse location/bin
    
    last_counted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(variant_id, warehouse_id)
);

-- Inventory Movements (audit trail)
CREATE TABLE IF NOT EXISTS inventory_movements (
    movement_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    inventory_item_id UUID NOT NULL REFERENCES inventory_items(inventory_item_id),
    type VARCHAR(30) NOT NULL, -- PURCHASE, SALE, RETURN, ADJUSTMENT, TRANSFER, RESERVATION, RELEASE
    quantity INTEGER NOT NULL, -- positive or negative
    quantity_before INTEGER NOT NULL,
    quantity_after INTEGER NOT NULL,
    reference_type VARCHAR(50), -- ORDER, TRANSFER, ADJUSTMENT, PURCHASE_ORDER
    reference_id VARCHAR(100),
    reason VARCHAR(255),
    note TEXT,
    performed_by BIGINT,
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Stock Reservations (hold stock for carts/pending orders)
CREATE TABLE IF NOT EXISTS stock_reservations (
    reservation_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    inventory_item_id UUID NOT NULL REFERENCES inventory_items(inventory_item_id),
    reference_type VARCHAR(30) NOT NULL, -- CART, ORDER
    reference_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    released_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- VENDOR CONTEXT ENHANCEMENTS
-- =====================================================

-- Enhanced Shop columns
ALTER TABLE shops ADD COLUMN IF NOT EXISTS slug VARCHAR(100);
ALTER TABLE shops ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PENDING';
ALTER TABLE shops ADD COLUMN IF NOT EXISTS legal_name VARCHAR(255);
ALTER TABLE shops ADD COLUMN IF NOT EXISTS business_type VARCHAR(30);
ALTER TABLE shops ADD COLUMN IF NOT EXISTS tax_id VARCHAR(50);
ALTER TABLE shops ADD COLUMN IF NOT EXISTS registration_number VARCHAR(100);
ALTER TABLE shops ADD COLUMN IF NOT EXISTS established_year INTEGER;
ALTER TABLE shops ADD COLUMN IF NOT EXISTS logo VARCHAR(500);
ALTER TABLE shops ADD COLUMN IF NOT EXISTS banner VARCHAR(500);
ALTER TABLE shops ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE shops ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE shops ADD COLUMN IF NOT EXISTS website VARCHAR(500);
ALTER TABLE shops ADD COLUMN IF NOT EXISTS support_email VARCHAR(255);
ALTER TABLE shops ADD COLUMN IF NOT EXISTS return_window_days INTEGER DEFAULT 30;
ALTER TABLE shops ADD COLUMN IF NOT EXISTS shipping_policy TEXT;
ALTER TABLE shops ADD COLUMN IF NOT EXISTS return_policy TEXT;
ALTER TABLE shops ADD COLUMN IF NOT EXISTS verified_at TIMESTAMP;
ALTER TABLE shops ADD COLUMN IF NOT EXISTS metadata TEXT;

-- Vendor Bank Accounts
CREATE TABLE IF NOT EXISTS vendor_bank_accounts (
    bank_account_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    shop_id BIGINT NOT NULL REFERENCES shops(shop_id) ON DELETE CASCADE,
    account_name VARCHAR(255) NOT NULL,
    account_number_encrypted VARCHAR(500) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    routing_number VARCHAR(50),
    swift_code VARCHAR(20),
    iban VARCHAR(50),
    currency VARCHAR(3) DEFAULT 'USD',
    is_primary BOOLEAN DEFAULT FALSE,
    verified_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Commission Rates
CREATE TABLE IF NOT EXISTS commission_rates (
    commission_rate_id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT REFERENCES shops(shop_id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES categories(category_id) ON DELETE CASCADE,
    rate DECIMAL(5, 2) NOT NULL, -- percentage
    effective_from DATE NOT NULL,
    effective_to DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(shop_id, category_id, effective_from)
);

-- Vendor Payouts
CREATE TABLE IF NOT EXISTS vendor_payouts (
    payout_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    shop_id BIGINT NOT NULL REFERENCES shops(shop_id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    
    -- Summary
    gross_sales DECIMAL(15, 2) NOT NULL,
    refunds DECIMAL(15, 2) DEFAULT 0,
    commission DECIMAL(15, 2) NOT NULL,
    fees DECIMAL(15, 2) DEFAULT 0,
    adjustments DECIMAL(15, 2) DEFAULT 0,
    net_payout DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    
    -- Bank Transfer
    bank_account_id UUID REFERENCES vendor_bank_accounts(bank_account_id),
    transaction_reference VARCHAR(255),
    
    scheduled_for DATE,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Payout Items (detail)
CREATE TABLE IF NOT EXISTS payout_items (
    payout_item_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    payout_id UUID NOT NULL REFERENCES vendor_payouts(payout_id) ON DELETE CASCADE,
    order_id UUID NOT NULL REFERENCES customer_orders(order_id),
    order_number VARCHAR(50),
    order_date DATE,
    gross_amount DECIMAL(15, 2) NOT NULL,
    commission_amount DECIMAL(15, 2) NOT NULL,
    net_amount DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- PROMOTION CONTEXT ENHANCEMENTS
-- =====================================================

-- Campaigns (extends coupons)
CREATE TABLE IF NOT EXISTS campaigns (
    campaign_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(30) NOT NULL, -- DISCOUNT, BUY_X_GET_Y, FREE_SHIPPING, FLASH_SALE, BUNDLE
    status VARCHAR(20) DEFAULT 'DRAFT', -- DRAFT, SCHEDULED, ACTIVE, PAUSED, ENDED
    
    -- Schedule
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    timezone VARCHAR(50) DEFAULT 'UTC',
    
    -- Eligibility
    customer_segments VARCHAR ARRAY,
    min_order_amount DECIMAL(15, 2),
    max_uses_total INTEGER,
    max_uses_per_customer INTEGER DEFAULT 1,
    first_time_customer_only BOOLEAN DEFAULT FALSE,
    
    -- Discount
    discount_type VARCHAR(20), -- PERCENTAGE, FIXED_AMOUNT, FIXED_PRICE
    discount_value DECIMAL(15, 2),
    max_discount_amount DECIMAL(15, 2),
    applies_to VARCHAR(20), -- ORDER, ITEM, SHIPPING
    
    -- Targeting
    target_type VARCHAR(30), -- ALL, SPECIFIC_PRODUCTS, CATEGORIES, BRANDS, SHOPS
    target_product_ids BIGINT ARRAY,
    target_category_ids BIGINT ARRAY,
    target_brand_ids BIGINT ARRAY,
    target_shop_ids BIGINT ARRAY,
    
    -- Metrics
    total_uses INTEGER DEFAULT 0,
    total_revenue DECIMAL(15, 2) DEFAULT 0,
    total_discount_given DECIMAL(15, 2) DEFAULT 0,
    
    created_by BIGINT REFERENCES admin_users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Loyalty Points Transactions
CREATE TABLE IF NOT EXISTS loyalty_transactions (
    transaction_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(customer_id),
    type VARCHAR(30) NOT NULL, -- EARN, REDEEM, EXPIRE, ADJUST
    points INTEGER NOT NULL, -- positive for earn, negative for redeem
    balance_after INTEGER NOT NULL,
    reference_type VARCHAR(50), -- ORDER, REFERRAL, PROMOTION, ADMIN
    reference_id VARCHAR(100),
    description VARCHAR(255),
    expires_at DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- NOTIFICATION CONTEXT
-- =====================================================

-- Notification Templates
CREATE TABLE IF NOT EXISTS notification_templates (
    template_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    code VARCHAR(100) NOT NULL, -- ORDER_PLACED, ORDER_SHIPPED, etc.
    channel VARCHAR(20) NOT NULL, -- EMAIL, SMS, PUSH, IN_APP
    name VARCHAR(255) NOT NULL,
    subject VARCHAR(500), -- for email
    body TEXT NOT NULL, -- with placeholders like {{order_number}}
    locale VARCHAR(10) DEFAULT 'en-US',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(code, channel, locale)
);

-- Notification Log
CREATE TABLE IF NOT EXISTS notifications (
    notification_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    template_id UUID REFERENCES notification_templates(template_id),
    recipient_id BIGINT NOT NULL,
    recipient_type VARCHAR(20) NOT NULL, -- CUSTOMER, ADMIN, VENDOR
    recipient_contact VARCHAR(255) NOT NULL, -- email, phone, device token
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, SENT, DELIVERED, FAILED, READ
    subject VARCHAR(500),
    body TEXT,
    metadata TEXT,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    failure_reason TEXT,
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notification Preferences
CREATE TABLE IF NOT EXISTS notification_preferences (
    preference_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_type VARCHAR(20) NOT NULL, -- CUSTOMER, ADMIN, VENDOR
    channel VARCHAR(20) NOT NULL,
    notification_type VARCHAR(50) NOT NULL, -- ORDER_UPDATES, MARKETING, PRICE_ALERTS, etc.
    is_enabled BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, user_type, channel, notification_type)
);

-- =====================================================
-- ENHANCED ORDER COLUMNS
-- =====================================================


-- Order Items enhancements

-- Order History
CREATE TABLE IF NOT EXISTS order_history (
    history_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES customer_orders(order_id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL,
    note TEXT,
    actor_id BIGINT,
    actor_type VARCHAR(20), -- CUSTOMER, ADMIN, SYSTEM
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- Product Variants
CREATE INDEX IF NOT EXISTS idx_variants_product ON product_variants(product_id);
CREATE INDEX IF NOT EXISTS idx_variants_sku ON product_variants(sku);
CREATE INDEX IF NOT EXISTS idx_variants_status ON product_variants(status);

-- Variant Attributes
CREATE INDEX IF NOT EXISTS idx_variant_attrs_variant ON variant_attributes(variant_id);
CREATE INDEX IF NOT EXISTS idx_variant_attrs_value ON variant_attributes(value_id);

-- Customer Addresses
CREATE INDEX IF NOT EXISTS idx_customer_addresses_customer ON customer_addresses(customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_addresses_default ON customer_addresses(customer_id, is_default);

-- Carts
CREATE INDEX IF NOT EXISTS idx_carts_customer ON carts(customer_id);
CREATE INDEX IF NOT EXISTS idx_carts_session ON carts(session_id);
CREATE INDEX IF NOT EXISTS idx_carts_status ON carts(status);
CREATE INDEX IF NOT EXISTS idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_variant ON cart_items(variant_id);

-- Wishlists
CREATE INDEX IF NOT EXISTS idx_wishlists_customer ON wishlists(customer_id);
CREATE INDEX IF NOT EXISTS idx_wishlist_items_wishlist ON wishlist_items(wishlist_id);
CREATE INDEX IF NOT EXISTS idx_wishlist_items_product ON wishlist_items(product_id);

-- Reviews
CREATE INDEX IF NOT EXISTS idx_reviews_product ON reviews(product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_customer ON reviews(customer_id);
CREATE INDEX IF NOT EXISTS idx_reviews_shop ON reviews(shop_id);
CREATE INDEX IF NOT EXISTS idx_reviews_status ON reviews(status);
CREATE INDEX IF NOT EXISTS idx_reviews_rating ON reviews(rating_overall);

-- Payment Transactions
CREATE INDEX IF NOT EXISTS idx_payment_trans_order ON payment_transactions(order_id);
CREATE INDEX IF NOT EXISTS idx_payment_trans_customer ON payment_transactions(customer_id);
CREATE INDEX IF NOT EXISTS idx_payment_trans_status ON payment_transactions(status);
CREATE INDEX IF NOT EXISTS idx_payment_trans_gateway ON payment_transactions(gateway, gateway_transaction_id);

-- Refunds
CREATE INDEX IF NOT EXISTS idx_refunds_order ON refunds(order_id);
CREATE INDEX IF NOT EXISTS idx_refunds_status ON refunds(status);

-- Shipments
CREATE INDEX IF NOT EXISTS idx_shipments_order ON shipments(order_id);
CREATE INDEX IF NOT EXISTS idx_shipments_shop ON shipments(shop_id);
CREATE INDEX IF NOT EXISTS idx_shipments_status ON shipments(status);
CREATE INDEX IF NOT EXISTS idx_shipments_tracking ON shipments(tracking_number);

-- Returns
CREATE INDEX IF NOT EXISTS idx_returns_order ON returns(order_id);
CREATE INDEX IF NOT EXISTS idx_returns_customer ON returns(customer_id);
CREATE INDEX IF NOT EXISTS idx_returns_status ON returns(status);

-- Inventory
CREATE INDEX IF NOT EXISTS idx_inventory_variant ON inventory_items(variant_id);
CREATE INDEX IF NOT EXISTS idx_inventory_warehouse ON inventory_items(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_inventory_sku ON inventory_items(sku);
-- CREATE INDEX IF NOT EXISTS idx_inventory_low_stock ON inventory_items(available) WHERE available <= low_stock_threshold;

-- Inventory Movements
CREATE INDEX IF NOT EXISTS idx_inv_movements_item ON inventory_movements(inventory_item_id);
CREATE INDEX IF NOT EXISTS idx_inv_movements_type ON inventory_movements(type);
CREATE INDEX IF NOT EXISTS idx_inv_movements_reference ON inventory_movements(reference_type, reference_id);

-- Notifications
CREATE INDEX IF NOT EXISTS idx_notifications_recipient ON notifications(recipient_id, recipient_type);
CREATE INDEX IF NOT EXISTS idx_notifications_status ON notifications(status);

-- Vendor Payouts
CREATE INDEX IF NOT EXISTS idx_payouts_shop ON vendor_payouts(shop_id);
CREATE INDEX IF NOT EXISTS idx_payouts_status ON vendor_payouts(status);
CREATE INDEX IF NOT EXISTS idx_payouts_period ON vendor_payouts(period_start, period_end);

-- Order Number (human readable)
CREATE INDEX IF NOT EXISTS idx_orders_number ON customer_orders(order_number);

-- Products
CREATE INDEX IF NOT EXISTS idx_products_slug ON products(slug);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_products_shop_category ON products(fk_shop_id, fk_category_id);

-- =====================================================
-- SEED DATA
-- =====================================================

-- Default Attributes
INSERT INTO attributes (name, code, type, is_variant_attribute, is_filterable, is_searchable, position)
SELECT 'Color', 'color', 'SELECT', true, true, true, 1 WHERE NOT EXISTS (SELECT 1 FROM attributes WHERE code = 'color');
INSERT INTO attributes (name, code, type, is_variant_attribute, is_filterable, is_searchable, position)
SELECT 'Size', 'size', 'SELECT', true, true, true, 2 WHERE NOT EXISTS (SELECT 1 FROM attributes WHERE code = 'size');
INSERT INTO attributes (name, code, type, is_variant_attribute, is_filterable, is_searchable, position)
SELECT 'Material', 'material', 'SELECT', false, true, true, 3 WHERE NOT EXISTS (SELECT 1 FROM attributes WHERE code = 'material');
INSERT INTO attributes (name, code, type, is_variant_attribute, is_filterable, is_searchable, position)
SELECT 'Weight', 'weight', 'TEXT', false, true, false, 4 WHERE NOT EXISTS (SELECT 1 FROM attributes WHERE code = 'weight');
INSERT INTO attributes (name, code, type, is_variant_attribute, is_filterable, is_searchable, position)
SELECT 'Pattern', 'pattern', 'SELECT', false, true, true, 5 WHERE NOT EXISTS (SELECT 1 FROM attributes WHERE code = 'pattern');

-- Common Color Values
INSERT INTO attribute_values (attribute_id, display_value, code, metadata, position)
SELECT a.attribute_id, v.display_val, v.code, v.metadata, v.position
FROM attributes a
CROSS JOIN (VALUES
    ('Red', 'red', '{"hex": "#FF0000"}', 1),
    ('Blue', 'blue', '{"hex": "#0000FF"}', 2),
    ('Green', 'green', '{"hex": "#00FF00"}', 3),
    ('Black', 'black', '{"hex": "#000000"}', 4),
    ('White', 'white', '{"hex": "#FFFFFF"}', 5),
    ('Yellow', 'yellow', '{"hex": "#FFFF00"}', 6),
    ('Orange', 'orange', '{"hex": "#FFA500"}', 7),
    ('Purple', 'purple', '{"hex": "#800080"}', 8),
    ('Pink', 'pink', '{"hex": "#FFC0CB"}', 9),
    ('Brown', 'brown', '{"hex": "#A52A2A"}', 10),
    ('Gray', 'gray', '{"hex": "#808080"}', 11),
    ('Navy', 'navy', '{"hex": "#000080"}', 12)
) AS v(display_val, code, metadata, position)
WHERE a.code = 'color'
AND NOT EXISTS (
    SELECT 1 FROM attribute_values av 
    WHERE av.attribute_id = a.attribute_id AND av.code = v.code
);

-- Common Size Values
INSERT INTO attribute_values (attribute_id, display_value, code, position)
SELECT a.attribute_id, v.display_val, v.code, v.position
FROM attributes a
CROSS JOIN (VALUES
    ('XXS', 'xxs', 1),
    ('XS', 'xs', 2),
    ('S', 's', 3),
    ('M', 'm', 4),
    ('L', 'l', 5),
    ('XL', 'xl', 6),
    ('XXL', 'xxl', 7),
    ('XXXL', 'xxxl', 8)
) AS v(display_val, code, position)
WHERE a.code = 'size'
AND NOT EXISTS (
    SELECT 1 FROM attribute_values av 
    WHERE av.attribute_id = a.attribute_id AND av.code = v.code
);

-- Common Material Values
INSERT INTO attribute_values (attribute_id, display_value, code, position)
SELECT a.attribute_id, v.display_val, v.code, v.position
FROM attributes a
CROSS JOIN (VALUES
    ('Cotton', 'cotton', 1),
    ('Polyester', 'polyester', 2),
    ('Wool', 'wool', 3),
    ('Silk', 'silk', 4),
    ('Leather', 'leather', 5),
    ('Denim', 'denim', 6),
    ('Linen', 'linen', 7),
    ('Nylon', 'nylon', 8),
    ('Velvet', 'velvet', 9),
    ('Cashmere', 'cashmere', 10)
) AS v(display_val, code, position)
WHERE a.code = 'material'
AND NOT EXISTS (
    SELECT 1 FROM attribute_values av 
    WHERE av.attribute_id = a.attribute_id AND av.code = v.code
);

-- Default Carriers
INSERT INTO carriers (name, code, tracking_url_template)
SELECT v.name, v.code, v.tracking_url_template
FROM (VALUES
    ('DHL', 'dhl', 'https://www.dhl.com/en/express/tracking.html?AWB={tracking_number}'),
    ('FedEx', 'fedex', 'https://www.fedex.com/fedextrack/?trknbr={tracking_number}'),
    ('UPS', 'ups', 'https://www.ups.com/track?tracknum={tracking_number}'),
    ('USPS', 'usps', 'https://tools.usps.com/go/TrackConfirmAction?tLabels={tracking_number}'),
    ('Local Delivery', 'local', NULL)
) AS v(name, code, tracking_url_template)
WHERE NOT EXISTS (
    SELECT 1 FROM carriers c WHERE c.code = v.code
);

-- Default Notification Templates
INSERT INTO notification_templates (code, channel, name, subject, body)
SELECT v.code, v.channel, v.name, v.subject, v.body
FROM (VALUES
('ORDER_PLACED', 'EMAIL', 'Order Confirmation', 'Your order #{{order_number}} has been placed', 
'Dear {{customer_name}},

Thank you for your order!

Order Number: {{order_number}}
Order Date: {{order_date}}
Total: {{currency}}{{total}}

We will notify you when your order ships.

Thank you for shopping with us!'),

('ORDER_SHIPPED', 'EMAIL', 'Order Shipped', 'Your order #{{order_number}} has been shipped',
'Dear {{customer_name}},

Great news! Your order has been shipped.

Order Number: {{order_number}}
Tracking Number: {{tracking_number}}
Carrier: {{carrier_name}}

Track your package: {{tracking_url}}

Thank you for shopping with us!'),

('ORDER_DELIVERED', 'EMAIL', 'Order Delivered', 'Your order #{{order_number}} has been delivered',
'Dear {{customer_name}},

Your order has been delivered!

Order Number: {{order_number}}
Delivered: {{delivery_date}}

We hope you enjoy your purchase. If you have any questions, please contact us.

Thank you for shopping with us!')
) AS v(code, channel, name, subject, body)
WHERE NOT EXISTS (
    SELECT 1 FROM notification_templates nt 
    WHERE nt.code = v.code AND nt.channel = v.channel AND nt.locale = 'en-US'
);

-- Default Warehouse (platform)
INSERT INTO warehouses (name, code, city, country_code, is_active, priority)
SELECT v.name, v.code, v.city, v.country, v.is_active, v.priority
FROM (VALUES
('Main Warehouse', 'MAIN', 'City', 'US', true, 1)
) AS v(name, code, city, country, is_active, priority)
WHERE NOT EXISTS (
    SELECT 1 FROM warehouses w WHERE w.code = v.code
);
