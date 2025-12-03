package com.cool.modules.config.service.impl;

import cn.hutool.core.util.StrUtil;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.config.entity.ApprovalNodeEntity;
import com.cool.modules.config.entity.ApprovalRecordEntity;
import com.cool.modules.config.mapper.ApprovalRecordMapper;
import com.cool.modules.config.service.ApprovalNodeService;
import com.cool.modules.config.service.ApprovalRecordService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.cool.modules.config.entity.table.ApprovalRecordEntityTableDef.APPROVAL_RECORD_ENTITY;

/**
 * 审批记录服务实现
 */
@Service
@RequiredArgsConstructor
public class ApprovalRecordServiceImpl extends BaseServiceImpl<ApprovalRecordMapper, ApprovalRecordEntity> 
        implements ApprovalRecordService {
    
    private final ApprovalNodeService approvalNodeService;
    
    @Override
    public List<ApprovalRecordEntity> getApprovalHistory(Integer instanceType, Long instanceId) {
        CoolPreconditions.check(instanceType == null, "实例类型不能为空");
        CoolPreconditions.check(instanceId == null, "实例ID不能为空");
        
        return list(QueryWrapper.create()
                .where(APPROVAL_RECORD_ENTITY.INSTANCE_TYPE.eq(instanceType))
                .and(APPROVAL_RECORD_ENTITY.INSTANCE_ID.eq(instanceId))
                .orderBy(APPROVAL_RECORD_ENTITY.NODE_ORDER, true)
                .orderBy(APPROVAL_RECORD_ENTITY.CREATE_TIME, true));
    }
    
    @Override
    public List<ApprovalRecordEntity> getMyPendingApprovals(Long approverId) {
        CoolPreconditions.check(approverId == null, "审批人ID不能为空");
        
        // 查询所有待审批的记录
        List<ApprovalRecordEntity> allPendingRecords = list(QueryWrapper.create()
                .where(APPROVAL_RECORD_ENTITY.STATUS.eq(0))
                .orderBy(APPROVAL_RECORD_ENTITY.CREATE_TIME, false));
        
        // 过滤出当前用户有权限审批的记录
        List<ApprovalRecordEntity> myPendingRecords = new ArrayList<>();
        String approverIdStr = approverId.toString();
        
        for (ApprovalRecordEntity record : allPendingRecords) {
            // 方式1：检查审批记录的 approverId 是否等于当前用户ID
            if (record.getApproverId() != null && record.getApproverId().equals(approverId)) {
                myPendingRecords.add(record);
                continue;
            }
            
            // 方式2：通过节点配置检查当前用户是否在审批人列表中
            if (record.getNodeId() != null) {
                ApprovalNodeEntity node = approvalNodeService.getById(record.getNodeId());
                if (node != null && StrUtil.isNotEmpty(node.getApproverIds())) {
                    // 将审批人ID列表转换为字符串列表
                    List<String> approverIdList = Arrays.stream(node.getApproverIds().split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());
                    
                    // 检查当前用户ID是否在审批人列表中
                    if (approverIdList.contains(approverIdStr)) {
                        myPendingRecords.add(record);
                    }
                }
            }
        }
        
        return myPendingRecords;
    }
    
    @Override
    public List<ApprovalRecordEntity> getMyApprovedRecords(Long approverId) {
        CoolPreconditions.check(approverId == null, "审批人ID不能为空");
        
        // 查询所有已审批的记录
        List<ApprovalRecordEntity> allApprovedRecords = list(QueryWrapper.create()
                .where(APPROVAL_RECORD_ENTITY.STATUS.eq(1))
                .orderBy(APPROVAL_RECORD_ENTITY.APPROVAL_TIME, false));
        
        // 过滤出当前用户审批过的记录
        List<ApprovalRecordEntity> myApprovedRecords = new ArrayList<>();
        String approverIdStr = approverId.toString();
        
        for (ApprovalRecordEntity record : allApprovedRecords) {
            // 方式1：检查审批记录的 approverId 是否等于当前用户ID
            if (record.getApproverId() != null && record.getApproverId().equals(approverId)) {
                myApprovedRecords.add(record);
                continue;
            }
            
            // 方式2：通过节点配置检查当前用户是否在审批人列表中
            if (record.getNodeId() != null) {
                ApprovalNodeEntity node = approvalNodeService.getById(record.getNodeId());
                if (node != null && StrUtil.isNotEmpty(node.getApproverIds())) {
                    // 将审批人ID列表转换为字符串列表
                    List<String> approverIdList = Arrays.stream(node.getApproverIds().split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());
                    
                    // 检查当前用户ID是否在审批人列表中
                    if (approverIdList.contains(approverIdStr)) {
                        myApprovedRecords.add(record);
                    }
                }
            }
        }
        
        return myApprovedRecords;
    }
    
    @Override
    public ApprovalRecordEntity getCurrentPendingRecord(Integer instanceType, Long instanceId) {
        CoolPreconditions.check(instanceType == null, "实例类型不能为空");
        CoolPreconditions.check(instanceId == null, "实例ID不能为空");
        
        return getOne(QueryWrapper.create()
                .where(APPROVAL_RECORD_ENTITY.INSTANCE_TYPE.eq(instanceType))
                .and(APPROVAL_RECORD_ENTITY.INSTANCE_ID.eq(instanceId))
                .and(APPROVAL_RECORD_ENTITY.STATUS.eq(0))
                .orderBy(APPROVAL_RECORD_ENTITY.NODE_ORDER, true));
    }
}



