package com.cool.modules.config.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.base.ModifyEnum;
import com.cool.core.exception.CoolPreconditions;
import com.cool.core.util.CodeGenerator;
import com.cool.modules.config.entity.BrandEntity;
import com.cool.modules.config.mapper.BrandMapper;
import com.cool.modules.config.service.BrandService;
import com.cool.modules.config.service.PointConfigService;
import com.cool.modules.config.service.ProductModelService;
import com.cool.modules.product.service.ProductService;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import static com.cool.modules.config.entity.table.BrandEntityTableDef.BRAND_ENTITY;
import static com.cool.modules.config.entity.table.PointConfigEntityTableDef.POINT_CONFIG_ENTITY;
import static com.cool.modules.config.entity.table.ProductEntityTableDef.PRODUCT_ENTITY;
import static com.cool.modules.config.entity.table.ProductModelEntityTableDef.PRODUCT_MODEL_ENTITY;

/**
 * 品牌信息服务实现类
 */
@Service
public class BrandServiceImpl extends BaseServiceImpl<BrandMapper, BrandEntity> implements BrandService {

    private final ProductModelService productModelService;
    private final ProductService productService;
    private final PointConfigService pointConfigService;

    // 使用构造器注入，@Lazy 打破循环依赖
    public BrandServiceImpl(@Lazy ProductModelService productModelService,
                           @Lazy ProductService productService,
                           @Lazy PointConfigService pointConfigService) {
        this.productModelService = productModelService;
        this.productService = productService;
        this.pointConfigService = pointConfigService;
    }

    @Override
    public void modifyBefore(JSONObject requestParams, BrandEntity entity, ModifyEnum type) {
        // 删除前检查是否被引用
        if (type == ModifyEnum.DELETE) {
            Long brandId = entity.getId();
            
            // 1. 检查型号表是否引用
            long modelCount = productModelService.count(QueryWrapper.create()
                .where(PRODUCT_MODEL_ENTITY.BRAND_ID.eq(brandId)));
            CoolPreconditions.check(modelCount > 0, 
                "该品牌已被 " + modelCount + " 个型号引用，无法删除。请先删除相关型号或修改型号的品牌");
            
            // 2. 检查商品表是否引用
            long productCount = productService.count(QueryWrapper.create()
                .where(PRODUCT_ENTITY.BRAND_ID.eq(brandId)));
            CoolPreconditions.check(productCount > 0, 
                "该品牌已被 " + productCount + " 个商品引用，无法删除。请先删除相关商品或修改商品的品牌");
            
            // 3. 检查点位配置表是否引用
            long pointConfigCount = pointConfigService.count(QueryWrapper.create()
                .where(POINT_CONFIG_ENTITY.BRAND_ID.eq(brandId)));
            CoolPreconditions.check(pointConfigCount > 0, 
                "该品牌已被 " + pointConfigCount + " 个点位配置引用，无法删除。请先删除相关点位配置或修改点位配置的品牌");
        }
        
        // 新增/修改前处理
        if (type == ModifyEnum.ADD || type == ModifyEnum.UPDATE) {
            // 设置默认状态：如果没有传status，默认为启用
            if (entity.getStatus() == null) {
                entity.setStatus(1);
            }
        }
        
        // 新增时自动生成品牌编码（如果前端未提供）
        if (type == ModifyEnum.ADD && StrUtil.isBlank(entity.getBrandCode())) {
            String autoCode = CodeGenerator.generateCode("BR", todayPrefix -> {
                // 查询当天该前缀的最大编码
                BrandEntity maxBrand = getOne(QueryWrapper.create()
                    .where(BRAND_ENTITY.BRAND_CODE.like(todayPrefix + "%"))
                    .orderBy(BRAND_ENTITY.BRAND_CODE, false)
                    .limit(1));
                return maxBrand != null ? maxBrand.getBrandCode() : null;
            });
            entity.setBrandCode(autoCode);
        }
    }

    @Override
    public BrandEntity getByName(String brandName) {
        return getOne(QueryWrapper.create()
            .where(BRAND_ENTITY.BRAND_NAME.eq(brandName)));
    }

    @Override
    public BrandEntity getByCode(String brandCode) {
        return getOne(QueryWrapper.create()
            .where(BRAND_ENTITY.BRAND_CODE.eq(brandCode)));
    }
}




