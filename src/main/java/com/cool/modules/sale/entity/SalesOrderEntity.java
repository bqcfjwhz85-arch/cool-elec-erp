package com.cool.modules.sale.entity;

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
 * 销售订单实体
 */
@Getter
@Setter
@Table(value = "erp_sales_order", comment = "销售订单表")
@Schema(description = "销售订单")
public class SalesOrderEntity extends BaseEntity<SalesOrderEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Index
    @ColumnDefine(comment = "订单号", length = 50, notNull = true)
    @Schema(description = "订单号")
    private String orderNo;
    
    @ColumnDefine(comment = "订单来源类型：1-服务商报备 2-平台订单 3-手动创建", notNull = true)
    @Schema(description = "订单来源类型：1-服务商报备 2-平台订单 3-手动创建")
    private Integer sourceType;
    
    @ColumnDefine(comment = "来源ID（报备ID或平台订单ID）")
    @Schema(description = "来源ID")
    private Long sourceId;
    
    @ColumnDefine(comment = "订单类型：1-借码 2-实供", notNull = true)
    @Schema(description = "订单类型：1-借码 2-实供")
    private Integer orderType;
    
    @ColumnDefine(comment = "是否区域订单：0-否 1-是", defaultValue = "0")
    @Schema(description = "是否区域订单")
    private Integer isRegional;
    
    @Index
    @ColumnDefine(comment = "客户ID")
    @Schema(description = "客户ID")
    private Long customerId;
    
    @ColumnDefine(comment = "客户名称", length = 200)
    @Schema(description = "客户名称")
    private String customerName;
    
    @Index
    @ColumnDefine(comment = "服务商ID")
    @Schema(description = "服务商ID")
    private Long providerId;
    
    @ColumnDefine(comment = "服务商名称", length = 200)
    @Schema(description = "服务商名称")
    private String providerName;
    
    @ColumnDefine(comment = "区域代码", length = 50)
    @Schema(description = "区域代码")
    private String regionCode;
    
    @ColumnDefine(comment = "平台ID")
    @Schema(description = "平台ID")
    private Long platformId;
    
    @ColumnDefine(comment = "平台名称", length = 100)
    @Schema(description = "平台名称")
    private String platformName;
    
    @ColumnDefine(comment = "平台订单号", length = 100)
    @Schema(description = "平台订单号")
    private String platformOrderNo;
    
    @ColumnDefine(comment = "报备单号（服务商报备来源）", length = 100)
    @Schema(description = "报备单号（服务商报备来源）")
    private String reportNo;
    
    @ColumnDefine(comment = "订单总金额", type = "decimal(18,2)", notNull = true, defaultValue = "0.00")
    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;
    
    @ColumnDefine(comment = "订单总数量", defaultValue = "0")
    @Schema(description = "订单总数量")
    private Integer totalQuantity;
    
    @ColumnDefine(comment = "点位（单位：%，点位为1代表1%）", type = "decimal(10,4)")
    @Schema(description = "点位（借码订单，单位：%，点位为1代表1%）")
    private BigDecimal points;
    
    @ColumnDefine(comment = "创建方式：1-自动创建 2-手动创建", notNull = true)
    @Schema(description = "创建方式：1-自动创建 2-手动创建")
    private Integer createMode;
    
    @ColumnDefine(comment = "订单状态：0-待确认 1-已确认 2-采购中 3-已完成 4-已取消", notNull = true, defaultValue = "0")
    @Schema(description = "订单状态：0-待确认 1-已确认 2-采购中 3-已完成 4-已取消")
    private Integer status;
    
    @ColumnDefine(comment = "审批状态：0-待审核 1-审核通过 2-审核驳回", defaultValue = "0")
    @Schema(description = "审批状态：0-待审核 1-审核通过 2-审核驳回")
    private Integer approvalStatus;

    @ColumnDefine(comment = "驳回原因", type = "text")
    @Schema(description = "驳回原因")
    private String rejectReason;
    
    @ColumnDefine(comment = "发货状态：0-未发货 1-部分发货 2-已发货", defaultValue = "0")
    @Schema(description = "发货状态：0-未发货 1-部分发货 2-已发货")
    private Integer deliveryStatus;
    
    @ColumnDefine(comment = "开票状态：0-未开票 1-部分开票 2-已开票", defaultValue = "0")
    @Schema(description = "开票状态：0-未开票 1-部分开票 2-已开票")
    private Integer invoiceStatus;
    
    @ColumnDefine(comment = "回款状态：0-未回款 1-部分回款 2-已回款", defaultValue = "0")
    @Schema(description = "回款状态：0-未回款 1-部分回款 2-已回款")
    private Integer paymentStatus;
    
    @ColumnDefine(comment = "已发货数量", defaultValue = "0")
    @Schema(description = "已发货数量")
    private Integer deliveredQuantity;
    
    @ColumnDefine(comment = "已开票金额", type = "decimal(18,2)", defaultValue = "0.00")
    @Schema(description = "已开票金额")
    private BigDecimal invoicedAmount;
    
    @ColumnDefine(comment = "已回款金额", type = "decimal(18,2)", defaultValue = "0.00")
    @Schema(description = "已回款金额")
    private BigDecimal paidAmount;
    
    @ColumnDefine(comment = "流转状态：0-未流转 1-已流转 2-已处理", defaultValue = "0")
    @Schema(description = "流转状态：0-未流转 1-已流转 2-已处理")
    private Integer flowStatus;
    
    @ColumnDefine(comment = "流转接收人ID")
    @Schema(description = "流转接收人ID")
    private Long flowReceiverId;
    
    @ColumnDefine(comment = "流转时间")
    @Schema(description = "流转时间")
    private LocalDateTime flowTime;
    
    @ColumnDefine(comment = "收货地址", length = 500)
    @Schema(description = "收货地址")
    private String deliveryAddress;
    
    @ColumnDefine(comment = "收货人", length = 50)
    @Schema(description = "收货人")
    private String deliveryContact;
    
    @ColumnDefine(comment = "收货电话", length = 20)
    @Schema(description = "收货电话")
    private String deliveryPhone;
    
    @ColumnDefine(comment = "收货省份", length = 50)
    @Schema(description = "收货省份")
    private String deliveryProvince;
    
    @ColumnDefine(comment = "收货城市", length = 50)
    @Schema(description = "收货城市")
    private String deliveryCity;
    
    @ColumnDefine(comment = "收货区县", length = 50)
    @Schema(description = "收货区县")
    private String deliveryDistrict;
    
    @ColumnDefine(comment = "发票类型：1-增值税专用发票 2-增值税普通发票 3-电子普通发票 4-电子专用发票 5-收据", defaultValue = "1")
    @Schema(description = "发票类型：1-增值税专用发票 2-增值税普通发票 3-电子普通发票 4-电子专用发票 5-收据")
    private Integer invoiceType;
    
    @ColumnDefine(comment = "备注", type = "text")
    @Schema(description = "备注")
    private String remark;
    
    @ColumnDefine(comment = "创建人ID")
    @Schema(description = "创建人ID")
    private Long creatorId;
    
    @ColumnDefine(comment = "创建人姓名", length = 50)
    @Schema(description = "创建人姓名")
    private String creatorName;

    @Column(ignore = true)
    @Schema(description = "报备商品名称（逗号分隔）")
    private String productNames;
    
    // 一对多关联：订单商品明细
    @Column(ignore = true)
    @RelationOneToMany(selfField = "id", targetField = "orderId")
    @Schema(description = "订单商品明细")
    private List<SalesOrderItemEntity> items;
    
    // 一对多关联：发货信息
    @Column(ignore = true)
    @RelationOneToMany(selfField = "id", targetField = "orderId")
    @Schema(description = "发货信息")
    private List<SalesDeliveryEntity> deliveries;
    
    // 一对多关联：发票信息
    @Column(ignore = true)
    @RelationOneToMany(selfField = "id", targetField = "orderId")
    @Schema(description = "发票信息")
    private List<SalesInvoiceEntity> invoices;
    
    // 一对多关联：回款信息
    @Column(ignore = true)
    @RelationOneToMany(selfField = "id", targetField = "orderId")
    @Schema(description = "回款信息")
    private List<SalesPaymentEntity> payments;
}
