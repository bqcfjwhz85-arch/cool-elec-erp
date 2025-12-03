package com.cool.modules.sale.service;

import com.cool.core.base.BaseService;
import com.cool.modules.sale.entity.SalesDeliveryEntity;

import java.util.List;

/**
 * 发货信息服务接口
 */
public interface SalesDeliveryService extends BaseService<SalesDeliveryEntity> {
    
    /**
     * 根据订单ID获取发货信息列表
     * 
     * @param orderId 订单ID
     * @return 发货信息列表
     */
    List<SalesDeliveryEntity> listByOrderId(Long orderId);
    
    /**
     * 删除发货信息(需要填写原因)
     * 
     * @param id 发货信息ID
     * @param reason 删除原因
     */
    void deleteWithReason(Long id, String reason);
    
    /**
     * 新增发货信息并保存明细
     * 
     * @param entity 发货信息实体
     * @param details 发货明细列表
     * @return 保存结果
     */
    Object addWithDetails(SalesDeliveryEntity entity, Object details);
    
    /**
     * 获取发货信息(包含明细)
     * 
     * @param id 发货ID
     * @return 发货信息Map,包含details字段
     */
    java.util.Map<String, Object> getInfoWithDetails(Long id);
    
    /**
     * 更新发货信息并保存明细
     * 
     * @param entity 发货信息实体
     * @param details 发货明细列表
     * @return 更新结果
     */
    Object updateWithDetails(SalesDeliveryEntity entity, Object details);
}






















