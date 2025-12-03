package com.cool.modules.config.service;

import com.cool.core.base.BaseService;
import com.cool.modules.config.entity.PriceConfigEntity;

import java.math.BigDecimal;

/**
 * 价格配置服务接口
 */
public interface PriceConfigService extends BaseService<PriceConfigEntity> {
    
    /**
     * 获取商品价格
     * 
     * @param productSku 商品SKU
     * @param priceType 价格类型：1-国网价 2-区域价 3-服务商价 4-结算价
     * @param regionCode 区域代码（可选）
     * @param providerId 服务商ID（可选）
     * @return 价格
     */
    BigDecimal getPrice(String productSku, Integer priceType, String regionCode, Long providerId);
    
    /**
     * 批量调整价格
     * 
     * @param category 商品类别
     * @param regionCode 区域代码
     * @param adjustRate 调整比例
     */
    void batchAdjustPrice(String category, String regionCode, BigDecimal adjustRate);
}

