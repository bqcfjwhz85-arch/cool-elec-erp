package com.cool.modules.config.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.base.ModifyEnum;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.config.entity.ApprovalFlowEntity;
import com.cool.modules.config.entity.ApprovalNodeEntity;
import com.cool.modules.config.mapper.ApprovalFlowMapper;
import com.cool.modules.config.service.ApprovalFlowService;
import com.cool.modules.config.service.ApprovalNodeService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.cool.modules.config.entity.table.ApprovalFlowEntityTableDef.APPROVAL_FLOW_ENTITY;
import static com.cool.modules.config.entity.table.ApprovalNodeEntityTableDef.APPROVAL_NODE_ENTITY;

/**
 * 审批流配置服务实现
 */
@Service
@RequiredArgsConstructor
public class ApprovalFlowServiceImpl extends BaseServiceImpl<ApprovalFlowMapper, ApprovalFlowEntity> 
        implements ApprovalFlowService {
    
    private final ApprovalNodeService approvalNodeService;
    
    @Override
    public ApprovalFlowEntity getEnabledFlowByType(Integer flowType) {
        CoolPreconditions.check(flowType == null, "流程类型不能为空");
        
        ApprovalFlowEntity flow = getOne(QueryWrapper.create()
                .where(APPROVAL_FLOW_ENTITY.FLOW_TYPE.eq(flowType))
                .and(APPROVAL_FLOW_ENTITY.ENABLED.eq(1))
                .orderBy(APPROVAL_FLOW_ENTITY.CREATE_TIME, false));
        
        if (flow != null) {
            // 加载审批节点
            List<ApprovalNodeEntity> nodes = approvalNodeService.list(QueryWrapper.create()
                    .where(APPROVAL_NODE_ENTITY.FLOW_ID.eq(flow.getId()))
                    .orderBy(APPROVAL_NODE_ENTITY.NODE_ORDER, true));
            flow.setNodes(nodes);
        }
        
        return flow;
    }
    
    @Override
    public ApprovalFlowEntity getFlowWithNodes(Long flowId) {
        CoolPreconditions.check(flowId == null, "审批流ID不能为空");
        
        ApprovalFlowEntity flow = getById(flowId);
        if (flow != null) {
            // 加载审批节点
            List<ApprovalNodeEntity> nodes = approvalNodeService.list(QueryWrapper.create()
                    .where(APPROVAL_NODE_ENTITY.FLOW_ID.eq(flowId))
                    .orderBy(APPROVAL_NODE_ENTITY.NODE_ORDER, true));
            flow.setNodes(nodes);
        }
        
        return flow;
    }
    
    @Override
    public void updateEnabled(Long flowId, Integer enabled) {
        CoolPreconditions.check(flowId == null, "审批流ID不能为空");
        CoolPreconditions.check(enabled == null || (enabled != 0 && enabled != 1), 
                "启用状态必须为0或1");
        
        ApprovalFlowEntity flow = new ApprovalFlowEntity();
        flow.setId(flowId);
        flow.setEnabled(enabled);
        updateById(flow);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyBefore(JSONObject requestParams, ApprovalFlowEntity entity, ModifyEnum type) {
        // 新增或修改时校验
        if (type == ModifyEnum.ADD || type == ModifyEnum.UPDATE) {
            CoolPreconditions.check(entity.getFlowName() == null || entity.getFlowName().isEmpty(), 
                    "流程名称不能为空");
            CoolPreconditions.check(entity.getFlowType() == null, "流程类型不能为空");
            
            // 校验流程类型是否合法
            CoolPreconditions.check(entity.getFlowType() != 1 && entity.getFlowType() != 2 && entity.getFlowType() != 3, 
                    "流程类型必须为1(销售订单)、2(采购订单)或3(服务商报备)");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyAfter(JSONObject requestParams, ApprovalFlowEntity entity, ModifyEnum type) {
        // 保存审批节点
        if (type == ModifyEnum.ADD || type == ModifyEnum.UPDATE) {
            List<ApprovalNodeEntity> nodes = entity.getNodes();
            if (CollectionUtil.isNotEmpty(nodes)) {
                // 删除原有节点
                if (type == ModifyEnum.UPDATE) {
                    approvalNodeService.remove(QueryWrapper.create()
                            .where(APPROVAL_NODE_ENTITY.FLOW_ID.eq(entity.getId())));
                }
                
                // 保存新节点
                for (ApprovalNodeEntity node : nodes) {
                    node.setFlowId(entity.getId());
                    approvalNodeService.save(node);
                }
            }
        }
    }
}

