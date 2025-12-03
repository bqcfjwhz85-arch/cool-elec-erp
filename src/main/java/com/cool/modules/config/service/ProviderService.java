package com.cool.modules.config.service;

import com.cool.core.base.BaseService;
import com.cool.modules.config.entity.ProviderEntity;

/**
 * 服务商服务接口
 */
public interface ProviderService extends BaseService<ProviderEntity> {
    
    /**
     * 根据编码获取服务商
     * 
     * @param code 服务商编码
     * @return 服务商信息
     */
    ProviderEntity getByCode(String code);
    
    /**
     * 检查服务商是否为区域服务商
     * 
     * @param providerId 服务商ID
     * @return true-是区域服务商 false-否
     */
    boolean isRegionalProvider(Long providerId);
}

