package com.cool.modules.sale.service;

import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseService;
import com.cool.modules.sale.entity.SalesOrderEntity;

import java.util.List;

/**
 * 销售订单服务接口
 */
public interface SalesOrderService extends BaseService<SalesOrderEntity> {
    
    /**
     * 根据订单号获取订单
     * 
     * @param orderNo 订单号
     * @return 订单信息
     */
    SalesOrderEntity getByOrderNo(String orderNo);
    
    /**
     * 生成订单号
     * 
     * @return 订单号
     */
    String generateOrderNo();
    
    /**
     * 从服务商报备生成销售订单
     * 
     * @param reportId 报备ID
     * @param params 生成参数（如点位等）
     * @return 销售订单
     */
    SalesOrderEntity generateFromReport(Long reportId, JSONObject params);

    /**
     * 批量从服务商报备生成销售订单
     *
     * @param reportIds 报备单ID列表
     * @param params    参数
     * @return 销售订单实体
     */
    SalesOrderEntity generateFromReports(List<Long> reportIds, JSONObject params);
    
    /**
     * 从平台订单生成销售订单
     * 
     * @param platformOrderId 平台订单ID
     * @return 销售订单
     */
    SalesOrderEntity generateFromPlatformOrder(Long platformOrderId);

    /**
     * 批量从平台订单生成销售订单
     *
     * @param platformOrderIds 平台订单ID列表
     * @param params           参数
     * @return 销售订单实体
     */
    SalesOrderEntity generateFromPlatformOrders(List<Long> platformOrderIds, JSONObject params);
    
    /**
     * 计算订单金额

    
    /**
     * 计算订单金额
     * 
     * @param order 订单信息
     * @return 订单金额
     */
    void calculateOrderAmount(SalesOrderEntity order);
    
    /**
     * 启动审批流程
     * 
     * @param orderId 订单ID
     */
    void startApproval(Long orderId);
    
    /**
     * 流转订单
     * 
     * @param orderId 订单ID
     * @param receiverId 接收人ID（手动模式需要）
     */
    void flowOrder(Long orderId, Long receiverId);
    
    /**
     * 更新发货状态
     * 
     * @param orderId 订单ID
     */
    void updateDeliveryStatus(Long orderId);
    
    /**
     * 更新开票状态
     * 
     * @param orderId 订单ID
     */
    void updateInvoiceStatus(Long orderId);
    
    /**
     * 更新回款状态
     * 
     * @param orderId 订单ID
     */
    void updatePaymentStatus(Long orderId);
}

