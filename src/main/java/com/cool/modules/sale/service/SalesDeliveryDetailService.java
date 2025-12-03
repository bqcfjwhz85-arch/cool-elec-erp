package com.cool.modules.sale.service;

import com.cool.core.base.BaseService;
import com.cool.modules.sale.entity.SalesDeliveryDetailEntity;

import java.util.List;

/**
 * 销售订单发货明细服务接口
 */
public interface SalesDeliveryDetailService extends BaseService<SalesDeliveryDetailEntity> {
    
    /**
     * 根据发货ID查询明细列表
     * @param deliveryId 发货ID
     * @return 明细列表
     */
    List<SalesDeliveryDetailEntity> listByDeliveryId(Long deliveryId);
    
    /**
     * 根据发货ID删除明细
     * @param deliveryId 发货ID
     */
    void deleteByDeliveryId(Long deliveryId);
}
