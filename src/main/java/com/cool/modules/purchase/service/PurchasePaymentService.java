package com.cool.modules.purchase.service;

import com.cool.core.base.BaseService;
import com.cool.modules.purchase.entity.PurchasePaymentEntity;

import java.util.List;

/**
 * 采购付款服务接口
 */
public interface PurchasePaymentService extends BaseService<PurchasePaymentEntity> {

    /**
     * 根据订单ID查询付款列表
     *
     * @param orderId 订单ID
     * @return 付款列表
     */
    List<PurchasePaymentEntity> listByOrderId(Long orderId);

    /**
     * 删除付款信息（需填写原因）
     *
     * @param id     付款ID
     * @param reason 删除原因
     */
    void deleteWithReason(Long id, String reason);
}
