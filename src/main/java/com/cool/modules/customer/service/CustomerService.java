package com.cool.modules.customer.service;

import com.cool.core.base.BaseService;
import com.cool.modules.customer.entity.CustomerEntity;

import java.util.Map;

/**
 * 客户服务接口
 */
public interface CustomerService extends BaseService<CustomerEntity> {
    
    /**
     * 根据客户名称获取客户
     * 
     * @param name 客户名称
     * @return 客户信息
     */
    CustomerEntity getByName(String name);
    
    /**
     * 根据编码获取客户
     * 
     * @param code 客户编码
     * @return 客户信息
     */
    CustomerEntity getByCode(String code);
    
    /**
     * 获取客户统计信息（订单数量、交易金额等）
     * 
     * @param customerId 客户ID
     * @return 统计信息
     */
    Map<String, Object> getCustomerStatistics(Long customerId);
}

