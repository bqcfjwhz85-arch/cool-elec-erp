package com.cool.modules.sale.service;

import com.cool.core.base.BaseService;
import com.cool.modules.sale.entity.SalesInvoiceEntity;

import java.util.List;

/**
 * 销售发票信息服务接口
 */
public interface SalesInvoiceService extends BaseService<SalesInvoiceEntity> {
    
    /**
     * 根据订单ID获取发票信息列表
     * 
     * @param orderId 订单ID
     * @return 发票信息列表
     */
    List<SalesInvoiceEntity> listByOrderId(Long orderId);
    
    /**
     * 删除发票信息（需要校验未关联付款）
     * 
     * @param id 发票信息ID
     * @param reason 删除原因
     */
    void deleteWithReason(Long id, String reason);
}






















