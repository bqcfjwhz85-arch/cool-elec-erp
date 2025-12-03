package com.cool.modules.config.service.impl;

import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.base.ModifyEnum;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.config.entity.BrandEntity;
import com.cool.modules.config.entity.PointConfigEntity;
import com.cool.modules.config.entity.ProductCategoryEntity;
import com.cool.modules.config.entity.ProductModelEntity;
import com.cool.modules.config.entity.PlatformEntity;
import com.cool.modules.config.entity.ProviderEntity;
import com.cool.modules.config.mapper.PointConfigMapper;
import com.cool.modules.config.service.BrandService;
import com.cool.modules.config.service.PointConfigService;
import com.cool.modules.config.service.ProductCategoryService;
import com.cool.modules.config.service.ProductModelService;
import com.cool.modules.config.service.PlatformService;
import com.cool.modules.config.service.ProviderService;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.cool.modules.config.entity.table.PointConfigEntityTableDef.POINT_CONFIG_ENTITY;

/**
 * 点位配置服务实现类
 */
@Service
public class PointConfigServiceImpl extends BaseServiceImpl<PointConfigMapper, PointConfigEntity> 
        implements PointConfigService {
    
    private final BrandService brandService;
    private final ProductModelService productModelService;
    private final ProductCategoryService productCategoryService;
    private final PlatformService platformService;
    private final ProviderService providerService;
    
    // 使用构造器注入，@Lazy 打破循环依赖
    public PointConfigServiceImpl(@Lazy BrandService brandService,
                                 @Lazy ProductModelService productModelService,
                                 @Lazy ProductCategoryService productCategoryService,
                                 @Lazy PlatformService platformService,
                                 @Lazy ProviderService providerService) {
        this.brandService = brandService;
        this.productModelService = productModelService;
        this.productCategoryService = productCategoryService;
        this.platformService = platformService;
        this.providerService = providerService;
    }
    
    @Override
    public void modifyBefore(JSONObject requestParams, PointConfigEntity entity, ModifyEnum type) {
        // 新增或修改时填充品牌、型号和类别名称
        if (type == ModifyEnum.ADD || type == ModifyEnum.UPDATE) {
            // 设置默认状态：如果没有传status，默认为启用
            if (entity.getStatus() == null) {
                entity.setStatus(1);
            }
            
            // 填充品牌名称
            if (entity.getBrandId() != null) {
                BrandEntity brand = brandService.getById(entity.getBrandId());
                if (brand != null) {
                    entity.setBrand(brand.getBrandName());
                }
            }
            
            // 填充型号名称
            if (entity.getModelId() != null) {
                ProductModelEntity model = productModelService.getById(entity.getModelId());
                if (model != null) {
                    entity.setModel(model.getModelName());
                }
            }
            
            // 填充类别名称
            if (entity.getCategoryId() != null) {
                ProductCategoryEntity category = productCategoryService.getById(entity.getCategoryId());
                if (category != null) {
                    entity.setCategoryName(category.getCategoryName());
                }
            }

            // 填充平台名称
            if (entity.getPlatformId() != null) {
                PlatformEntity platform = platformService.getById(entity.getPlatformId());
                if (platform != null) {
                    entity.setPlatformName(platform.getPlatformName());
                }
            }

            // 填充服务商名称
            if (entity.getProviderId() != null) {
                ProviderEntity provider = providerService.getById(entity.getProviderId());
                if (provider != null) {
                    entity.setProviderName(provider.getProviderName());
                }
            }
        }
    }
    
    @Override
    public BigDecimal getPoints(Integer configType, Long platformId, Long providerId, 
                                String regionCode, String brand, String model) {
        // 根据优先级查询点位配置
        QueryWrapper qw = QueryWrapper.create()
            .where(POINT_CONFIG_ENTITY.CONFIG_TYPE.eq(configType))
            .and(POINT_CONFIG_ENTITY.STATUS.eq(1));
        
        if (configType == 1 && platformId != null) {
            qw.and(POINT_CONFIG_ENTITY.PLATFORM_ID.eq(platformId));
        } else if (configType == 2 && providerId != null) {
            qw.and(POINT_CONFIG_ENTITY.PROVIDER_ID.eq(providerId));
        }
        
        // 按优先级排序：精确匹配优先
        qw.orderBy(POINT_CONFIG_ENTITY.PRIORITY.desc());
        
        PointConfigEntity config = getOne(qw);
        CoolPreconditions.check(config == null, "未找到点位配置");
        return config.getPoints();
    }
}

