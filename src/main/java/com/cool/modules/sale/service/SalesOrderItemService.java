package com.cool.modules.sale.service;

import com.cool.core.base.BaseService;
import com.cool.modules.sale.entity.SalesOrderItemEntity;

import java.util.List;

/**
 * 销售订单商品明细服务接口
 */
public interface SalesOrderItemService extends BaseService<SalesOrderItemEntity> {
    
    /**
     * 根据订单ID获取商品明细
     * 
     * @param orderId 订单ID
     * @return 商品明细列表
     */
    List<SalesOrderItemEntity> listByOrderId(Long orderId);
    
    /**
     * 批量保存商品明细
     * 
     * @param orderId 订单ID
     * @param items 商品明细列表
     */
    void batchSave(Long orderId, List<SalesOrderItemEntity> items);
}






















