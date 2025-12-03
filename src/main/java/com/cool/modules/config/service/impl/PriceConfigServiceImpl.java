package com.cool.modules.config.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.base.ModifyEnum;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.config.entity.PriceConfigEntity;
import com.cool.modules.config.entity.ProductEntity;
import com.cool.modules.config.entity.PlatformEntity;
import com.cool.modules.config.mapper.PriceConfigMapper;
import com.cool.modules.config.mapper.ProductMapper;
import com.cool.modules.config.mapper.PlatformMapper;
import com.cool.modules.config.service.PriceConfigService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cool.modules.config.entity.table.PriceConfigEntityTableDef.PRICE_CONFIG_ENTITY;
import static com.cool.modules.config.entity.table.ProductEntityTableDef.PRODUCT_ENTITY;
import static com.cool.modules.config.entity.table.PlatformEntityTableDef.PLATFORM_ENTITY;

/**
 * 价格配置服务实现类
 */
@Service
@RequiredArgsConstructor
public class PriceConfigServiceImpl extends BaseServiceImpl<PriceConfigMapper, PriceConfigEntity> 
        implements PriceConfigService {
    
    private final ProductMapper productMapper;
    private final PlatformMapper platformMapper;
    
    @Override
    public BigDecimal getPrice(String productSku, Integer priceType, String regionCode, Long providerId) {
        CoolPreconditions.check(StrUtil.isBlank(productSku), "商品SKU不能为空");
        CoolPreconditions.check(priceType == null, "价格类型不能为空");
        
        PriceConfigEntity config = null;
        
        // 区域价需要按优先级查询
        if (priceType == 2 && StrUtil.isNotBlank(regionCode)) {
            config = getRegionalPrice(productSku, regionCode, null);
        } 
        // 服务商价查询
        else if (priceType == 3) {
            config = getProviderPrice(productSku, providerId);
        } 
        // 国网价和结算价直接查询
        else {
            QueryWrapper qw = QueryWrapper.create()
                .from(PRICE_CONFIG_ENTITY)
                .where(PRICE_CONFIG_ENTITY.PRODUCT_SKU.eq(productSku))
                .and(PRICE_CONFIG_ENTITY.PRICE_TYPE.eq(priceType))
                .and(PRICE_CONFIG_ENTITY.STATUS.eq(1))
                .and(PRICE_CONFIG_ENTITY.EFFECTIVE_TIME.le(LocalDateTime.now()))
                .and(PRICE_CONFIG_ENTITY.EXPIRY_TIME.ge(LocalDateTime.now())
                    .or(PRICE_CONFIG_ENTITY.EXPIRY_TIME.isNull()))
                .orderBy(PRICE_CONFIG_ENTITY.CREATE_TIME.desc())
                .limit(1);
            
            config = getOne(qw);
        }
        
        CoolPreconditions.check(config == null, "未找到有效价格配置");
        return config.getPrice();
    }
    
    /**
     * 获取区域价（支持优先级匹配）
     * 优先级：平台+省份 > 省份通用 > 默认价格
     */
    private PriceConfigEntity getRegionalPrice(String productSku, String province, Long platformId) {
        QueryWrapper qw = QueryWrapper.create()
            .from(PRICE_CONFIG_ENTITY)
            .where(PRICE_CONFIG_ENTITY.PRODUCT_SKU.eq(productSku))
            .and(PRICE_CONFIG_ENTITY.PRICE_TYPE.eq(2))
            .and(PRICE_CONFIG_ENTITY.STATUS.eq(1))
            .and(PRICE_CONFIG_ENTITY.EFFECTIVE_TIME.le(LocalDateTime.now()))
            .and(PRICE_CONFIG_ENTITY.EXPIRY_TIME.ge(LocalDateTime.now())
                .or(PRICE_CONFIG_ENTITY.EXPIRY_TIME.isNull()));
        
        if (StrUtil.isNotBlank(province)) {
            // 查询所有可能匹配的价格配置（包括精确匹配、省份通用和默认价格）
            qw.and(
                PRICE_CONFIG_ENTITY.PLATFORM_ID.eq(platformId).and(PRICE_CONFIG_ENTITY.PROVINCE.eq(province)).and(PRICE_CONFIG_ENTITY.IS_DEFAULT.eq(0))
                .or(PRICE_CONFIG_ENTITY.PLATFORM_ID.isNull().and(PRICE_CONFIG_ENTITY.PROVINCE.eq(province)).and(PRICE_CONFIG_ENTITY.IS_DEFAULT.eq(0)))
                .or(PRICE_CONFIG_ENTITY.IS_DEFAULT.eq(1))
            );
        } else {
            // 如果没有省份信息，只查询默认价格
            qw.and(PRICE_CONFIG_ENTITY.IS_DEFAULT.eq(1));
        }
        
        List<PriceConfigEntity> configs = list(qw);
        
        if (configs.isEmpty()) {
            return null;
        }
        
        // 按优先级排序并返回第一个
        for (PriceConfigEntity config : configs) {
            // 优先级1：精确匹配（平台+省份）
            if (platformId != null && platformId.equals(config.getPlatformId()) 
                && province.equals(config.getProvince()) && config.getIsDefault() == 0) {
                return config;
            }
        }
        
        for (PriceConfigEntity config : configs) {
            // 优先级2：省份通用（platformId为NULL+省份）
            if (config.getPlatformId() == null && province.equals(config.getProvince()) 
                && config.getIsDefault() == 0) {
                return config;
            }
        }
        
        // 优先级3：默认价格
        return configs.stream()
            .filter(c -> c.getIsDefault() == 1)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * 获取服务商价
     */
    private PriceConfigEntity getProviderPrice(String productSku, Long providerId) {
        QueryWrapper qw = QueryWrapper.create()
            .from(PRICE_CONFIG_ENTITY)
            .where(PRICE_CONFIG_ENTITY.PRODUCT_SKU.eq(productSku))
            .and(PRICE_CONFIG_ENTITY.PRICE_TYPE.eq(3))
            .and(PRICE_CONFIG_ENTITY.STATUS.eq(1))
            .and(PRICE_CONFIG_ENTITY.EFFECTIVE_TIME.le(LocalDateTime.now()))
            .and(PRICE_CONFIG_ENTITY.EXPIRY_TIME.ge(LocalDateTime.now())
                .or(PRICE_CONFIG_ENTITY.EXPIRY_TIME.isNull()));
        
        if (providerId != null) {
            // 先查询特殊服务商价
            qw.and(PRICE_CONFIG_ENTITY.PROVIDER_ID.eq(providerId))
              .and(PRICE_CONFIG_ENTITY.IS_DEFAULT.eq(0));
            
            PriceConfigEntity config = getOne(qw);
            if (config != null) {
                return config;
            }
        }
        
        // 如果没有找到特殊服务商价，返回默认服务商价
        qw = QueryWrapper.create()
            .from(PRICE_CONFIG_ENTITY)
            .where(PRICE_CONFIG_ENTITY.PRODUCT_SKU.eq(productSku))
            .and(PRICE_CONFIG_ENTITY.PRICE_TYPE.eq(3))
            .and(PRICE_CONFIG_ENTITY.IS_DEFAULT.eq(1))
            .and(PRICE_CONFIG_ENTITY.STATUS.eq(1))
            .and(PRICE_CONFIG_ENTITY.EFFECTIVE_TIME.le(LocalDateTime.now()))
            .and(PRICE_CONFIG_ENTITY.EXPIRY_TIME.ge(LocalDateTime.now())
                .or(PRICE_CONFIG_ENTITY.EXPIRY_TIME.isNull()))
            .limit(1);
        
        return getOne(qw);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAdjustPrice(String category, String regionCode, BigDecimal adjustRate) {
        CoolPreconditions.check(StrUtil.isBlank(regionCode), "区域代码不能为空");
        CoolPreconditions.check(adjustRate == null, "调整比例不能为空");
        
        // 查询需要调整的价格配置
        List<PriceConfigEntity> configs = list(QueryWrapper.create()
            .where(PRICE_CONFIG_ENTITY.PRICE_TYPE.eq(2))
            .and(PRICE_CONFIG_ENTITY.REGION_CODE.eq(regionCode))
            .and(PRICE_CONFIG_ENTITY.STATUS.eq(1)));
        
        configs.forEach(config -> {
            BigDecimal newPrice = config.getPrice()
                .multiply(BigDecimal.ONE.add(adjustRate))
                .setScale(2, RoundingMode.HALF_UP);
            config.setPrice(newPrice);
        });
        
        updateBatch(configs);
    }
    
    @Override
    public void modifyBefore(JSONObject requestParams, PriceConfigEntity entity, ModifyEnum type) {
        // 新增/修改前校验
        if (type == ModifyEnum.ADD || type == ModifyEnum.UPDATE) {
            // 根据productSku自动填充productName
            if (StrUtil.isNotBlank(entity.getProductSku())) {
                ProductEntity product = productMapper.selectOneByQuery(
                    QueryWrapper.create()
                        .select(PRODUCT_ENTITY.PRODUCT_NAME)
                        .where(PRODUCT_ENTITY.PRODUCT_SKU.eq(entity.getProductSku()))
                );
                if (product != null && StrUtil.isNotBlank(product.getProductName())) {
                    entity.setProductName(product.getProductName());
                }
            }
            
            // 根据platformId自动填充platformName
            if (entity.getPlatformId() != null) {
                PlatformEntity platform = platformMapper.selectOneByQuery(
                    QueryWrapper.create()
                        .select(PLATFORM_ENTITY.PLATFORM_NAME)
                        .where(PLATFORM_ENTITY.ID.eq(entity.getPlatformId()))
                );
                if (platform != null && StrUtil.isNotBlank(platform.getPlatformName())) {
                    entity.setPlatformName(platform.getPlatformName());
                }
            }
            
            // 填充冗余价格字段
            if (entity.getPrice() != null) {
                if (entity.getPriceType() == 1) {
                    entity.setStateGridPrice(entity.getPrice());
                } else if (entity.getPriceType() == 2) {
                    entity.setRegionalPrice(entity.getPrice());
                } else if (entity.getPriceType() == 3) {
                    entity.setProviderPrice(entity.getPrice());
                } else if (entity.getPriceType() == 4) {
                    entity.setSettlementPrice(entity.getPrice());
                }
            }
            
            // 设置默认值：生效时间默认为当前时间（即时生效）
            if (entity.getEffectiveTime() == null) {
                entity.setEffectiveTime(LocalDateTime.now());
            }
            
            // 设置默认状态：如果没有传status，默认为启用
            if (entity.getStatus() == null) {
                entity.setStatus(1);
            }
            
            // 设置isDefault默认值（国网价和结算价没有isDefault概念）
            if (entity.getPriceType() == 1 || entity.getPriceType() == 4) {
                entity.setIsDefault(null);
            } else if (entity.getIsDefault() == null) {
                // 区域价和服务商价，如果没有传isDefault，默认为0（特殊价格）
                entity.setIsDefault(0);
            }
            
            // 校验默认价格的唯一性
            if ((entity.getPriceType() == 2 || entity.getPriceType() == 3) && entity.getIsDefault() == 1) {
                QueryWrapper qw = QueryWrapper.create()
                    .where(PRICE_CONFIG_ENTITY.PRODUCT_SKU.eq(entity.getProductSku()))
                    .and(PRICE_CONFIG_ENTITY.PRICE_TYPE.eq(entity.getPriceType()))
                    .and(PRICE_CONFIG_ENTITY.IS_DEFAULT.eq(1));
                
                if (type == ModifyEnum.UPDATE) {
                    qw.and(PRICE_CONFIG_ENTITY.ID.ne(entity.getId()));
                }
                
                PriceConfigEntity exists = getOne(qw);
                String priceTypeName = entity.getPriceType() == 2 ? "区域价" : "服务商价";
                CoolPreconditions.check(exists != null, 
                    String.format("该商品的默认%s已存在，每个商品只能有一个默认%s", priceTypeName, priceTypeName));
            }
            
            // 校验特殊价格的唯一性
            if (entity.getPriceType() == 2 && entity.getIsDefault() == 0) {
                // 检查特殊区域价的唯一性（平台+省份组合）
                if (StrUtil.isNotBlank(entity.getProvince())) {
                    QueryWrapper qw = QueryWrapper.create()
                        .where(PRICE_CONFIG_ENTITY.PRODUCT_SKU.eq(entity.getProductSku()))
                        .and(PRICE_CONFIG_ENTITY.PRICE_TYPE.eq(2))
                        .and(PRICE_CONFIG_ENTITY.IS_DEFAULT.eq(0))
                        .and(PRICE_CONFIG_ENTITY.PROVINCE.eq(entity.getProvince()));
                    
                    // 处理platformId可能为NULL的情况
                    if (entity.getPlatformId() != null) {
                        qw.and(PRICE_CONFIG_ENTITY.PLATFORM_ID.eq(entity.getPlatformId()));
                    } else {
                        qw.and(PRICE_CONFIG_ENTITY.PLATFORM_ID.isNull());
                    }
                    
                    if (type == ModifyEnum.UPDATE) {
                        qw.and(PRICE_CONFIG_ENTITY.ID.ne(entity.getId()));
                    }
                    
                    PriceConfigEntity exists = getOne(qw);
                    if (exists != null) {
                        String locationDesc = entity.getPlatformId() != null 
                            ? entity.getPlatformName() + "+" + entity.getProvince()
                            : entity.getProvince() + "（全平台）";
                        CoolPreconditions.check(true, 
                            String.format("价格配置已存在：该商品在%s的价格已配置", locationDesc));
                    }
                }
            } else if (entity.getPriceType() == 3 && entity.getIsDefault() == 0) {
                // 检查特殊服务商价的唯一性
                if (entity.getProviderId() != null) {
                    QueryWrapper qw = QueryWrapper.create()
                        .where(PRICE_CONFIG_ENTITY.PRODUCT_SKU.eq(entity.getProductSku()))
                        .and(PRICE_CONFIG_ENTITY.PRICE_TYPE.eq(3))
                        .and(PRICE_CONFIG_ENTITY.IS_DEFAULT.eq(0))
                        .and(PRICE_CONFIG_ENTITY.PROVIDER_ID.eq(entity.getProviderId()));
                    
                    if (type == ModifyEnum.UPDATE) {
                        qw.and(PRICE_CONFIG_ENTITY.ID.ne(entity.getId()));
                    }
                    
                    PriceConfigEntity exists = getOne(qw);
                    CoolPreconditions.check(exists != null, 
                        String.format("价格配置已存在：该商品的服务商【%s】价格已配置", entity.getProviderName()));
                }
            }
        }
    }
    
    @Override
    public Object list(JSONObject requestParams, QueryWrapper queryWrapper) {
        // 调用父类list方法获取结果
        Object result = super.list(requestParams, queryWrapper);
        
        // 填充productName（处理历史数据）
        if (result instanceof List) {
            @SuppressWarnings("unchecked")
            List<PriceConfigEntity> list = (List<PriceConfigEntity>) result;
            fillProductName(list);
        }
        
        return result;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Object page(JSONObject requestParams, com.mybatisflex.core.paginate.Page<PriceConfigEntity> page, QueryWrapper queryWrapper) {
        // 调用父类page方法获取结果
        com.mybatisflex.core.paginate.Page<PriceConfigEntity> pageResult = 
            (com.mybatisflex.core.paginate.Page<PriceConfigEntity>) super.page(requestParams, page, queryWrapper);
        
        // 填充productName（处理历史数据）
        if (pageResult != null && pageResult.getRecords() != null) {
            fillProductName(pageResult.getRecords());
        }
        
        return pageResult;
    }
    
    /**
     * 填充商品名称
     */
    private void fillProductName(List<PriceConfigEntity> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        
        // 收集所有需要查询的productSku（productName为空的）
        List<String> skuList = list.stream()
            .filter(item -> StrUtil.isNotBlank(item.getProductSku()) && StrUtil.isBlank(item.getProductName()))
            .map(PriceConfigEntity::getProductSku)
            .distinct()
            .collect(Collectors.toList());
        
        if (skuList.isEmpty()) {
            return;
        }
        
        // 批量查询商品信息
        List<ProductEntity> products = productMapper.selectListByQuery(
            QueryWrapper.create()
                .select(PRODUCT_ENTITY.PRODUCT_SKU, PRODUCT_ENTITY.PRODUCT_NAME)
                .where(PRODUCT_ENTITY.PRODUCT_SKU.in(skuList))
        );
        
        // 构建SKU到商品名称的映射
        Map<String, String> skuNameMap = products.stream()
            .collect(Collectors.toMap(
                ProductEntity::getProductSku,
                ProductEntity::getProductName,
                (v1, v2) -> v1
            ));
        
        // 填充商品名称
        list.forEach(item -> {
            if (StrUtil.isNotBlank(item.getProductSku()) && StrUtil.isBlank(item.getProductName())) {
                String productName = skuNameMap.get(item.getProductSku());
                if (StrUtil.isNotBlank(productName)) {
                    item.setProductName(productName);
                }
            }
        });
    }
}

