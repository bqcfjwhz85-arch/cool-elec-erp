package com.cool.modules.config.service.impl;

import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.base.ModifyEnum;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.config.entity.ApprovalNodeEntity;
import com.cool.modules.config.mapper.ApprovalNodeMapper;
import com.cool.modules.config.service.ApprovalNodeService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.cool.modules.config.entity.table.ApprovalNodeEntityTableDef.APPROVAL_NODE_ENTITY;

/**
 * 审批节点服务实现
 */
@Service
@RequiredArgsConstructor
public class ApprovalNodeServiceImpl extends BaseServiceImpl<ApprovalNodeMapper, ApprovalNodeEntity> 
        implements ApprovalNodeService {
    
    @Override
    public List<ApprovalNodeEntity> getNodesByFlowId(Long flowId) {
        CoolPreconditions.check(flowId == null, "审批流ID不能为空");
        
        return list(QueryWrapper.create()
                .where(APPROVAL_NODE_ENTITY.FLOW_ID.eq(flowId))
                .orderBy(APPROVAL_NODE_ENTITY.NODE_ORDER, true));
    }
    
    @Override
    public ApprovalNodeEntity getNextNode(Long flowId, Integer currentOrder) {
        CoolPreconditions.check(flowId == null, "审批流ID不能为空");
        CoolPreconditions.check(currentOrder == null, "当前节点顺序不能为空");
        
        return getOne(QueryWrapper.create()
                .where(APPROVAL_NODE_ENTITY.FLOW_ID.eq(flowId))
                .and(APPROVAL_NODE_ENTITY.NODE_ORDER.gt(currentOrder))
                .orderBy(APPROVAL_NODE_ENTITY.NODE_ORDER, true));
    }
    
    @Override
    public void modifyBefore(JSONObject requestParams, ApprovalNodeEntity entity, ModifyEnum type) {
        // 新增或修改时校验
        if (type == ModifyEnum.ADD || type == ModifyEnum.UPDATE) {
            CoolPreconditions.check(entity.getFlowId() == null, "审批流ID不能为空");
            CoolPreconditions.check(entity.getNodeName() == null || entity.getNodeName().isEmpty(), 
                    "节点名称不能为空");
            CoolPreconditions.check(entity.getNodeOrder() == null, "节点顺序不能为空");
            CoolPreconditions.check(entity.getApproverRole() == null || entity.getApproverRole().isEmpty(), 
                    "审批人角色不能为空");
        }
    }
}

