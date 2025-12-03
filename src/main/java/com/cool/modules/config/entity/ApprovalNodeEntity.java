package com.cool.modules.config.entity;

import com.cool.core.annotation.ColumnDefine;
import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 审批节点实体
 */
@Getter
@Setter
@Table(value = "erp_approval_node", comment = "审批节点表")
@Schema(description = "审批节点")
public class ApprovalNodeEntity extends BaseEntity<ApprovalNodeEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @ColumnDefine(comment = "审批流ID", notNull = true)
    @Schema(description = "审批流ID")
    private Long flowId;
    
    @ColumnDefine(comment = "节点名称", length = 100, notNull = true)
    @Schema(description = "节点名称")
    private String nodeName;
    
    @ColumnDefine(comment = "节点顺序", notNull = true)
    @Schema(description = "节点顺序")
    private Integer nodeOrder;
    
    @ColumnDefine(comment = "审批人角色（多个用逗号分隔）", length = 200, notNull = true)
    @Schema(description = "审批人角色")
    private String approverRole;
    
    @ColumnDefine(comment = "审批人ID（多个用逗号分隔）", length = 500)
    @Schema(description = "审批人ID")
    private String approverIds;
    
    @ColumnDefine(comment = "备注", type = "text")
    @Schema(description = "备注")
    private String remark;
}
