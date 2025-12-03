package com.cool.modules.config.service;

import com.cool.core.base.BaseService;
import com.cool.modules.config.entity.PointConfigEntity;

import java.math.BigDecimal;

/**
 * 点位配置服务接口
 */
public interface PointConfigService extends BaseService<PointConfigEntity> {
    
    /**
     * 获取点位
     * 
     * @param configType 配置类型
     * @param platformId 平台ID
     * @param providerId 服务商ID
     * @param regionCode 区域代码
     * @param brand 品牌
     * @param model 型号
     * @return 点位
     */
    BigDecimal getPoints(Integer configType, Long platformId, Long providerId, 
                         String regionCode, String brand, String model);
}

