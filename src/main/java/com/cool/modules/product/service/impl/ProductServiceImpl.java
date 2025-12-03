package com.cool.modules.product.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.base.ModifyEnum;
import com.cool.core.exception.CoolPreconditions;
import com.cool.core.util.CodeGenerator;
import com.cool.modules.config.entity.BrandEntity;
import com.cool.modules.config.entity.ProductCategoryEntity;
import com.cool.modules.config.entity.ProductEntity;
import com.cool.modules.config.entity.ProductModelEntity;
import com.cool.modules.config.mapper.ProductMapper;
import com.cool.modules.config.service.BrandService;
import com.cool.modules.config.service.ProductCategoryService;
import com.cool.modules.config.service.ProductModelService;
import com.cool.modules.config.mapper.SupplierGoodsMapper;
import com.cool.modules.config.entity.SupplierGoodsEntity;
import com.cool.modules.product.service.ProductService;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import static com.cool.modules.config.entity.table.ProductEntityTableDef.PRODUCT_ENTITY;
import static com.cool.modules.config.entity.table.SupplierGoodsEntityTableDef.SUPPLIER_GOODS_ENTITY;
import static com.cool.modules.config.entity.table.BrandEntityTableDef.BRAND_ENTITY;
import static com.cool.modules.config.entity.table.ProductCategoryEntityTableDef.PRODUCT_CATEGORY_ENTITY;
import static com.cool.modules.config.entity.table.ProductModelEntityTableDef.PRODUCT_MODEL_ENTITY;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

/**
 * 商品服务实现类
 */
@Service
@Transactional
public class ProductServiceImpl extends BaseServiceImpl<ProductMapper, ProductEntity> 
        implements ProductService {
    
    private final BrandService brandService;
    private final ProductModelService productModelService;
    private final ProductCategoryService productCategoryService;
    private final SupplierGoodsMapper supplierGoodsMapper;
    private final com.cool.modules.config.service.PriceConfigService priceConfigService;
    
    // 使用构造器注入,@Lazy 打破循环依赖
    public ProductServiceImpl(@Lazy BrandService brandService,
                             @Lazy ProductModelService productModelService,
                             @Lazy ProductCategoryService productCategoryService,
                             SupplierGoodsMapper supplierGoodsMapper,
                             @Lazy com.cool.modules.config.service.PriceConfigService priceConfigService) {
        this.brandService = brandService;
        this.productModelService = productModelService;
        this.productCategoryService = productCategoryService;
        this.supplierGoodsMapper = supplierGoodsMapper;
        this.priceConfigService = priceConfigService;
    }

    @Override
    public Object page(JSONObject requestParams, com.mybatisflex.core.paginate.Page<ProductEntity> page, QueryWrapper queryWrapper) {
        com.mybatisflex.core.paginate.Page<ProductEntity> result = (com.mybatisflex.core.paginate.Page<ProductEntity>) super.page(requestParams, page, queryWrapper);
        java.util.List<ProductEntity> list = result.getRecords();
        if (list == null || list.isEmpty()) {
            return result;
        }

        // 提取SKU列表
        java.util.List<String> skus = list.stream()
                .map(ProductEntity::getProductSku)
                .filter(StrUtil::isNotBlank)
                .collect(java.util.stream.Collectors.toList());

        if (skus.isEmpty()) {
            return result;
        }

        // 查询国网价 (priceType=1)
        java.util.List<com.cool.modules.config.entity.PriceConfigEntity> prices = priceConfigService.list(QueryWrapper.create()
                .in(com.cool.modules.config.entity.PriceConfigEntity::getProductSku, skus)
                .eq(com.cool.modules.config.entity.PriceConfigEntity::getPriceType, 1));

        // 映射价格
        java.util.Map<String, java.math.BigDecimal> priceMap = prices.stream()
                .collect(java.util.stream.Collectors.toMap(
                        com.cool.modules.config.entity.PriceConfigEntity::getProductSku,
                        p -> p.getPrice() != null ? p.getPrice() : p.getStateGridPrice(),
                        (v1, v2) -> v1 // 如果有重复，取第一个
                ));

        for (ProductEntity product : list) {
            product.setPrice(priceMap.get(product.getProductSku()));
        }
        
        // 填充 supplierIds
        // 批量查询所有商品的供应商关联
        java.util.List<Long> productIds = list.stream().map(ProductEntity::getId).collect(java.util.stream.Collectors.toList());
        if (!productIds.isEmpty()) {
            java.util.List<SupplierGoodsEntity> relations = supplierGoodsMapper.selectListByQuery(
                QueryWrapper.create().where(SUPPLIER_GOODS_ENTITY.GOODS_ID.in(productIds))
            );
            
            java.util.Map<Long, java.util.List<Long>> relationMap = relations.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    SupplierGoodsEntity::getGoodsId,
                    java.util.stream.Collectors.mapping(SupplierGoodsEntity::getSupplierId, java.util.stream.Collectors.toList())
                ));
                
            for (ProductEntity product : list) {
                product.setSupplierIds(relationMap.get(product.getId()));
            }
        }

        return result;
    }

    @Override
    public ProductEntity info(JSONObject requestParams, Long id) {
        ProductEntity entity = (ProductEntity) super.info(requestParams, id);
        if (entity != null) {
            // 查询供应商ID集合
            java.util.List<SupplierGoodsEntity> list = supplierGoodsMapper.selectListByQuery(
                QueryWrapper.create().where(SUPPLIER_GOODS_ENTITY.GOODS_ID.eq(id))
            );
            entity.setSupplierIds(list.stream()
                .map(SupplierGoodsEntity::getSupplierId)
                .collect(java.util.stream.Collectors.toList()));
        }
        return entity;
    }
    
    @Override
    public ProductEntity getBySku(String sku) {
        return getOne(QueryWrapper.create()
            .where(PRODUCT_ENTITY.PRODUCT_SKU.eq(sku))
            .and(PRODUCT_ENTITY.STATUS.eq(1)));
    }
    
    @Override
    public boolean existsBySku(String sku) {
        return count(QueryWrapper.create()
            .where(PRODUCT_ENTITY.PRODUCT_SKU.eq(sku))) > 0;
    }
    
    @Override
    public void modifyBefore(JSONObject requestParams, ProductEntity entity, ModifyEnum type) {
        // 新增/修改前校验和自动填充
        if (type == ModifyEnum.ADD || type == ModifyEnum.UPDATE) {
            // 设置默认状态：如果没有传status，默认为启用
            if (entity.getStatus() == null) {
                entity.setStatus(1);
            }
            
            // 新增时自动生成SKU（如果前端未提供）
            if (type == ModifyEnum.ADD && StrUtil.isBlank(entity.getProductSku())) {
                String autoSku = CodeGenerator.generateCode("SKU", todayPrefix -> {
                    // 查询当天该前缀的最大SKU
                    ProductEntity maxProduct = getOne(QueryWrapper.create()
                        .where(PRODUCT_ENTITY.PRODUCT_SKU.like(todayPrefix + "%"))
                        .orderBy(PRODUCT_ENTITY.PRODUCT_SKU, false)
                        .limit(1));
                    return maxProduct != null ? maxProduct.getProductSku() : null;
                });
                entity.setProductSku(autoSku);
            }
            
            // 校验SKU唯一性
            QueryWrapper qw = QueryWrapper.create()
                .where(PRODUCT_ENTITY.PRODUCT_SKU.eq(entity.getProductSku()));
            
            if (type == ModifyEnum.UPDATE) {
                qw.and(PRODUCT_ENTITY.ID.ne(entity.getId()));
            }
            
            ProductEntity exists = getOne(qw);
            CoolPreconditions.check(exists != null, "商品SKU已存在");
            
            // 自动填充品牌名称（兼容旧字段）
            if (entity.getBrandId() != null) {
                BrandEntity brand = brandService.getById(entity.getBrandId());
                if (brand != null) {
                    entity.setBrand(brand.getBrandName());
                }
            } else if (type == ModifyEnum.UPDATE && requestParams.containsKey("brandId")) {
                // 更新时如果前端显式传了 brandId=null，清空品牌名称
                entity.setBrand(null);
            }
            
            // 自动填充型号名称（兼容旧字段）
            if (entity.getModelId() != null) {
                ProductModelEntity model = productModelService.getById(entity.getModelId());
                if (model != null) {
                    entity.setModel(model.getModelName());
                }
            } else if (type == ModifyEnum.UPDATE && requestParams.containsKey("modelId")) {
                // 更新时如果前端显式传了 modelId=null，清空型号名称
                entity.setModel(null);
            }
            
            // 自动填充类别名称（兼容旧字段）
            if (entity.getCategoryId() != null) {
                ProductCategoryEntity category = productCategoryService.getById(entity.getCategoryId());
                if (category != null) {
                    entity.setCategory(category.getCategoryName());
                }
            } else if (type == ModifyEnum.UPDATE && requestParams.containsKey("categoryId")) {
                // 更新时如果前端显式传了 categoryId=null，清空类别名称
                entity.setCategory(null);
            }
        }
    }
    
    @Override
    public Object add(JSONObject requestParams, ProductEntity entity) {
        this.modifyBefore(requestParams, entity, ModifyEnum.ADD);
        this.add(entity);
        this.modifyAfter(requestParams, entity, ModifyEnum.ADD);
        
        // 返回 id 和 productSku（前端需要 productSku 来配置价格）
        JSONObject result = new JSONObject();
        result.set("id", entity.getId());
        result.set("productSku", entity.getProductSku());
        return result;
    }
    
    @Override
    public boolean update(JSONObject requestParams, ProductEntity entity) {
        // 获取更新前的商品信息,用于比较supplierId是否变化
        ProductEntity oldEntity = this.getById(entity.getId());
        
        this.modifyBefore(requestParams, entity, ModifyEnum.UPDATE);
        boolean result = this.updateById(entity);
        
        if (result) {
            this.modifyAfter(requestParams, entity, ModifyEnum.UPDATE, oldEntity);
        }
        
        return result;
    }
    
    @Override
    public void modifyAfter(JSONObject requestParams, ProductEntity entity, ModifyEnum type) {
        this.modifyAfter(requestParams, entity, type, null);
    }
    
    /**
     * 修改后处理,同步供应商-商品关联表
     * 
     * @param requestParams 请求参数
     * @param entity 实体对象
     * @param type 操作类型
     * @param oldEntity 旧实体对象(仅UPDATE时有值)
     */
    private void modifyAfter(JSONObject requestParams, ProductEntity entity, ModifyEnum type, ProductEntity oldEntity) {
        // 同步供应商-商品关联表
        if (type == ModifyEnum.ADD || type == ModifyEnum.UPDATE) {
            java.util.List<Long> supplierIds = entity.getSupplierIds();
            
            // 如果前端传了 supplierIds (即使是空列表)，则以 supplierIds 为准
            if (supplierIds != null) {
                Long productId = entity.getId();
                
                // 1. 删除该商品的所有供应商关联
                supplierGoodsMapper.deleteByQuery(
                    QueryWrapper.create()
                        .where(SUPPLIER_GOODS_ENTITY.GOODS_ID.eq(productId))
                );
                
                // 2. 插入新关联
                if (!supplierIds.isEmpty()) {
                    java.util.List<SupplierGoodsEntity> list = new java.util.ArrayList<>();
                    for (Long sid : supplierIds) {
                        SupplierGoodsEntity sge = new SupplierGoodsEntity();
                        sge.setGoodsId(productId);
                        sge.setSupplierId(sid);
                        list.add(sge);
                    }
                    supplierGoodsMapper.insertBatch(list);
                }
                
                // 3. 更新主表的 supplierId (取第一个或null，保持兼容)
                Long primarySupplierId = supplierIds.isEmpty() ? null : supplierIds.get(0);
                // 仅当主表字段与第一个供应商不一致时才更新，避免不必要的数据库操作
                // 注意：这里直接使用 mapper 更新，避免触发递归调用
                if (!java.util.Objects.equals(primarySupplierId, entity.getSupplierId())) {
                    entity.setSupplierId(primarySupplierId);
                    mapper.update(entity);
                }
            } else {
                // 兼容旧逻辑：如果没传 supplierIds，但 supplierId 变了
                Long newSupplierId = entity.getSupplierId();
                Long oldSupplierId = (oldEntity != null) ? oldEntity.getSupplierId() : null;
                
                if (!java.util.Objects.equals(newSupplierId, oldSupplierId)) {
                    Long productId = entity.getId();
                    
                    supplierGoodsMapper.deleteByQuery(
                        QueryWrapper.create()
                            .where(SUPPLIER_GOODS_ENTITY.GOODS_ID.eq(productId))
                    );
                    
                    if (newSupplierId != null) {
                        SupplierGoodsEntity supplierGoods = new SupplierGoodsEntity();
                        supplierGoods.setSupplierId(newSupplierId);
                        supplierGoods.setGoodsId(productId);
                        supplierGoodsMapper.insert(supplierGoods);
                    }
                }
            }
        }
    }

    @Override
    public Object importProducts(MultipartFile file) throws IOException {
        CoolPreconditions.check(file.isEmpty(), "上传文件不能为空");
        CoolPreconditions.check(!file.getOriginalFilename().endsWith(".xlsx") && 
                               !file.getOriginalFilename().endsWith(".xls"), "仅支持Excel文件格式");
        
        ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
        List<Map<String, Object>> list = (List) reader.readAll(Map.class);
        return importProducts(list);
    }

    @Override
    public Object importProducts(List<Map<String, Object>> list) {
        CoolPreconditions.check(CollUtil.isEmpty(list), "数据不能为空");
        
        int successCount = 0;
        int failCount = 0;
        List<Map<String, Object>> failList = new ArrayList<>();
        
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> row = list.get(i);
            try {
                String sku = getStr(row, "productSku", "商品SKU");
                String name = getStr(row, "productName", "商品名称");
                String brandName = getStr(row, "brandName", "品牌");
                String modelName = getStr(row, "modelName", "型号");
                String categoryName = getStr(row, "categoryName", "商品类别");
                
                if (StrUtil.isBlank(sku)) {
                    // Auto-generate SKU
                    sku = CodeGenerator.generateCode("P", prefix -> {
                        ProductEntity max = this.getOne(QueryWrapper.create()
                            .where(PRODUCT_ENTITY.PRODUCT_SKU.like(prefix + "%"))
                            .orderBy(PRODUCT_ENTITY.PRODUCT_SKU, false)
                            .limit(1));
                        return max != null ? max.getProductSku() : null;
                    });
                }
                if (StrUtil.isBlank(name)) throw new RuntimeException("商品名称不能为空");
                
                // Handle Brand
                Long brandId = null;
                if (StrUtil.isNotBlank(brandName)) {
                    BrandEntity brand = brandService.getOne(QueryWrapper.create().where(BRAND_ENTITY.BRAND_NAME.eq(brandName)));
                    if (brand == null) {
                        brand = new BrandEntity();
                        brand.setBrandName(brandName);
                        brand.setBrandCode(CodeGenerator.generateCode("B", prefix -> {
                            BrandEntity max = brandService.getOne(QueryWrapper.create()
                                .where(BRAND_ENTITY.BRAND_CODE.like(prefix + "%"))
                                .orderBy(BRAND_ENTITY.BRAND_CODE, false)
                                .limit(1));
                            return max != null ? max.getBrandCode() : null;
                        }));
                        brand.setStatus(1);
                        brandService.add(brand);
                    }
                    brandId = brand.getId();
                }
                
                // Handle Category
                Long categoryId = null;
                if (StrUtil.isNotBlank(categoryName)) {
                    ProductCategoryEntity category = productCategoryService.getOne(QueryWrapper.create().where(PRODUCT_CATEGORY_ENTITY.CATEGORY_NAME.eq(categoryName)));
                    if (category == null) {
                        category = new ProductCategoryEntity();
                        category.setCategoryName(categoryName);
                        category.setStatus(1);
                        productCategoryService.add(category);
                    }
                    categoryId = category.getId();
                }
                
                // Handle Model
                Long modelId = null;
                if (StrUtil.isNotBlank(modelName)) {
                    QueryWrapper modelQw = QueryWrapper.create().where(PRODUCT_MODEL_ENTITY.MODEL_NAME.eq(modelName));
                    if (brandId != null) {
                        modelQw.and(PRODUCT_MODEL_ENTITY.BRAND_ID.eq(brandId));
                    }
                    ProductModelEntity model = productModelService.getOne(modelQw);
                    if (model == null) {
                        model = new ProductModelEntity();
                        model.setModelName(modelName);
                        model.setBrandId(brandId);
                        model.setBrandName(brandName);
                        model.setCategoryId(categoryId);
                        model.setCategoryName(categoryName);
                        model.setStatus(1);
                        model.setModelCode(CodeGenerator.generateCode("M", prefix -> {
                            ProductModelEntity max = productModelService.getOne(QueryWrapper.create()
                                .where(PRODUCT_MODEL_ENTITY.MODEL_CODE.like(prefix + "%"))
                                .orderBy(PRODUCT_MODEL_ENTITY.MODEL_CODE, false)
                                .limit(1));
                            return max != null ? max.getModelCode() : null;
                        }));
                        productModelService.add(model);
                    }
                    modelId = model.getId();
                }
                
                // Handle Product
                ProductEntity product = getBySku(sku);
                if (product == null) {
                    product = new ProductEntity();
                    product.setProductSku(sku);
                }
                product.setProductName(name);
                product.setBrandId(brandId);
                product.setBrand(brandName);
                product.setModelId(modelId);
                product.setModel(modelName);
                product.setCategoryId(categoryId);
                product.setCategory(categoryName);
                product.setUnit(getStr(row, "unit", "单位"));
                product.setSpecification(getStr(row, "specification", "规格"));
                product.setRemark(getStr(row, "remark", "备注"));
                product.setStatus(1);
                
                boolean result;
                if (product.getId() == null) {
                    Long id = this.add(product);
                    result = id != null;
                } else {
                    result = this.update(product);
                }
                if (!result) {
                    throw new RuntimeException("保存商品失败");
                }
                
                // Save Prices
                savePrice(sku, name, 1, getNum(row, "stateGridPrice", "国网价"), null, null);
                savePrice(sku, name, 4, getNum(row, "settlementPrice", "结算价"), null, null);
                savePrice(sku, name, 2, getNum(row, "defaultRegionPrice", "默认区域价"), 1, null);
                savePrice(sku, name, 3, getNum(row, "defaultProviderPrice", "默认服务商价"), 1, null);
                
                successCount++;

                
            } catch (Exception e) {
                failCount++;
                Map<String, Object> failItem = new HashMap<>();
                failItem.put("row", i + 2);
                failItem.put("sku", row.get("productSku"));
                failItem.put("reason", e.getMessage());
                failList.add(failItem);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", list.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failList", failList);
        return result;
    }

    private String getStr(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            if (row.containsKey(key) && row.get(key) != null) {
                return String.valueOf(row.get(key));
            }
        }
        return null;
    }

    private Number getNum(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            if (row.containsKey(key) && row.get(key) != null) {
                Object val = row.get(key);
                if (val instanceof Number) {
                    return (Number) val;
                }
                try {
                    return new java.math.BigDecimal(val.toString());
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return null;
    }

    @Override
    public void exportProducts(JSONObject requestParams, HttpServletResponse response) throws IOException {
        QueryWrapper queryWrapper = QueryWrapper.create();
        // Simple filtering if needed
        if (requestParams.containsKey("keyWord")) {
            String keyWord = requestParams.getStr("keyWord");
            if (StrUtil.isNotBlank(keyWord)) {
                queryWrapper.where(PRODUCT_ENTITY.PRODUCT_NAME.like(keyWord))
                    .or(PRODUCT_ENTITY.PRODUCT_SKU.like(keyWord));
            }
        }
        
        List<ProductEntity> list = this.list(queryWrapper);
        
        // Fetch prices
        List<String> skus = list.stream().map(ProductEntity::getProductSku).filter(StrUtil::isNotBlank).collect(java.util.stream.Collectors.toList());
        Map<String, Map<String, java.math.BigDecimal>> priceMap = new HashMap<>();
        
        if (!skus.isEmpty()) {
            List<com.cool.modules.config.entity.PriceConfigEntity> prices = priceConfigService.list(QueryWrapper.create()
                .in(com.cool.modules.config.entity.PriceConfigEntity::getProductSku, skus));
                
            for (com.cool.modules.config.entity.PriceConfigEntity p : prices) {
                String sku = p.getProductSku();
                priceMap.putIfAbsent(sku, new HashMap<>());
                Map<String, java.math.BigDecimal> pm = priceMap.get(sku);
                
                if (p.getPriceType() == 1) {
                    pm.put("stateGridPrice", p.getPrice());
                } else if (p.getPriceType() == 4) {
                    pm.put("settlementPrice", p.getPrice());
                } else if (p.getPriceType() == 2 && Integer.valueOf(1).equals(p.getIsDefault())) {
                    pm.put("defaultRegionPrice", p.getPrice());
                } else if (p.getPriceType() == 3 && Integer.valueOf(1).equals(p.getIsDefault())) {
                    pm.put("defaultProviderPrice", p.getPrice());
                }
            }
        }
        
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.addHeaderAlias("productSku", "商品SKU");
        writer.addHeaderAlias("productName", "商品名称");
        writer.addHeaderAlias("brand", "品牌");
        writer.addHeaderAlias("model", "型号");
        writer.addHeaderAlias("category", "商品类别");
        writer.addHeaderAlias("unit", "单位");
        writer.addHeaderAlias("specification", "规格");
        writer.addHeaderAlias("remark", "备注");
        writer.addHeaderAlias("stateGridPrice", "国网价");
        writer.addHeaderAlias("settlementPrice", "结算价");
        writer.addHeaderAlias("defaultRegionPrice", "默认区域价");
        writer.addHeaderAlias("defaultProviderPrice", "默认服务商价");
        writer.addHeaderAlias("createTime", "创建时间");
        
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ProductEntity p : list) {
            Map<String, Object> row = new HashMap<>();
            row.put("productSku", p.getProductSku());
            row.put("productName", p.getProductName());
            row.put("brand", p.getBrand());
            row.put("model", p.getModel());
            row.put("category", p.getCategory());
            row.put("unit", p.getUnit());
            row.put("specification", p.getSpecification());
            row.put("remark", p.getRemark());
            row.put("createTime", p.getCreateTime());
            
            Map<String, java.math.BigDecimal> pm = priceMap.get(p.getProductSku());
            if (pm != null) {
                row.put("stateGridPrice", pm.get("stateGridPrice"));
                row.put("settlementPrice", pm.get("settlementPrice"));
                row.put("defaultRegionPrice", pm.get("defaultRegionPrice"));
                row.put("defaultProviderPrice", pm.get("defaultProviderPrice"));
            }
            
            rows.add(row);
        }
        
        writer.write(rows, true);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=products.xlsx");
        writer.flush(response.getOutputStream(), true);
        writer.close();
    }

    private void savePrice(String sku, String productName, Integer priceType, Number priceVal, Integer isDefault, Long platformId) {
        if (priceVal == null) return;
        java.math.BigDecimal price = new java.math.BigDecimal(priceVal.toString());
        
        QueryWrapper qw = QueryWrapper.create()
            .where(com.cool.modules.config.entity.table.PriceConfigEntityTableDef.PRICE_CONFIG_ENTITY.PRODUCT_SKU.eq(sku))
            .and(com.cool.modules.config.entity.table.PriceConfigEntityTableDef.PRICE_CONFIG_ENTITY.PRICE_TYPE.eq(priceType));
            
        if (isDefault != null) {
            qw.and(com.cool.modules.config.entity.table.PriceConfigEntityTableDef.PRICE_CONFIG_ENTITY.IS_DEFAULT.eq(isDefault));
        }
        
        com.cool.modules.config.entity.PriceConfigEntity entity = priceConfigService.getOne(qw);
        if (entity == null) {
            entity = new com.cool.modules.config.entity.PriceConfigEntity();
            entity.setProductSku(sku);
            entity.setPriceType(priceType);
            entity.setIsDefault(isDefault);
            entity.setStatus(1);
        }
        
        entity.setProductName(productName);
        entity.setPrice(price);
        
        // Set redundant fields based on type
        if (priceType == 1) entity.setStateGridPrice(price);
        else if (priceType == 2) entity.setRegionalPrice(price);
        else if (priceType == 3) entity.setProviderPrice(price);
        else if (priceType == 4) entity.setSettlementPrice(price);
        
        if (entity.getId() == null) {
            priceConfigService.add(entity);
        } else {
            priceConfigService.update(entity);
        }
    }
}
