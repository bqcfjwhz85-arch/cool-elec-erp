package com.cool.modules.sale.entity;

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
import java.time.LocalDate;

/**
 * 回款信息实体
 */
@Getter
@Setter
@Table(value = "erp_sales_payment", comment = "销售订单回款信息表")
@Schema(description = "回款信息")
public class SalesPaymentEntity extends BaseEntity<SalesPaymentEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Index
    @ColumnDefine(comment = "订单ID", notNull = true)
    @Schema(description = "订单ID")
    private Long orderId;
    
    @ColumnDefine(comment = "回款编号", length = 50)
    @Schema(description = "回款编号")
    private String paymentNo;
    
    @ColumnDefine(comment = "回款日期", notNull = true)
    @Schema(description = "回款日期")
    private LocalDate paymentDate;
    
    @ColumnDefine(comment = "回款金额", type = "decimal(18,2)", notNull = true)
    @Schema(description = "回款金额")
    private BigDecimal amount;
    
    @ColumnDefine(comment = "回款方式：1-银行转账 2-现金 3-支票 4-其他", notNull = true)
    @Schema(description = "回款方式：1-银行转账 2-现金 3-支票 4-其他")
    private Integer paymentMethod;
    
    @ColumnDefine(comment = "回款账户", length = 100)
    @Schema(description = "回款账户")
    private String paymentAccount;
    
    @ColumnDefine(comment = "交易流水号", length = 100)
    @Schema(description = "交易流水号")
    private String transactionNo;
    
    @ColumnDefine(comment = "回款凭证URL", length = 500)
    @Schema(description = "回款凭证URL")
    private String voucherUrl;
    
    @ColumnDefine(comment = "状态：0-待确认 1-已确认", defaultValue = "0")
    @Schema(description = "状态：0-待确认 1-已确认")
    private Integer status;
    
    @ColumnDefine(comment = "确认人ID")
    @Schema(description = "确认人ID")
    private Long confirmBy;
    
    @ColumnDefine(comment = "确认时间")
    @Schema(description = "确认时间")
    private LocalDate confirmTime;
    
    @ColumnDefine(comment = "删除原因", length = 500)
    @Schema(description = "删除原因")
    private String deleteReason;
    
    @ColumnDefine(comment = "备注", type = "text")
    @Schema(description = "备注")
    private String remark;
}

