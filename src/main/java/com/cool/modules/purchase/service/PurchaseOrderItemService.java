package com.cool.modules.purchase.service;

import com.cool.core.base.BaseService;
import com.cool.modules.purchase.entity.PurchaseOrderItemEntity;

import java.util.List;

/**
 * 采购订单商品明细服务接口
 */
public interface PurchaseOrderItemService extends BaseService<PurchaseOrderItemEntity> {
    
    /**
     * 根据订单ID查询商品明细列表
     * 
     * @param orderId 订单ID
     * @return 商品明细列表
     */
    List<PurchaseOrderItemEntity> listByOrderId(Long orderId);
    
    /**
     * 批量删除订单明细
     * 
     * @param orderId 订单ID
     */
    void deleteByOrderId(Long orderId);
}






