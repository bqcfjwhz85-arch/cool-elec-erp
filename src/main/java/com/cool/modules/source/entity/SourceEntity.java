package com.cool.modules.source.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.RelationOneToMany;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单来源（服务商报备）实体
 */
@Getter
@Setter
@Table(value = "erp_source_order", comment = "订单来源报备表")
@Schema(description = "订单来源报备")
public class SourceEntity extends BaseEntity<SourceEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Schema(description = "报备编号（唯一）")
    private String sourceNo;
    
    @Schema(description = "报备公司名称（服务商）")
    private String providerName;
    
    @Schema(description = "服务商ID")
    private Long providerId;
    
    @Schema(description = "PR单号")
    private String prNo;
    
    @Schema(description = "ERP单号")
    private String erpNo;
    
    @Schema(description = "订单时间")
    private LocalDateTime orderTime;
    
    @Schema(description = "报备商品名称（单商品时使用）")
    private String productName;
    
    @Schema(description = "商品SKU（单商品时使用）")
    private String productSku;
    
    @Schema(description = "报备商品数量（单商品时使用）")
    private Integer quantity;
    
    @Column(ignore = true)
    @Schema(description = "报备商品品牌（列表展示用）")
    private String brand;
    
    @Column(ignore = true)
    @Schema(description = "报备商品名称集合（列表展示用）")
    private String productNames;
    
    @Schema(description = "国网价（单商品时使用）")
    private BigDecimal netPrice;
    
    @Schema(description = "报备商品总金额")
    private BigDecimal totalAmount;
    
    @Schema(description = "下单平台")
    private String platformName;
    
    @Schema(description = "平台ID")
    private Long platformId;
    
    @Schema(description = "订单方式：0-借码 1-实供")
    private Integer orderMode;
    
    @Schema(description = "采购单位名称（客户）")
    private String customerName;
    
    @Schema(description = "客户ID")
    private Long customerId;
    
    @Schema(description = "订单所属区域（省份）")
    private String orderRegion;
    
    @Schema(description = "国网采购单位")
    private String stateGridUnit;
    
    @Schema(description = "是否区域服务商：0-否 1-是")
    private Integer isRegional;
    
    @Schema(description = "区域编码")
    private String regionCode;
    
    @Schema(description = "区域名称")
    private String regionName;
    
    @Schema(description = "报备状态：0-待审核 1-审核通过 2-审核驳回 3-已生成订单")
    private Integer status;
    
    @Schema(description = "审核人ID")
    private Long reviewerId;
    
    @Schema(description = "审核人姓名")
    private String reviewerName;
    
    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;
    
    @Schema(description = "驳回原因")
    private String rejectReason;
    
    @Schema(description = "订单凭证图片URL（多个用逗号分隔）")
    private String voucherUrl;
    
    @Schema(description = "OCR识别结果JSON")
    private String ocrResult;
    
    @Schema(description = "OCR识别置信度（0-1）")
    private BigDecimal ocrConfidence;
    
    @Schema(description = "校验结果：0-未校验 1-校验通过 2-校验失败")
    private Integer validateStatus;
    
    @Schema(description = "校验错误信息")
    private String validateError;
    
    @Schema(description = "关联销售订单ID")
    private Long salesOrderId;
    
    @Schema(description = "关联销售订单号")
    private String salesOrderNo;
    
    @Schema(description = "备注")
    private String remark;
    
    @Schema(description = "创建人ID")
    private Long creatorId;
    
    @Schema(description = "创建人姓名")
    private String creatorName;
    
    // 一对多关联：报备商品明细
    @Column(ignore = true)
    @RelationOneToMany(selfField = "id", targetField = "sourceId")
    @Schema(description = "报备商品明细")
    private List<SourceItemEntity> items;
}
