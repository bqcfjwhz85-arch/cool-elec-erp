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
 * 订单流转配置实体
 */
@Getter
@Setter
@Table(value = "erp_flow_config", comment = "订单流转配置表")
@Schema(description = "订单流转配置")
public class FlowConfigEntity extends BaseEntity<FlowConfigEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @ColumnDefine(comment = "配置名称", length = 100, notNull = true)
    @Schema(description = "配置名称")
    private String configName;
    
    @ColumnDefine(comment = "订单类型：1-销售订单 2-采购订单", notNull = true)
    @Schema(description = "订单类型")
    private Integer orderType;
    
    @ColumnDefine(comment = "流转模式：1-自动 2-手动", defaultValue = "1")
    @Schema(description = "流转模式：1-自动 2-手动")
    private Integer flowMode;
    
    @ColumnDefine(comment = "通知方式：1-系统消息 2-邮件 3-短信（多个用逗号分隔）", length = 50)
    @Schema(description = "通知方式")
    private String notifyMethod;
    
    @ColumnDefine(comment = "超时提醒时长（小时）", defaultValue = "24")
    @Schema(description = "超时提醒时长（小时）")
    private Integer timeoutHours;
    
    @ColumnDefine(comment = "是否启用：0-停用 1-启用", defaultValue = "1")
    @Schema(description = "是否启用：0-停用 1-启用")
    private Integer enabled;
    
    @ColumnDefine(comment = "备注", type = "text")
    @Schema(description = "备注")
    private String remark;
}
