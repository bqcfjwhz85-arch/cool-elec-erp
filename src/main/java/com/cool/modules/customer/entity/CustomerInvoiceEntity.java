package com.cool.modules.customer.entity;

import com.cool.core.annotation.ColumnDefine;
import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 客户开票信息实体
 */
@Getter
@Setter
@Table(value = "erp_customer_invoice", comment = "客户开票信息表")
@Schema(description = "客户开票信息")
public class CustomerInvoiceEntity extends BaseEntity<CustomerInvoiceEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @ColumnDefine(comment = "客户ID", notNull = true)
    @Schema(description = "客户ID")
    private Long customerId;
    
    @ColumnDefine(comment = "发票类型：1-增值税专用发票 2-增值税普通发票", notNull = true)
    @Schema(description = "发票类型")
    private Integer invoiceType;
    
    @ColumnDefine(comment = "发票抬头", length = 200, notNull = true)
    @Schema(description = "发票抬头")
    private String invoiceTitle;
    
    @ColumnDefine(comment = "纳税人识别号", length = 50, notNull = true)
    @Schema(description = "纳税人识别号")
    private String taxNumber;
    
    @ColumnDefine(comment = "注册地址", length = 200)
    @Schema(description = "注册地址")
    private String registeredAddress;
    
    @ColumnDefine(comment = "注册电话", length = 50)
    @Schema(description = "注册电话")
    private String registeredPhone;
    
    @ColumnDefine(comment = "开户银行", length = 100)
    @Schema(description = "开户银行")
    private String bankName;
    
    @ColumnDefine(comment = "银行账号", length = 50)
    @Schema(description = "银行账号")
    private String bankAccount;
    
    @ColumnDefine(comment = "收票人", length = 50)
    @Schema(description = "收票人")
    private String recipient;
    
    @ColumnDefine(comment = "收票人电话", length = 20)
    @Schema(description = "收票人电话")
    private String recipientPhone;
    
    @ColumnDefine(comment = "收票地址", length = 200)
    @Schema(description = "收票地址")
    private String recipientAddress;
    
    @ColumnDefine(comment = "是否默认：0-否 1-是", defaultValue = "0")
    @Schema(description = "是否默认：0-否 1-是")
    private Integer isDefault;
    
    @ColumnDefine(comment = "备注", type = "text")
    @Schema(description = "备注")
    private String remark;
}

