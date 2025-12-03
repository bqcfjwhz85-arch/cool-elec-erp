package com.cool.modules.purchase.entity;

import com.cool.core.annotation.ColumnDefine;
import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.dromara.autotable.annotation.Index;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对账单明细实体
 */
@Getter
@Setter
@Table(value = "erp_reconciliation_item", comment = "对账单明细表")
@Schema(description = "对账单明细")
public class ReconciliationItemEntity extends BaseEntity<ReconciliationItemEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Index
    @ColumnDefine(comment = "对账单ID", notNull = true)
    @Schema(description = "对账单ID")
    private Long reconciliationId;
    
    @Index
    @ColumnDefine(comment = "采购订单ID", notNull = true)
    @Schema(description = "采购订单ID")
    private Long orderId;
    
    @ColumnDefine(comment = "采购单号", length = 50)
    @Schema(description = "采购单号")
    private String orderNo;
    
    @ColumnDefine(comment = "采购日期")
    @Schema(description = "采购日期")
    private LocalDateTime purchaseDate;
    
    @ColumnDefine(comment = "采购金额", type = "decimal(18,2)", notNull = true)
    @Schema(description = "采购金额")
    private BigDecimal orderAmount;
    
    @ColumnDefine(comment = "已付款金额", type = "decimal(18,2)", defaultValue = "0.00")
    @Schema(description = "已付款金额")
    private BigDecimal paidAmount;
    
    @ColumnDefine(comment = "未付款金额", type = "decimal(18,2)", defaultValue = "0.00")
    @Schema(description = "未付款金额")
    private BigDecimal unpaidAmount;
    
    @ColumnDefine(comment = "订单状态", length = 20)
    @Schema(description = "订单状态")
    private String orderStatus;
    
    @ColumnDefine(comment = "备注", type = "text")
    @Schema(description = "备注")
    private String remark;
}






