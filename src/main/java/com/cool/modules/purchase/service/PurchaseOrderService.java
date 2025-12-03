package com.cool.modules.purchase.service;

import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseService;
import com.cool.modules.purchase.entity.PurchaseOrderEntity;

import java.math.BigDecimal;

/**
 * 采购订单服务接口
 */
public interface PurchaseOrderService extends BaseService<PurchaseOrderEntity> {
    
    /**
     * 根据订单号获取订单
     * 
     * @param orderNo 订单号
     * @return 订单信息
     */
    PurchaseOrderEntity getByOrderNo(String orderNo);
    
    /**
     * 生成订单号
     * 
     * @return 订单号（PO+年月+6位流水）
     */
    String generateOrderNo();
    
    /**
     * 从销售订单生成采购订单（一键生成）
     * 
     * @param salesOrderId 销售订单ID
     * @param params 生成参数（供应商、点位等）
     * @return 采购订单
     */
    PurchaseOrderEntity generateFromSalesOrder(Long salesOrderId, JSONObject params);
    
    /**
     * 计算毛利
     * 
     * @param salesAmount 销售金额
     * @param purchaseAmount 采购金额
     * @return 毛利
     */
    BigDecimal calculateProfit(BigDecimal salesAmount, BigDecimal purchaseAmount);
    
    /**
     * 更新发票状态
     * 
     * @param orderId 订单ID
     */
    void updateInvoiceStatus(Long orderId);
    
    /**
     * 更新付款状态
     * 
     * @param orderId 订单ID
     */
    void updatePaymentStatus(Long orderId);
}






