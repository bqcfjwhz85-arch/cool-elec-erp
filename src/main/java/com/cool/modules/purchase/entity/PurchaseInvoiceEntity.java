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
 * 采购发票实体
 */
@Getter
@Setter
@Table(value = "erp_purchase_invoice", comment = "采购发票表")
@Schema(description = "采购发票")
public class PurchaseInvoiceEntity extends BaseEntity<PurchaseInvoiceEntity> {

    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;

    @Index
    @ColumnDefine(comment = "采购订单ID", notNull = true)
    @Schema(description = "采购订单ID")
    private Long orderId;

    @Index
    @ColumnDefine(comment = "发票号码", length = 50, notNull = true)
    @Schema(description = "发票号码")
    private String invoiceNo;

    @ColumnDefine(comment = "发票类型", length = 50, notNull = true)
    @Schema(description = "发票类型")
    private String invoiceType;

    @ColumnDefine(comment = "开票日期", notNull = true)
    @Schema(description = "开票日期")
    private LocalDate invoiceDate;

    @Column("invoice_amount")
    @ColumnDefine(comment = "发票金额", type = "decimal(18,2)", notNull = true)
    @Schema(description = "发票金额")
    private BigDecimal amount;

    @ColumnDefine(comment = "税额", type = "decimal(18,2)")
    @Schema(description = "税额")
    private BigDecimal taxAmount;

    @ColumnDefine(comment = "价税合计", type = "decimal(18,2)", notNull = true)
    @Schema(description = "价税合计")
    private BigDecimal totalAmount;

    @ColumnDefine(comment = "销售方名称", length = 200)
    @Schema(description = "销售方名称")
    private String sellerName;

    @ColumnDefine(comment = "销售方税号", length = 50)
    @Schema(description = "销售方税号")
    private String sellerTaxNo;

    @Column("scan_file")
    @Schema(description = "发票扫描件URL")
    private String scanFileUrl;

    @ColumnDefine(comment = "状态：0-待开票 1-已开票 2-已作废", defaultValue = "0")
    @Schema(description = "状态：0-待开票 1-已开票 2-已作废")
    private Integer status;

    @ColumnDefine(comment = "删除原因", length = 500)
    @Schema(description = "删除原因")
    private String deleteReason;

    @ColumnDefine(comment = "备注", type = "text")
    @Schema(description = "备注")
    private String remark;
}
