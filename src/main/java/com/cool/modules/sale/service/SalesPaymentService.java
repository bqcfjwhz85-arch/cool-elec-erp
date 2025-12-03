package com.cool.modules.sale.service;

import com.cool.core.base.BaseService;
import com.cool.modules.sale.entity.SalesPaymentEntity;

import java.util.List;

/**
 * 回款信息服务接口
 */
public interface SalesPaymentService extends BaseService<SalesPaymentEntity> {
    
    /**
     * 根据订单ID获取回款信息列表
     * 
     * @param orderId 订单ID
     * @return 回款信息列表
     */
    List<SalesPaymentEntity> listByOrderId(Long orderId);
    
    /**
     * 删除回款信息（需要填写原因）
     * 
     * @param id 回款信息ID
     * @param reason 删除原因
     */
    void deleteWithReason(Long id, String reason);
    /**
     * 批量回款
     * 
     * @param orderIds 订单ID列表
     * @param paymentMethod 回款方式
     * @param paymentDate 回款日期
     * @param paymentAccount 回款账户
     * @param transactionNo 交易流水号
     * @param voucherUrl 回款凭证URL
     * @param remark 备注
     */
    void batchPayment(List<Long> orderIds, Integer paymentMethod, String paymentDate, 
                     String paymentAccount, String transactionNo, String voucherUrl, String remark, Boolean confirm);
}






















