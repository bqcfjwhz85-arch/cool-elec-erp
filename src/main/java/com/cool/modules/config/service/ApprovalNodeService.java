package com.cool.modules.config.service;

import com.cool.core.base.BaseService;
import com.cool.modules.config.entity.ApprovalNodeEntity;

import java.util.List;

/**
 * 审批节点服务
 */
public interface ApprovalNodeService extends BaseService<ApprovalNodeEntity> {
    
    /**
     * 根据审批流ID获取节点列表
     * @param flowId 审批流ID
     * @return 节点列表（按顺序排序）
     */
    List<ApprovalNodeEntity> getNodesByFlowId(Long flowId);
    
    /**
     * 获取下一个审批节点
     * @param flowId 审批流ID
     * @param currentOrder 当前节点顺序
     * @return 下一个节点，如果没有则返回null
     */
    ApprovalNodeEntity getNextNode(Long flowId, Integer currentOrder);
}





