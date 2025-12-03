package com.cool.modules.config.service;

import com.cool.core.base.BaseService;
import com.cool.modules.config.entity.BrandEntity;

/**
 * 品牌信息服务接口
 */
public interface BrandService extends BaseService<BrandEntity> {
    
    /**
     * 根据品牌名称获取品牌信息
     * @param brandName 品牌名称
     * @return 品牌信息
     */
    BrandEntity getByName(String brandName);
    
    /**
     * 根据品牌编码获取品牌信息
     * @param brandCode 品牌编码
     * @return 品牌信息
     */
    BrandEntity getByCode(String brandCode);
}




