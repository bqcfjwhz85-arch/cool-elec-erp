package com.cool.modules.config.service;

/**
 * 合同打印服务接口
 */
public interface ContractPrintService {
    
    /**
     * 生成销售合同PDF
     *
     * @param orderId 销售订单ID
     * @param templateId 模板ID(可选,不传则使用默认模板)
     * @return PDF字节数组
     */
    byte[] generateSalesContractPdf(Long orderId, Long templateId);
    
    /**
     * 生成采购合同PDF
     *
     * @param orderId 采购订单ID
     * @param templateId 模板ID(可选,不传则使用默认模板)
     * @return PDF字节数组
     */
    byte[] generatePurchaseContractPdf(Long orderId, Long templateId);
}
