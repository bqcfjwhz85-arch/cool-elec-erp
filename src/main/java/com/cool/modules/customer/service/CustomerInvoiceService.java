package com.cool.modules.customer.service;

import com.cool.core.base.BaseService;
import com.cool.modules.customer.entity.CustomerInvoiceEntity;

import java.util.List;

/**
 * 客户开票信息服务接口
 */
public interface CustomerInvoiceService extends BaseService<CustomerInvoiceEntity> {
    
    /**
     * 获取客户的所有开票信息
     * 
     * @param customerId 客户ID
     * @return 开票信息列表
     */
    List<CustomerInvoiceEntity> listByCustomerId(Long customerId);
    
    /**
     * 获取客户的默认开票信息
     * 
     * @param customerId 客户ID
     * @return 默认开票信息
     */
    CustomerInvoiceEntity getDefaultByCustomerId(Long customerId);
}

