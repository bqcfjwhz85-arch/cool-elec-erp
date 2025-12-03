-- ================================================================
-- 价格配置表结构更新
-- 版本：V1.1
-- 日期：2025-11-06
-- 说明：添加平台、省份、默认标识等字段，支持灵活的价格配置
-- ================================================================

-- 1. 添加新字段
ALTER TABLE erp_price_config 
ADD COLUMN platform_id BIGINT NULL COMMENT '平台ID（区域价可选，NULL表示全平台通用）' AFTER region_name;

ALTER TABLE erp_price_config 
ADD COLUMN platform_name VARCHAR(200) NULL COMMENT '平台名称' AFTER platform_id;

ALTER TABLE erp_price_config 
ADD COLUMN province VARCHAR(100) NULL COMMENT '省份名称（区域价使用）' AFTER platform_name;

ALTER TABLE erp_price_config 
ADD COLUMN is_default TINYINT NULL COMMENT '是否默认：0-否 1-是（国网价和结算价为NULL）' AFTER province;

-- 2. 添加冗余价格字段（用于前端显示和数据完整性）
ALTER TABLE erp_price_config 
ADD COLUMN state_grid_price DECIMAL(10, 2) NULL COMMENT '国网价（冗余字段，priceType=1时使用）' AFTER price;

ALTER TABLE erp_price_config 
ADD COLUMN regional_price DECIMAL(10, 2) NULL COMMENT '区域价（冗余字段，priceType=2时使用）' AFTER state_grid_price;

ALTER TABLE erp_price_config 
ADD COLUMN provider_price DECIMAL(10, 2) NULL COMMENT '服务商价（冗余字段，priceType=3时使用）' AFTER regional_price;

ALTER TABLE erp_price_config 
ADD COLUMN settlement_price DECIMAL(10, 2) NULL COMMENT '结算价（冗余字段，priceType=4时使用）' AFTER provider_price;

-- 3. 创建索引
-- 3.1 平台ID索引（用于按平台查询区域价）
CREATE INDEX idx_price_platform ON erp_price_config(platform_id);

-- 3.2 省份索引（用于按省份查询区域价）
CREATE INDEX idx_price_province ON erp_price_config(province);

-- 3.3 isDefault索引（用于快速查询默认价格）
CREATE INDEX idx_price_default ON erp_price_config(is_default);

-- 3.4 复合索引：商品SKU + 价格类型 + isDefault（用于快速查询默认价格）
CREATE INDEX idx_price_sku_type_default ON erp_price_config(product_sku, price_type, is_default);

-- 4. 创建唯一性约束（注意：MySQL不支持带WHERE条件的唯一索引，需要在应用层校验）
-- 4.1 默认区域价唯一性（应用层校验）
-- 规则：同一商品(productSku) + 价格类型(priceType=2) + isDefault=1 只能有一条

-- 4.2 默认服务商价唯一性（应用层校验）
-- 规则：同一商品(productSku) + 价格类型(priceType=3) + isDefault=1 只能有一条

-- 4.3 特殊服务商价唯一性（应用层校验）
-- 规则：同一商品(productSku) + priceType=3 + isDefault=0 + providerId 不能重复
-- 注意：由于需要过滤条件，使用应用层校验而不是数据库约束

-- 4.4 特殊区域价唯一性（应用层校验）
-- 规则：同一商品(productSku) + priceType=2 + isDefault=0 + platformId + province 不能重复
-- 注意：由于platformId可能为NULL，需要在应用层验证

-- 5. 更新注释（确保表注释完整）
ALTER TABLE erp_price_config COMMENT = '价格配置表（支持国网价、区域价、服务商价、结算价）';

-- ================================================================
-- 说明：
-- 1. 区域价配置：
--    - 默认区域价：priceType=2, isDefault=1，每个商品只有1条
--    - 特殊区域价：priceType=2, isDefault=0，可以有多条
--      - 可以指定"平台+省份"
--      - 可以只指定"省份"（platformId=NULL，表示全平台通用）
--
-- 2. 服务商价配置：
--    - 默认服务商价：priceType=3, isDefault=1，每个商品只有1条
--    - 特殊服务商价：priceType=3, isDefault=0，可以有多条
--
-- 3. 国网价和结算价：
--    - priceType=1（国网价）和 priceType=4（结算价）不使用isDefault字段
--    - 每个商品只有一条国网价和一条结算价
--
-- 4. 查询优先级（区域价）：
--    - 平台+省份精确匹配 > 省份通用 > 默认区域价
--
-- 5. 查询优先级（服务商价）：
--    - 特殊服务商价 > 默认服务商价
-- ================================================================

