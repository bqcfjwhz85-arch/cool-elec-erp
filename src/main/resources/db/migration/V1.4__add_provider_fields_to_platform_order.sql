-- ================================================================
-- 平台订单表添加服务商字段
-- 版本：V1.4
-- 日期：2025-11-21
-- 说明：为平台订单表添加服务商相关字段，用于记录服务商信息
-- ================================================================

-- 添加服务商ID字段
ALTER TABLE erp_platform_order 
ADD COLUMN provider_id BIGINT NULL COMMENT '服务商ID' AFTER platform_name;

-- 添加服务商名称字段
ALTER TABLE erp_platform_order 
ADD COLUMN provider_name VARCHAR(200) NULL COMMENT '服务商名称' AFTER provider_id;

-- 添加服务商所属区域字段
ALTER TABLE erp_platform_order
ADD COLUMN provider_region VARCHAR(200) NULL COMMENT '服务商所属区域' AFTER provider_name;

-- 创建索引（优化按服务商查询订单的性能）
CREATE INDEX idx_platform_order_provider ON erp_platform_order(provider_id);

-- ================================================================
-- 说明：
-- 1. provider_id：服务商ID，关联 erp_provider 表
-- 2. provider_name：服务商名称（冗余存储，提高查询性能）
-- 3. provider_region：服务商所属区域（省/市）
-- 4. 字段位置：在 platform_name 之后，customer_id 之前
-- 5. 所有字段可选，允许为 NULL（兼容已有数据）
-- ================================================================
