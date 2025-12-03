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
 * 合同模板实体
 */
@Getter
@Setter
@Table(value = "erp_contract_template", comment = "合同模板表")
@Schema(description = "合同模板")
public class ContractTemplateEntity extends BaseEntity<ContractTemplateEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @ColumnDefine(comment = "模板名称", length = 100, notNull = true)
    @Schema(description = "模板名称")
    private String templateName;
    
    @ColumnDefine(comment = "模板类型：1-销售合同 2-采购合同", notNull = true)
    @Schema(description = "模板类型：1-销售合同 2-采购合同")
    private Integer templateType;
    
    @ColumnDefine(comment = "模板内容（HTML/JSON格式）", type = "longtext")
    @Schema(description = "模板内容")
    private String templateContent;
    
    @ColumnDefine(comment = "字段配置（JSON格式）", type = "text")
    @Schema(description = "字段配置")
    private String fieldConfig;
    
    @ColumnDefine(comment = "是否默认模板：0-否 1-是", defaultValue = "0")
    @Schema(description = "是否默认模板：0-否 1-是")
    private Integer isDefault;
    
    @ColumnDefine(comment = "是否启用：0-停用 1-启用", defaultValue = "1")
    @Schema(description = "是否启用：0-停用 1-启用")
    private Integer enabled;
    
    @ColumnDefine(comment = "校验结果（是否跨页等）", type = "text")
    @Schema(description = "校验结果")
    private String validationResult;
    
    @ColumnDefine(comment = "模板编码", length = 50, notNull = true)
    @Schema(description = "模板编码")
    private String templateCode;

    @ColumnDefine(comment = "适用类型：1-借码 2-实供 3-通用", defaultValue = "3")
    @Schema(description = "适用类型：1-借码 2-实供 3-通用")
    private Integer applicableType;

    @ColumnDefine(comment = "页眉高度", defaultValue = "0")
    @Schema(description = "页眉高度")
    private Integer headerHeight;

    @ColumnDefine(comment = "页脚高度", defaultValue = "0")
    @Schema(description = "页脚高度")
    private Integer footerHeight;

    @ColumnDefine(comment = "明细行高", defaultValue = "0")
    @Schema(description = "明细行高")
    private Integer itemLineHeight;

    @ColumnDefine(comment = "每页最大行数", defaultValue = "15")
    @Schema(description = "每页最大行数")
    private Integer maxItemsPerPage;

    @ColumnDefine(comment = "备注", type = "text")
    @Schema(description = "备注")
    private String remark;
}
