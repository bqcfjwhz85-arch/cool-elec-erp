package com.cool.modules.purchase.entity;

import com.cool.core.annotation.ColumnDefine;
import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Column;
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
 * 采购付款实体
 */
@Getter
@Setter
@Table(value = "erp_purchase_payment", comment = "采购付款表")
@Schema(description = "采购付款")
public class PurchasePaymentEntity extends BaseEntity<PurchasePaymentEntity> {

    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;

    @Index
    @ColumnDefine(comment = "采购订单ID", notNull = true)
    @Schema(description = "采购订单ID")
    private Long orderId;

    @ColumnDefine(comment = "付款编号", length = 50)
    @Schema(description = "付款编号")
    private String paymentNo;

    @ColumnDefine(comment = "付款日期", notNull = true)
    @Schema(description = "付款日期")
    private LocalDate paymentDate;

    @Column("payment_amount")
    @ColumnDefine(comment = "付款金额", type = "decimal(18,2)", notNull = true)
    @Schema(description = "付款金额")
    private BigDecimal amount;

    @ColumnDefine(comment = "付款方式", notNull = true)
    @Schema(description = "付款方式")
    private String paymentMethod;

    @ColumnDefine(comment = "付款账户", length = 100)
    @Schema(description = "付款账户")
    private String paymentAccount;

    @ColumnDefine(comment = "交易流水号", length = 100)
    @Schema(description = "交易流水号")
    private String transactionNo;

    @Column("attachment_file")
    @Schema(description = "付款凭证URL")
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
