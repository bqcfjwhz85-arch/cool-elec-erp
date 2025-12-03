package com.cool.modules.purchase.entity;

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
import org.dromara.autotable.annotation.Index;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 对账单实体
 */
@Getter
@Setter
@Table(value = "erp_reconciliation", comment = "对账单表")
@Schema(description = "对账单")
public class ReconciliationEntity extends BaseEntity<ReconciliationEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Index
    @ColumnDefine(comment = "对账单编号（BILL+年月+序号）", length = 50, notNull = true)
    @Schema(description = "对账单编号")
    private String billNo;
    
    @Index
    @ColumnDefine(comment = "供应商ID", notNull = true)
    @Schema(description = "供应商ID")
    private Long supplierId;
    
    @ColumnDefine(comment = "供应商名称", length = 200, notNull = true)
    @Schema(description = "供应商名称")
    private String supplierName;
    
    @ColumnDefine(comment = "开始日期", notNull = true)
    @Schema(description = "开始日期")
    private LocalDateTime startDate;
    
    @ColumnDefine(comment = "结束日期", notNull = true)
    @Schema(description = "结束日期")
    private LocalDateTime endDate;
    
    @ColumnDefine(comment = "订单数量", defaultValue = "0")
    @Schema(description = "订单数量")
    private Integer orderCount;
    
    @ColumnDefine(comment = "总金额", type = "decimal(18,2)", notNull = true, defaultValue = "0.00")
    @Schema(description = "总金额")
    private BigDecimal totalAmount;
    
    @ColumnDefine(comment = "已付款金额", type = "decimal(18,2)", defaultValue = "0.00")
    @Schema(description = "已付款金额")
    private BigDecimal paidAmount;
    
    @ColumnDefine(comment = "未付款金额", type = "decimal(18,2)", defaultValue = "0.00")
    @Schema(description = "未付款金额")
    private BigDecimal unpaidAmount;
    
    @ColumnDefine(comment = "对账状态：0-待对账 1-已对账 2-有差异", notNull = true, defaultValue = "0")
    @Schema(description = "对账状态：0-待对账 1-已对账 2-有差异")
    private Integer status;
    
    @ColumnDefine(comment = "差异原因", type = "text")
    @Schema(description = "差异原因")
    private String differenceReason;
    
    @ColumnDefine(comment = "备注", type = "text")
    @Schema(description = "备注")
    private String remark;
    
    @ColumnDefine(comment = "创建人", length = 50)
    @Schema(description = "创建人")
    private String createBy;
    
    // 一对多关联：对账单明细
    @Column(ignore = true)
    @RelationOneToMany(selfField = "id", targetField = "reconciliationId")
    @Schema(description = "对账单明细")
    private List<ReconciliationItemEntity> items;
}






