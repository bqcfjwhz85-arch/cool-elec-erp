package com.cool.modules.purchase.service;

import com.cool.core.base.BaseService;
import com.cool.modules.purchase.entity.PurchaseInvoiceEntity;

import java.util.List;

/**
 * 采购发票服务接口
 */
public interface PurchaseInvoiceService extends BaseService<PurchaseInvoiceEntity> {
    
    /**
     * 根据订单ID查询发票列表
     * 
     * @param orderId 订单ID
     * @return 发票列表
     */
    List<PurchaseInvoiceEntity> listByOrderId(Long orderId);
    
    /**
     * 删除发票（需要填写原因）
     * 
     * @param id 发票ID
     * @param reason 删除原因
     */
    void deleteWithReason(Long id, String reason);

    /**
     * 变更发票状态
     *
     * @param id 发票ID
     * @param status 状态：0-待开票 1-已开票 2-已作废
     */
    void changeStatus(Long id, Integer status);
}






