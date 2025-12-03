package com.cool.modules.config.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.base.ModifyEnum;
import com.cool.core.exception.CoolPreconditions;
import com.cool.core.util.CodeGenerator;
import com.cool.modules.config.entity.BrandEntity;
import com.cool.modules.config.entity.ProductCategoryEntity;
import com.cool.modules.config.entity.ProductModelEntity;
import com.cool.modules.config.mapper.ProductModelMapper;
import com.cool.modules.config.service.BrandService;
import com.cool.modules.config.service.PointConfigService;
import com.cool.modules.config.service.ProductCategoryService;
import com.cool.modules.config.service.ProductModelService;
import com.cool.modules.product.service.ProductService;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.cool.modules.config.entity.table.PointConfigEntityTableDef.POINT_CONFIG_ENTITY;
import static com.cool.modules.config.entity.table.ProductEntityTableDef.PRODUCT_ENTITY;
import static com.cool.modules.config.entity.table.ProductModelEntityTableDef.PRODUCT_MODEL_ENTITY;

/**
 * 商品型号服务实现类
 */
@Service
public class ProductModelServiceImpl extends BaseServiceImpl<ProductModelMapper, ProductModelEntity> 
        implements ProductModelService {

    private final BrandService brandService;
    private final ProductCategoryService productCategoryService;
    private final ProductService productService;
    private final PointConfigService pointConfigService;

    // 使用构造器注入，@Lazy 打破循环依赖
    public ProductModelServiceImpl(@Lazy BrandService brandService,
                                  @Lazy ProductCategoryService productCategoryService,
                                  @Lazy ProductService productService,
                                  @Lazy PointConfigService pointConfigService) {
        this.brandService = brandService;
        this.productCategoryService = productCategoryService;
        this.productService = productService;
        this.pointConfigService = pointConfigService;
    }

    @Override
    public void modifyBefore(JSONObject requestParams, ProductModelEntity entity, ModifyEnum type) {
        // 删除前检查是否被引用
        if (type == ModifyEnum.DELETE) {
            Long modelId = entity.getId();
            
            // 1. 检查商品表是否引用
            long productCount = productService.count(QueryWrapper.create()
                .where(PRODUCT_ENTITY.MODEL_ID.eq(modelId)));
            CoolPreconditions.check(productCount > 0, 
                "该型号已被 " + productCount + " 个商品引用，无法删除。请先删除相关商品或修改商品的型号");
            
            // 2. 检查点位配置表是否引用
            long pointConfigCount = pointConfigService.count(QueryWrapper.create()
                .where(POINT_CONFIG_ENTITY.MODEL_ID.eq(modelId)));
            CoolPreconditions.check(pointConfigCount > 0, 
                "该型号已被 " + pointConfigCount + " 个点位配置引用，无法删除。请先删除相关点位配置或修改点位配置的型号");
        }
        
        // 新增时自动生成型号编码（如果前端未提供）
        if (type == ModifyEnum.ADD && StrUtil.isBlank(entity.getModelCode())) {
            String autoCode = CodeGenerator.generateCode("MDL", todayPrefix -> {
                // 查询当天该前缀的最大编码
                ProductModelEntity maxModel = getOne(QueryWrapper.create()
                    .where(PRODUCT_MODEL_ENTITY.MODEL_CODE.like(todayPrefix + "%"))
                    .orderBy(PRODUCT_MODEL_ENTITY.MODEL_CODE, false)
                    .limit(1));
                return maxModel != null ? maxModel.getModelCode() : null;
            });
            entity.setModelCode(autoCode);
        }
        
        // 新增或修改时填充品牌名称和类别名称
        if (type == ModifyEnum.ADD || type == ModifyEnum.UPDATE) {
            // 填充品牌名称
            if (entity.getBrandId() != null) {
                BrandEntity brand = brandService.getById(entity.getBrandId());
                if (brand != null) {
                    entity.setBrandName(brand.getBrandName());
                }
            }
            
            // 填充类别名称
            if (entity.getCategoryId() != null) {
                ProductCategoryEntity category = productCategoryService.getById(entity.getCategoryId());
                if (category != null) {
                    entity.setCategoryName(category.getCategoryName());
                }
            }
        }
    }

    @Override
    public ProductModelEntity getByCode(String modelCode) {
        return getOne(QueryWrapper.create()
            .where(PRODUCT_MODEL_ENTITY.MODEL_CODE.eq(modelCode)));
    }

    @Override
    public List<ProductModelEntity> listByBrandId(Long brandId) {
        return list(QueryWrapper.create()
            .where(PRODUCT_MODEL_ENTITY.BRAND_ID.eq(brandId))
            .and(PRODUCT_MODEL_ENTITY.STATUS.eq(1))
            .orderBy(PRODUCT_MODEL_ENTITY.SORT_ORDER, true)
            .orderBy(PRODUCT_MODEL_ENTITY.MODEL_NAME, true));
    }

    @Override
    public List<ProductModelEntity> fuzzySearch(String keyword) {
        return list(QueryWrapper.create()
            .where(PRODUCT_MODEL_ENTITY.MODEL_NAME.like(keyword)
                .or(PRODUCT_MODEL_ENTITY.MODEL_CODE.like(keyword)))
            .and(PRODUCT_MODEL_ENTITY.STATUS.eq(1))
            .orderBy(PRODUCT_MODEL_ENTITY.SORT_ORDER, true)
            .orderBy(PRODUCT_MODEL_ENTITY.MODEL_NAME, true));
    }
}

