-- ================================================================
-- 价格配置测试数据
-- 用途：快速测试新的价格配置功能
-- 说明：包含各种价格类型和配置场景的测试数据
-- ================================================================

-- 注意：执行前请确保已执行 V1.1__update_price_config_schema.sql

-- 前置条件：确保有测试商品
-- 如果不存在，请先创建测试商品
INSERT INTO erp_product (product_sku, product_name, brand, model, specification, unit, category, status, create_time, update_time)
VALUES ('TEST_PROD_001', '测试电表', '测试品牌', '测试型号', '220V/10A', '台', '电表', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE product_name = '测试电表';

-- ================================================================
-- 1. 国网价（统一价格）
-- ================================================================
INSERT INTO erp_price_config 
(product_sku, product_name, price_type, price, state_grid_price, status, effective_time, remark, create_time, update_time)
VALUES 
('TEST_PROD_001', '测试电表', 1, 100.00, 100.00, 1, NOW(), '国网统一价', NOW(), NOW())
ON DUPLICATE KEY UPDATE price = 100.00;

-- ================================================================
-- 2. 结算价（统一价格）
-- ================================================================
INSERT INTO erp_price_config 
(product_sku, product_name, price_type, price, settlement_price, status, effective_time, remark, create_time, update_time)
VALUES 
('TEST_PROD_001', '测试电表', 4, 90.00, 90.00, 1, NOW(), '结算统一价', NOW(), NOW())
ON DUPLICATE KEY UPDATE price = 90.00;

-- ================================================================
-- 3. 区域价配置
-- ================================================================

-- 3.1 默认区域价（每个商品只有1条）
INSERT INTO erp_price_config 
(product_sku, product_name, price_type, price, regional_price, is_default, region_name, status, effective_time, remark, create_time, update_time)
VALUES 
('TEST_PROD_001', '测试电表', 2, 95.00, 95.00, 1, '默认', 1, NOW(), '默认区域价', NOW(), NOW())
ON DUPLICATE KEY UPDATE price = 95.00;

-- 3.2 特殊区域价：国网商城+上海市
INSERT INTO erp_price_config 
(product_sku, product_name, price_type, price, regional_price, platform_id, platform_name, province, region_name, is_default, status, effective_time, remark, create_time, update_time)
VALUES 
('TEST_PROD_001', '测试电表', 2, 98.00, 98.00, 1, '国网商城', '上海市', '上海市', 0, 1, NOW(), '国网商城上海特价', NOW(), NOW())
ON DUPLICATE KEY UPDATE price = 98.00;

-- 3.3 特殊区域价：国网商城+江苏省
INSERT INTO erp_price_config 
(product_sku, product_name, price_type, price, regional_price, platform_id, platform_name, province, region_name, is_default, status, effective_time, remark, create_time, update_time)
VALUES 
('TEST_PROD_001', '测试电表', 2, 97.50, 97.50, 1, '国网商城', '江苏省', '江苏省', 0, 1, NOW(), '国网商城江苏特价', NOW(), NOW())
ON DUPLICATE KEY UPDATE price = 97.50;

-- 3.4 特殊区域价：浙江省（全平台，platformId为NULL）
INSERT INTO erp_price_config 
(product_sku, product_name, price_type, price, regional_price, platform_id, platform_name, province, region_name, is_default, status, effective_time, remark, create_time, update_time)
VALUES 
('TEST_PROD_001', '测试电表', 2, 96.00, 96.00, NULL, NULL, '浙江省', '浙江省', 0, 1, NOW(), '浙江省全平台通用价格', NOW(), NOW())
ON DUPLICATE KEY UPDATE price = 96.00;

-- 3.5 特殊区域价：广东省（全平台）
INSERT INTO erp_price_config 
(product_sku, product_name, price_type, price, regional_price, platform_id, platform_name, province, region_name, is_default, status, effective_time, remark, create_time, update_time)
VALUES 
('TEST_PROD_001', '测试电表', 2, 94.00, 94.00, NULL, NULL, '广东省', '广东省', 0, 1, NOW(), '广东省全平台通用价格', NOW(), NOW())
ON DUPLICATE KEY UPDATE price = 94.00;

-- ================================================================
-- 4. 服务商价配置
-- ================================================================

-- 4.1 默认服务商价（每个商品只有1条）
INSERT INTO erp_price_config 
(product_sku, product_name, price_type, price, provider_price, is_default, provider_name, status, effective_time, remark, create_time, update_time)
VALUES 
('TEST_PROD_001', '测试电表', 3, 88.00, 88.00, 1, '默认', 1, NOW(), '默认服务商价', NOW(), NOW())
ON DUPLICATE KEY UPDATE price = 88.00;

-- 4.2 特殊服务商价：服务商ID=1（假设存在）
-- 注意：实际使用时需要确保 provider_id 在 erp_provider 表中存在
INSERT INTO erp_price_config 
(product_sku, product_name, price_type, price, provider_price, provider_id, provider_name, is_default, status, effective_time, remark, create_time, update_time)
VALUES 
('TEST_PROD_001', '测试电表', 3, 90.00, 90.00, 1, 'A服务商', 0, 1, NOW(), 'A服务商优惠价', NOW(), NOW())
ON DUPLICATE KEY UPDATE price = 90.00;

-- 4.3 特殊服务商价：服务商ID=2
INSERT INTO erp_price_config 
(product_sku, product_name, price_type, price, provider_price, provider_id, provider_name, is_default, status, effective_time, remark, create_time, update_time)
VALUES 
('TEST_PROD_001', '测试电表', 3, 92.00, 92.00, 2, 'B服务商', 0, 1, NOW(), 'B服务商价格', NOW(), NOW())
ON DUPLICATE KEY UPDATE price = 92.00;

-- ================================================================
-- 测试场景说明
-- ================================================================

/*
场景1：查询国网价
  - 商品SKU: TEST_PROD_001
  - 价格类型: 1
  - 预期结果: 100.00

场景2：查询结算价
  - 商品SKU: TEST_PROD_001
  - 价格类型: 4
  - 预期结果: 90.00

场景3：查询区域价（国网商城+上海市）
  - 商品SKU: TEST_PROD_001
  - 价格类型: 2
  - 平台ID: 1
  - 省份: 上海市
  - 预期结果: 98.00（精确匹配）

场景4：查询区域价（浙江省，任意平台）
  - 商品SKU: TEST_PROD_001
  - 价格类型: 2
  - 省份: 浙江省
  - 预期结果: 96.00（省份通用）

场景5：查询区域价（北京市，没有特殊配置）
  - 商品SKU: TEST_PROD_001
  - 价格类型: 2
  - 省份: 北京市
  - 预期结果: 95.00（默认区域价）

场景6：查询服务商价（服务商ID=1）
  - 商品SKU: TEST_PROD_001
  - 价格类型: 3
  - 服务商ID: 1
  - 预期结果: 90.00（特殊服务商价）

场景7：查询服务商价（服务商ID=999，不存在配置）
  - 商品SKU: TEST_PROD_001
  - 价格类型: 3
  - 服务商ID: 999
  - 预期结果: 88.00（默认服务商价）
*/

-- ================================================================
-- 查询测试 SQL
-- ================================================================

-- 查看所有测试价格配置
SELECT 
    id,
    price_type,
    CASE price_type
        WHEN 1 THEN '国网价'
        WHEN 2 THEN '区域价'
        WHEN 3 THEN '服务商价'
        WHEN 4 THEN '结算价'
    END AS price_type_name,
    price,
    is_default,
    platform_id,
    platform_name,
    province,
    provider_id,
    provider_name,
    remark
FROM erp_price_config
WHERE product_sku = 'TEST_PROD_001'
  AND status = 1
ORDER BY price_type, is_default DESC, id;

-- 测试区域价查询优先级（上海市）
SELECT 
    id,
    price,
    platform_id,
    platform_name,
    province,
    is_default,
    CASE
        WHEN platform_id = 1 AND province = '上海市' THEN '优先级1：精确匹配'
        WHEN platform_id IS NULL AND province = '上海市' THEN '优先级2：省份通用'
        WHEN is_default = 1 THEN '优先级3：默认价格'
    END AS priority_level,
    remark
FROM erp_price_config
WHERE product_sku = 'TEST_PROD_001'
  AND price_type = 2
  AND status = 1
  AND (
    (platform_id = 1 AND province = '上海市' AND is_default = 0)
    OR (platform_id IS NULL AND province = '上海市' AND is_default = 0)
    OR (is_default = 1)
  )
ORDER BY
  CASE
    WHEN platform_id = 1 AND province = '上海市' THEN 1
    WHEN platform_id IS NULL AND province = '上海市' THEN 2
    WHEN is_default = 1 THEN 3
  END;

-- ================================================================
-- 清理测试数据（可选）
-- ================================================================

-- 如果需要清理测试数据，执行以下语句
-- DELETE FROM erp_price_config WHERE product_sku = 'TEST_PROD_001';
-- DELETE FROM erp_product WHERE product_sku = 'TEST_PROD_001';







