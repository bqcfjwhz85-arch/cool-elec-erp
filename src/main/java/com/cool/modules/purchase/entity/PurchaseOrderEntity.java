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
 * 采购订单实体
 */
@Getter
@Setter
@Table(value = "erp_purchase_order", comment = "采购订单表")
@Schema(description = "采购订单")
public class PurchaseOrderEntity extends BaseEntity<PurchaseOrderEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Index
    @ColumnDefine(comment = "采购单号", length = 50, notNull = true)
    @Schema(description = "采购单号（PO+年月+6位流水）")
    private String orderNo;
    
    @ColumnDefine(comment = "来源类型：1-一键生成 2-手动创建", notNull = true)
    @Schema(description = "来源类型：1-一键生成 2-手动创建")
    private Integer sourceType;
    
    @Index
    @ColumnDefine(comment = "关联销售订单ID")
    @Schema(description = "关联销售订单ID")
    private Long salesOrderId;
    
    @ColumnDefine(comment = "来源销售单号", length = 50)
    @Schema(description = "来源销售单号")
    private String salesOrderNo;
    
    @ColumnDefine(comment = "销售金额", type = "decimal(18,2)", defaultValue = "0.00")
    @Schema(description = "销售金额（来源订单金额）")
    private BigDecimal salesAmount;
    
    @Index
    @ColumnDefine(comment = "供应商ID", notNull = true)
    @Schema(description = "供应商ID")
    private Long supplierId;
    
    @ColumnDefine(comment = "供应商名称", length = 200, notNull = true)
    @Schema(description = "供应商名称")
    private String supplierName;
    
    @ColumnDefine(comment = "供应商联系人", length = 50)
    @Schema(description = "供应商联系人")
    private String supplierContact;
    
    @ColumnDefine(comment = "供应商电话", length = 20)
    @Schema(description = "供应商电话")
    private String supplierPhone;
    
    @ColumnDefine(comment = "开户银行", length = 200)
    @Schema(description = "开户银行")
    private String bankName;
    
    @ColumnDefine(comment = "银行账号", length = 50)
    @Schema(description = "银行账号")
    private String bankAccount;
    
    @ColumnDefine(comment = "订单方式：1-借码 2-实供", notNull = true)
    @Schema(description = "订单方式：1-借码 2-实供")
    private Integer orderType;
    
    @ColumnDefine(comment = "结算价协议ID")
    @Schema(description = "结算价协议ID")
    private Long priceAgreementId;
    
    @ColumnDefine(comment = "结算价协议名称", length = 200)
    @Schema(description = "结算价协议名称")
    private String priceAgreementName;
    
    @ColumnDefine(comment = "采购日期", notNull = true)
    @Schema(description = "采购日期")
    private LocalDateTime purchaseDate;
    
    @ColumnDefine(comment = "采购金额（∑行金额）", type = "decimal(18,2)", notNull = true, defaultValue = "0.00")
    @Schema(description = "采购金额")
    private BigDecimal totalAmount;
    
    @ColumnDefine(comment = "订单总数量", defaultValue = "0")
    @Schema(description = "订单总数量")
    private Integer totalQuantity;
    
    @ColumnDefine(comment = "毛利（销售金额-采购金额）", type = "decimal(18,2)", defaultValue = "0.00")
    @Schema(description = "毛利")
    private BigDecimal profit;
    
    @ColumnDefine(comment = "订单状态：0-待确认 1-已确认 2-已完成 3-已取消", notNull = true, defaultValue = "0")
    @Schema(description = "订单状态：0-待确认 1-已确认 2-已完成 3-已取消")
    private Integer status;
    
    @ColumnDefine(comment = "审批状态：0-待审核 1-审核通过 2-审核驳回", defaultValue = "0")
    @Schema(description = "审批状态（手动创建时才有）")
    private Integer approvalStatus;

    @ColumnDefine(comment = "驳回原因", type = "text")
    @Schema(description = "驳回原因")
    private String rejectReason;
    
    @ColumnDefine(comment = "审批实例ID")
    @Schema(description = "审批实例ID（手动创建时才有）")
    private Long approveInstanceId;
    
    @ColumnDefine(comment = "付款状态：0-未付款 1-部分付款 2-已付清", defaultValue = "0")
    @Schema(description = "付款状态：0-未付款 1-部分付款 2-已付清")
    private Integer paymentStatus;
    
    @ColumnDefine(comment = "发票状态：0-未开票 1-部分开票 2-已开完", defaultValue = "0")
    @Schema(description = "发票状态：0-未开票 1-部分开票 2-已开完")
    private Integer invoiceStatus;
    
    @ColumnDefine(comment = "已付款金额", type = "decimal(18,2)", defaultValue = "0.00")
    @Schema(description = "已付款金额")
    private BigDecimal paidAmount;
    
    @ColumnDefine(comment = "已开票金额", type = "decimal(18,2)", defaultValue = "0.00")
    @Schema(description = "已开票金额")
    private BigDecimal invoicedAmount;
    
    @ColumnDefine(comment = "流转接收人ID")
    @Schema(description = "流转接收人ID")
    private Long flowReceiverId;
    
    @ColumnDefine(comment = "流转时间")
    @Schema(description = "流转时间")
    private LocalDateTime flowTime;
    
    @ColumnDefine(comment = "备注", type = "text")
    @Schema(description = "备注")
    private String remark;
    
    @ColumnDefine(comment = "创建人", length = 50)
    @Schema(description = "创建人")
    private String createBy;
    
    @ColumnDefine(comment = "创建人ID")
    @Schema(description = "创建人ID")
    private Long creatorId;
    
    @ColumnDefine(comment = "创建人姓名", length = 50)
    @Schema(description = "创建人姓名")
    private String creatorName;

    @Column(ignore = true)
    @Schema(description = "报备商品名称（逗号分隔）")
    private String productNames;
    
    // 一对多关联：采购订单商品明细
    @Column(ignore = true)
    @RelationOneToMany(selfField = "id", targetField = "orderId")
    @Schema(description = "采购订单商品明细")
    private List<PurchaseOrderItemEntity> items;
    
    // 一对多关联：发票信息
    @Column(ignore = true)
    @RelationOneToMany(selfField = "id", targetField = "orderId")
    @Schema(description = "发票信息")
    private List<PurchaseInvoiceEntity> invoices;
    
    // 一对多关联：付款信息
    @Column(ignore = true)
    @RelationOneToMany(selfField = "id", targetField = "orderId")
    @Schema(description = "付款信息")
    private List<PurchasePaymentEntity> payments;
}
