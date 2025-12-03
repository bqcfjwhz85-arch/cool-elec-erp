package com.cool.modules.config.service;

import com.cool.core.base.BaseService;
import com.cool.modules.config.entity.ProductCategoryEntity;

import java.util.List;

/**
 * 商品类别服务接口
 */
public interface ProductCategoryService extends BaseService<ProductCategoryEntity> {
    
    /**
     * 根据类别编码获取类别信息
     * @param categoryCode 类别编码
     * @return 类别信息
     */
    ProductCategoryEntity getByCode(String categoryCode);
    
    /**
     * 根据类别名称获取类别信息
     * @param categoryName 类别名称
     * @return 类别信息
     */
    ProductCategoryEntity getByName(String categoryName);
    
    /**
     * 根据父级ID获取子类别列表
     * @param parentId 父级ID
     * @return 子类别列表
     */
    List<ProductCategoryEntity> listByParentId(Long parentId);
    
    /**
     * 获取树形结构类别列表
     * @return 树形结构列表
     */
    List<ProductCategoryEntity> getTreeList();
}




