package com.cool.modules.source.service;

import com.cool.core.base.BaseService;
import com.cool.modules.source.entity.PlatformOrderItemEntity;

import java.util.List;

/**
 * 平台订单商品明细服务接口
 */
public interface PlatformOrderItemService extends BaseService<PlatformOrderItemEntity> {
    
    /**
     * 根据平台订单ID获取商品明细
     * 
     * @param platformOrderId 平台订单ID
     * @return 商品明细列表
     */
    List<PlatformOrderItemEntity> listByPlatformOrderId(Long platformOrderId);
    
    /**
     * 批量保存商品明细
     * 
     * @param platformOrderId 平台订单ID
     * @param items 商品明细列表
     */
    void batchSave(Long platformOrderId, List<PlatformOrderItemEntity> items);
}

