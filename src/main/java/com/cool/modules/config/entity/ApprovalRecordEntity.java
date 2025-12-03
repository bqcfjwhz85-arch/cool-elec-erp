package com.cool.modules.config.entity;

import com.cool.core.annotation.ColumnDefine;
import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.dromara.autotable.annotation.Index;

import java.time.LocalDateTime;

/**
 * 审批记录实体
 */
@Getter
@Setter
@Table(value = "erp_approval_record", comment = "审批记录表")
@Schema(description = "审批记录")
public class ApprovalRecordEntity extends BaseEntity<ApprovalRecordEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Index
    @ColumnDefine(comment = "审批流ID", notNull = true)
    @Schema(description = "审批流ID")
    private Long flowId;
    
    @ColumnDefine(comment = "审批流名称", length = 100)
    @Schema(description = "审批流名称")
    private String flowName;
    
    @Index
    @ColumnDefine(comment = "审批节点ID", notNull = true)
    @Schema(description = "审批节点ID")
    private Long nodeId;
    
    @ColumnDefine(comment = "节点名称", length = 100)
    @Schema(description = "节点名称")
    private String nodeName;
    
    @ColumnDefine(comment = "节点顺序", notNull = true)
    @Schema(description = "节点顺序")
    private Integer nodeOrder;
    
    @Index
    @ColumnDefine(comment = "审批实例ID（业务表主键ID）", notNull = true)
    @Schema(description = "审批实例ID")
    private Long instanceId;
    
    @Index
    @ColumnDefine(comment = "实例类型：1-销售订单 2-采购订单 3-服务商报备", notNull = true)
    @Schema(description = "实例类型：1-销售订单 2-采购订单 3-服务商报备")
    private Integer instanceType;
    
    @Index
    @ColumnDefine(comment = "业务单号", length = 50, notNull = true)
    @Schema(description = "业务单号")
    private String instanceNo;
    
    @Index
    @ColumnDefine(comment = "审批人ID")
    @Schema(description = "审批人ID")
    private Long approverId;
    
    @ColumnDefine(comment = "审批人姓名", length = 50)
    @Schema(description = "审批人姓名")
    private String approverName;
    
    @ColumnDefine(comment = "审批结果：1-通过 2-驳回")
    @Schema(description = "审批结果：1-通过 2-驳回")
    private Integer approvalResult;
    
    @ColumnDefine(comment = "审批意见", type = "text")
    @Schema(description = "审批意见")
    private String approvalOpinion;
    
    @ColumnDefine(comment = "审批时间")
    @Schema(description = "审批时间")
    private LocalDateTime approvalTime;
    
    @Index
    @ColumnDefine(comment = "状态：0-待审批 1-已审批 2-已撤回", notNull = true, defaultValue = "0")
    @Schema(description = "状态：0-待审批 1-已审批 2-已撤回")
    private Integer status;
    
    @ColumnDefine(comment = "申请人ID")
    @Schema(description = "申请人ID")
    private Long applicantId;
    
    @ColumnDefine(comment = "申请人姓名", length = 50)
    @Schema(description = "申请人姓名")
    private String applicantName;
    
    @ColumnDefine(comment = "申请时间")
    @Schema(description = "申请时间")
    private LocalDateTime applyTime;
}





