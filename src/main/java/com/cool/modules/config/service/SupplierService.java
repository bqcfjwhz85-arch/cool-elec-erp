package com.cool.modules.config.service;

import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseService;
import com.cool.modules.config.entity.SupplierEntity;

import java.util.List;

/**
 * 供应商信息服务接口
 */
public interface SupplierService extends BaseService<SupplierEntity> {
    
    /**
     * 获取供应商关联的商品列表
     */
    JSONObject getGoods(Long supplierId, Integer page, Integer size, String keyword);
    
    /**
     * 批量关联商品到供应商
     */
    void addGoods(Long supplierId, List<Long> goodsIds);
    
    /**
     * 移除供应商的商品关联
     */
    void removeGoods(Long supplierId, Long goodsId);
}
