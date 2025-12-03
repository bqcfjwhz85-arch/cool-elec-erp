package com.cool.modules.config.service.impl;

import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.config.entity.ContractTemplateEntity;
import com.cool.modules.config.mapper.ContractTemplateMapper;
import com.cool.modules.config.service.ContractTemplateService;
import org.springframework.stereotype.Service;

/**
 * 合同模板服务实现类
 */
@Service
public class ContractTemplateServiceImpl extends BaseServiceImpl<ContractTemplateMapper, ContractTemplateEntity> implements ContractTemplateService {
    @Override
    public Long add(ContractTemplateEntity entity) {
        if (entity.getTemplateType() == null) {
            entity.setTemplateType(1); // 默认销售合同
        }
        if (entity.getTemplateCode() == null || entity.getTemplateCode().isEmpty()) {
            // 自动生成编码: CT + 时间戳
            entity.setTemplateCode("CT" + System.currentTimeMillis());
        }
        return (Long) super.add(entity);
    }
}
