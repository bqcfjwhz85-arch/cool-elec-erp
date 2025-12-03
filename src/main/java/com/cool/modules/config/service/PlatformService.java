package com.cool.modules.config.service;

import com.cool.core.base.BaseService;
import com.cool.modules.config.entity.PlatformEntity;

/**
 * 平台服务接口
 */
public interface PlatformService extends BaseService<PlatformEntity> {
    
    /**
     * 根据编码获取平台
     * 
     * @param code 平台编码
     * @return 平台信息
     */
    PlatformEntity getByCode(String code);
}

