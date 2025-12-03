-- ================================================================
-- 平台订单表添加客户地区字段
-- 版本：V1.2
-- 日期：2025-11-06
-- 说明：为平台订单表添加客户地区字段，用于记录客户所在地区
-- ================================================================

-- 添加客户地区字段
ALTER TABLE erp_platform_order 
ADD COLUMN customer_region VARCHAR(200) NULL COMMENT '客户地区' AFTER customer_name;

-- 创建索引（如果需要按地区查询订单）
CREATE INDEX idx_platform_order_region ON erp_platform_order(customer_region);

-- ================================================================
-- 说明：
-- 1. customer_region：记录客户所在地区（省/市）
-- 2. 字段位置：在customer_name之后，shippingAddress之前
-- 3. 该字段可选，允许为NULL
-- ================================================================

