package com.cool.modules.config.service;

import com.cool.modules.config.entity.ApprovalRecordEntity;

import java.util.List;

/**
 * 审批流程处理服务
 */
public interface ApprovalProcessService {
    
    /**
     * 启动审批流程
     * @param instanceType 实例类型：1-销售订单 2-采购订单 3-服务商报备
     * @param instanceId 实例ID（订单ID/报备ID）
     * @param instanceNo 业务单号
     * @param applicantId 申请人ID
     * @param applicantName 申请人姓名
     * @return 是否启动成功
     */
    boolean startApproval(Integer instanceType, Long instanceId, String instanceNo, 
                         Long applicantId, String applicantName);
    
    /**
     * 审批处理
     * @param recordId 审批记录ID
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @param result 审批结果：1-通过 2-驳回
     * @param opinion 审批意见
     */
    void approve(Long recordId, Long approverId, String approverName, 
                Integer result, String opinion);
    
    /**
     * 撤回审批
     * @param instanceType 实例类型
     * @param instanceId 实例ID
     */
    void withdraw(Integer instanceType, Long instanceId);
    
    /**
     * 检查审批权限
     * @param recordId 审批记录ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean checkApprovalPermission(Long recordId, Long userId);
    
    /**
     * 获取审批进度
     * @param instanceType 实例类型
     * @param instanceId 实例ID
     * @return 审批记录列表
     */
    List<ApprovalRecordEntity> getApprovalProgress(Integer instanceType, Long instanceId);
}





