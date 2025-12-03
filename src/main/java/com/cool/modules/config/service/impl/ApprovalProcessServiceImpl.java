package com.cool.modules.config.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.cool.core.exception.CoolPreconditions;
import com.cool.core.util.CoolSecurityUtil;
import com.cool.modules.config.entity.ApprovalFlowEntity;
import com.cool.modules.config.entity.ApprovalNodeEntity;
import com.cool.modules.config.entity.ApprovalRecordEntity;
import com.cool.modules.config.service.ApprovalFlowService;
import com.cool.modules.config.service.ApprovalNodeService;
import com.cool.modules.config.service.ApprovalProcessService;
import com.cool.modules.config.service.ApprovalRecordService;
import com.cool.modules.purchase.entity.PurchaseOrderEntity;
import com.cool.modules.purchase.service.PurchaseOrderService;
import com.cool.modules.sale.entity.SalesOrderEntity;
import com.cool.modules.sale.service.SalesOrderService;
import com.cool.modules.source.entity.SourceEntity;
import com.cool.modules.source.service.SourceService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.cool.modules.config.entity.table.ApprovalRecordEntityTableDef.APPROVAL_RECORD_ENTITY;

/**
 * 审批流程处理服务实现
 */
@Slf4j
@Service
public class ApprovalProcessServiceImpl implements ApprovalProcessService {
    
    private final ApprovalFlowService approvalFlowService;
    private final ApprovalNodeService approvalNodeService;
    private final ApprovalRecordService approvalRecordService;
    private final SalesOrderService salesOrderService;
    private final PurchaseOrderService purchaseOrderService;
    private final SourceService sourceService;
    
    // 使用构造函数注入，并对循环依赖的服务使用@Lazy
    public ApprovalProcessServiceImpl(
            ApprovalFlowService approvalFlowService,
            ApprovalNodeService approvalNodeService,
            ApprovalRecordService approvalRecordService,
            @Lazy SalesOrderService salesOrderService,
            PurchaseOrderService purchaseOrderService,
            @Lazy SourceService sourceService) {
        this.approvalFlowService = approvalFlowService;
        this.approvalNodeService = approvalNodeService;
        this.approvalRecordService = approvalRecordService;
        this.salesOrderService = salesOrderService;
        this.purchaseOrderService = purchaseOrderService;
        this.sourceService = sourceService;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startApproval(Integer instanceType, Long instanceId, String instanceNo,
                                Long applicantId, String applicantName) {
        CoolPreconditions.check(instanceType == null, "实例类型不能为空");
        CoolPreconditions.check(instanceId == null, "实例ID不能为空");
        CoolPreconditions.check(StrUtil.isEmpty(instanceNo), "业务单号不能为空");
        
        // 获取对应的审批流配置
        Integer flowType = getFlowType(instanceType);
        ApprovalFlowEntity flow = approvalFlowService.getEnabledFlowByType(flowType);
        
        if (flow == null) {
            log.warn("未找到启用的审批流配置，实例类型: {}", instanceType);
            return false;
        }
        
        List<ApprovalNodeEntity> nodes = flow.getNodes();
        if (CollectionUtil.isEmpty(nodes)) {
            log.warn("审批流未配置节点，流程ID: {}", flow.getId());
            return false;
        }
        
        // 创建审批记录
        ApprovalNodeEntity firstNode = nodes.get(0);
        ApprovalRecordEntity record = new ApprovalRecordEntity();
        record.setFlowId(flow.getId());
        record.setFlowName(flow.getFlowName());
        record.setNodeId(firstNode.getId());
        record.setNodeName(firstNode.getNodeName());
        record.setNodeOrder(firstNode.getNodeOrder());
        record.setInstanceId(instanceId);
        record.setInstanceType(instanceType);
        record.setInstanceNo(instanceNo);
        record.setStatus(0); // 待审批
        record.setApplicantId(applicantId);
        record.setApplicantName(applicantName);
        record.setApplyTime(LocalDateTime.now());
        
        // 设置审批人（从节点配置中获取第一个审批人）
        String approverIds = firstNode.getApproverIds();
        if (StrUtil.isNotEmpty(approverIds)) {
            String[] ids = approverIds.split(",");
            if (ids.length > 0) {
                try {
                    Long approverId = Long.parseLong(ids[0].trim());
                    record.setApproverId(approverId);
                    log.info("设置审批人ID: {}, 审批人列表: {}", approverId, approverIds);
                } catch (NumberFormatException e) {
                    log.error("审批人ID格式错误: {}", ids[0], e);
                }
            } else {
                log.warn("审批节点未配置审批人ID，节点ID: {}, 节点名称: {}", firstNode.getId(), firstNode.getNodeName());
            }
        } else {
            log.warn("审批节点approverIds为空，节点ID: {}, 节点名称: {}", firstNode.getId(), firstNode.getNodeName());
        }
        
        approvalRecordService.save(record);
        log.info("审批记录创建成功，记录ID: {}, 实例类型: {}, 实例ID: {}, 实例单号: {}, 审批人ID: {}", 
                record.getId(), instanceType, instanceId, instanceNo, record.getApproverId());
        
        // 更新业务表的审批状态为待审核
        updateBusinessApprovalStatus(instanceType, instanceId, 0);
        
        log.info("启动审批流程成功，实例类型: {}, 实例ID: {}, 审批流: {}", 
                instanceType, instanceId, flow.getFlowName());
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long recordId, Long approverId, String approverName,
                       Integer result, String opinion) {
        CoolPreconditions.check(recordId == null, "审批记录ID不能为空");
        CoolPreconditions.check(approverId == null, "审批人ID不能为空");
        CoolPreconditions.check(result == null || (result != 1 && result != 2), 
                "审批结果必须为1(通过)或2(驳回)");
        
        // 获取审批记录
        ApprovalRecordEntity record = approvalRecordService.getById(recordId);
        CoolPreconditions.check(record == null, "审批记录不存在");
        CoolPreconditions.check(record.getStatus() != 0, "该审批记录已处理");
        
        // 检查审批权限
        CoolPreconditions.check(!checkApprovalPermission(recordId, approverId), 
                "您无权审批该记录");
        
        // 更新审批记录
        record.setApproverId(approverId);
        record.setApproverName(approverName);
        record.setApprovalResult(result);
        record.setApprovalOpinion(opinion);
        record.setApprovalTime(LocalDateTime.now());
        record.setStatus(1); // 已审批
        approvalRecordService.updateById(record);
        
        if (result == 2) {
            // 审批驳回，保存驳回原因到业务实体
            if (record.getInstanceType() == 1) {
                SalesOrderEntity salesOrder = salesOrderService.getById(record.getInstanceId());
                if (salesOrder != null) {
                    salesOrder.setRejectReason(opinion);
                    salesOrderService.updateById(salesOrder);
                }
            } else if (record.getInstanceType() == 2) {
                PurchaseOrderEntity purchaseOrder = purchaseOrderService.getById(record.getInstanceId());
                if (purchaseOrder != null) {
                    purchaseOrder.setRejectReason(opinion);
                    purchaseOrderService.updateById(purchaseOrder);
                }
            } else if (record.getInstanceType() == 3) {
                SourceEntity source = sourceService.getById(record.getInstanceId());
                if (source != null) {
                    source.setRejectReason(opinion);
                    sourceService.updateById(source);
                }
            }
            // 审批驳回，更新业务表审批状态为驳回
            updateBusinessApprovalStatus(record.getInstanceType(), record.getInstanceId(), 2);
            log.info("审批驳回，实例类型: {}, 实例ID: {}", record.getInstanceType(), record.getInstanceId());
        } else {
            // 审批通过，检查是否有下一个节点
            ApprovalNodeEntity nextNode = approvalNodeService.getNextNode(
                    record.getFlowId(), record.getNodeOrder());
            
            if (nextNode == null) {
                // 没有下一个节点，审批流程结束，更新业务表审批状态为通过
                updateBusinessApprovalStatus(record.getInstanceType(), record.getInstanceId(), 1);
                log.info("审批流程全部通过，实例类型: {}, 实例ID: {}", 
                        record.getInstanceType(), record.getInstanceId());
            } else {
                // 创建下一个审批节点的记录
                ApprovalRecordEntity nextRecord = new ApprovalRecordEntity();
                nextRecord.setFlowId(record.getFlowId());
                nextRecord.setFlowName(record.getFlowName());
                nextRecord.setNodeId(nextNode.getId());
                nextRecord.setNodeName(nextNode.getNodeName());
                nextRecord.setNodeOrder(nextNode.getNodeOrder());
                nextRecord.setInstanceId(record.getInstanceId());
                nextRecord.setInstanceType(record.getInstanceType());
                nextRecord.setInstanceNo(record.getInstanceNo());
                nextRecord.setStatus(0); // 待审批
                nextRecord.setApplicantId(record.getApplicantId());
                nextRecord.setApplicantName(record.getApplicantName());
                nextRecord.setApplyTime(record.getApplyTime());
                
                // 设置审批人
                String approverIds = nextNode.getApproverIds();
                if (StrUtil.isNotEmpty(approverIds)) {
                    String[] ids = approverIds.split(",");
                    if (ids.length > 0) {
                        nextRecord.setApproverId(Long.parseLong(ids[0].trim()));
                    }
                }
                
                approvalRecordService.save(nextRecord);
                log.info("审批流转到下一节点: {}, 实例类型: {}, 实例ID: {}", 
                        nextNode.getNodeName(), record.getInstanceType(), record.getInstanceId());
            }
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Integer instanceType, Long instanceId) {
        CoolPreconditions.check(instanceType == null, "实例类型不能为空");
        CoolPreconditions.check(instanceId == null, "实例ID不能为空");
        
        // 查询所有待审批的记录
        List<ApprovalRecordEntity> records = approvalRecordService.list(QueryWrapper.create()
                .where(APPROVAL_RECORD_ENTITY.INSTANCE_TYPE.eq(instanceType))
                .and(APPROVAL_RECORD_ENTITY.INSTANCE_ID.eq(instanceId))
                .and(APPROVAL_RECORD_ENTITY.STATUS.eq(0)));
        
        if (CollectionUtil.isEmpty(records)) {
            throw new RuntimeException("未找到待审批记录");
        }
        
        // 更新所有待审批记录为已撤回
        for (ApprovalRecordEntity record : records) {
            record.setStatus(2); // 已撤回
            approvalRecordService.updateById(record);
        }
        
        log.info("撤回审批成功，实例类型: {}, 实例ID: {}", instanceType, instanceId);
    }
    
    @Override
    public boolean checkApprovalPermission(Long recordId, Long userId) {
        // 超级管理员拥有所有审批权限
        if (CoolSecurityUtil.isSuperAdmin()) {
            return true;
        }

        ApprovalRecordEntity record = approvalRecordService.getById(recordId);
        if (record == null || record.getStatus() != 0) {
            return false;
        }
        
        // 检查用户是否在审批人列表中
        ApprovalNodeEntity node = approvalNodeService.getById(record.getNodeId());
        if (node == null) {
            return false;
        }
        
        String approverIds = node.getApproverIds();
        if (StrUtil.isEmpty(approverIds)) {
            return false;
        }
        
        List<String> idList = Arrays.asList(approverIds.split(","));
        return idList.contains(userId.toString());
    }
    
    @Override
    public List<ApprovalRecordEntity> getApprovalProgress(Integer instanceType, Long instanceId) {
        return approvalRecordService.getApprovalHistory(instanceType, instanceId);
    }
    
    /**
     * 根据实例类型获取审批流类型
     */
    private Integer getFlowType(Integer instanceType) {
        // 根据对接文档：
        // 1-销售订单 -> flowType=1
        // 2-采购订单 -> flowType=2
        // 3-服务商报备 -> flowType=3
        // 4-销售合同 -> flowType=4
        // 5-采购合同 -> flowType=5
        switch (instanceType) {
            case 1:
                return 1; // 销售订单
            case 2:
                return 2; // 采购订单
            case 3:
                return 3; // 服务商报备
            case 4:
                return 4; // 销售合同
            case 5:
                return 5; // 采购合同
            default:
                return instanceType; // 默认使用相同的类型
        }
    }
    
    /**
     * 更新业务表的审批状态
     */
    private void updateBusinessApprovalStatus(Integer instanceType, Long instanceId, Integer status) {
        try {
            switch (instanceType) {
                case 1:
                    // 销售订单
                    SalesOrderEntity salesOrder = salesOrderService.getById(instanceId);
                    if (salesOrder != null) {
                        salesOrder.setApprovalStatus(status);
                        // 手动创建(2/3)的销售合同审批通过后自动更新订单状态为已确认
                        Integer createMode = salesOrder.getCreateMode();
                        if (Integer.valueOf(1).equals(status)
                            && createMode != null
                            && !Integer.valueOf(1).equals(createMode)) {
                            salesOrder.setStatus(1);
                        }
                        salesOrderService.updateById(salesOrder);
                    }
                    break;
                case 2:
                    // 采购订单
                    PurchaseOrderEntity purchaseOrder = purchaseOrderService.getById(instanceId);
                    if (purchaseOrder != null) {
                        purchaseOrder.setApprovalStatus(status);
                        if (status == 0) {
                            purchaseOrder.setRejectReason(null);
                        }
                        purchaseOrderService.updateById(purchaseOrder);
                    }
                    break;
                case 3:
                    // 服务商报备
                    // 注意：SourceEntity 使用 status 字段而不是 approvalStatus
                    // status: 0-待审核 1-审核通过 2-审核驳回 3-已生成订单
                    SourceEntity source = new SourceEntity();
                    source.setId(instanceId);
                    // 审批状态映射：0-待审核 -> status=0, 1-审核通过 -> status=1, 2-审核驳回 -> status=2
                    source.setStatus(status);
                    sourceService.updateById(source);
                    break;
                case 4:
                    // 销售合同（如果存在独立的销售合同实体，需要在这里处理）
                    // TODO: 如果存在销售合同实体，需要添加相应的更新逻辑
                    log.warn("销售合同审批状态更新暂未实现，实例ID: {}", instanceId);
                    break;
                case 5:
                    // 采购合同（如果存在独立的采购合同实体，需要在这里处理）
                    // TODO: 如果存在采购合同实体，需要添加相应的更新逻辑
                    log.warn("采购合同审批状态更新暂未实现，实例ID: {}", instanceId);
                    break;
                default:
                    log.warn("未知的实例类型: {}, 实例ID: {}", instanceType, instanceId);
            }
        } catch (Exception e) {
            log.error("更新业务表审批状态失败，实例类型: {}, 实例ID: {}, 状态: {}", 
                    instanceType, instanceId, status, e);
        }
    }
}



