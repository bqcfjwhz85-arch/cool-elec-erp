package com.cool.modules.config.service;

import com.cool.core.base.BaseService;
import com.cool.modules.config.entity.ApprovalRecordEntity;

import java.util.List;

/**
 * 审批记录服务
 */
public interface ApprovalRecordService extends BaseService<ApprovalRecordEntity> {
    
    /**
     * 获取审批历史
     * @param instanceType 实例类型
     * @param instanceId 实例ID
     * @return 审批记录列表
     */
    List<ApprovalRecordEntity> getApprovalHistory(Integer instanceType, Long instanceId);
    
    /**
     * 获取我的待审批列表
     * @param approverId 审批人ID
     * @return 待审批记录列表
     */
    List<ApprovalRecordEntity> getMyPendingApprovals(Long approverId);
    
    /**
     * 获取我的已审批列表
     * @param approverId 审批人ID
     * @return 已审批记录列表
     */
    List<ApprovalRecordEntity> getMyApprovedRecords(Long approverId);
    
    /**
     * 获取当前待审批记录
     * @param instanceType 实例类型
     * @param instanceId 实例ID
     * @return 当前待审批记录
     */
    ApprovalRecordEntity getCurrentPendingRecord(Integer instanceType, Long instanceId);
}





