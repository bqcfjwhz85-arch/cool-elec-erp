package com.cool.modules.config.entity;

import com.cool.core.annotation.ColumnDefine;
import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.RelationOneToMany;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 审批流配置实体
 */
@Getter
@Setter
@Table(value = "erp_approval_flow", comment = "审批流配置表")
@Schema(description = "审批流配置")
public class ApprovalFlowEntity extends BaseEntity<ApprovalFlowEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @ColumnDefine(comment = "流程名称", length = 100, notNull = true)
    @Schema(description = "流程名称")
    private String flowName;
    
    @ColumnDefine(comment = "流程类型：1-销售订单 2-采购订单 3-服务商报备", notNull = true)
    @Schema(description = "流程类型：1-销售订单 2-采购订单 3-服务商报备")
    private Integer flowType;
    
    @ColumnDefine(comment = "超时提醒时长（小时）", defaultValue = "24")
    @Schema(description = "超时提醒时长（小时）")
    private Integer timeoutHours;
    
    @ColumnDefine(comment = "是否启用：0-停用 1-启用", defaultValue = "1")
    @Schema(description = "是否启用：0-停用 1-启用")
    private Integer enabled;
    
    @ColumnDefine(comment = "备注", type = "text")
    @Schema(description = "备注")
    private String remark;
    
    // 关联审批节点
    @Column(ignore = true)
    @RelationOneToMany(selfField = "id", targetField = "flowId")
    @Schema(description = "审批节点列表")
    private List<ApprovalNodeEntity> nodes;
}

