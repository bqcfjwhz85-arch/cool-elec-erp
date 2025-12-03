package com.cool.modules.config.service;

import com.cool.core.base.BaseService;
import com.cool.modules.config.entity.ApprovalFlowEntity;

/**
 * 审批流配置服务
 */
public interface ApprovalFlowService extends BaseService<ApprovalFlowEntity> {
    
    /**
     * 根据流程类型获取启用的审批流
     * @param flowType 流程类型：1-销售订单 2-采购订单 3-服务商报备
     * @return 审批流配置（包含节点）
     */
    ApprovalFlowEntity getEnabledFlowByType(Integer flowType);
    
    /**
     * 获取审批流详情（包含节点）
     * @param flowId 审批流ID
     * @return 审批流配置（包含节点）
     */
    ApprovalFlowEntity getFlowWithNodes(Long flowId);
    
    /**
     * 启用/禁用审批流
     * @param flowId 审批流ID
     * @param enabled 是否启用：0-禁用 1-启用
     */
    void updateEnabled(Long flowId, Integer enabled);
}




