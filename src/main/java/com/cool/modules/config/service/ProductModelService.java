package com.cool.modules.config.service;

import com.cool.core.base.BaseService;
import com.cool.modules.config.entity.ProductModelEntity;

import java.util.List;

/**
 * 商品型号服务接口
 */
public interface ProductModelService extends BaseService<ProductModelEntity> {
    
    /**
     * 根据型号编码获取型号信息
     * @param modelCode 型号编码
     * @return 型号信息
     */
    ProductModelEntity getByCode(String modelCode);
    
    /**
     * 根据品牌ID获取型号列表
     * @param brandId 品牌ID
     * @return 型号列表
     */
    List<ProductModelEntity> listByBrandId(Long brandId);
    
    /**
     * 模糊搜索型号（支持型号名称、型号编码）
     * @param keyword 关键词
     * @return 型号列表
     */
    List<ProductModelEntity> fuzzySearch(String keyword);
}




