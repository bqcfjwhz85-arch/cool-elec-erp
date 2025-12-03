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

/**
 * 采购订单商品明细实体
 */
@Getter
@Setter
@Table(value = "erp_purchase_order_item", comment = "采购订单商品明细表")
@Schema(description = "采购订单商品明细")
public class PurchaseOrderItemEntity extends BaseEntity<PurchaseOrderItemEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Index
    @ColumnDefine(comment = "采购订单ID", notNull = true)
    @Schema(description = "采购订单ID")
    private Long orderId;
    
    @Index
    @ColumnDefine(comment = "商品SKU", length = 50, notNull = true)
    @Schema(description = "商品SKU")
    private String productSku;
    
    @ColumnDefine(comment = "商品名称", length = 200, notNull = true)
    @Schema(description = "商品名称")
    private String productName;
    
    @ColumnDefine(comment = "品牌", length = 100)
    @Schema(description = "品牌")
    private String brand;
    
    @ColumnDefine(comment = "规格型号", length = 100)
    @Schema(description = "规格型号")
    private String specification;
    
    @ColumnDefine(comment = "单位", length = 20)
    @Schema(description = "单位")
    private String unit;
    
    @ColumnDefine(comment = "数量", notNull = true)
    @Schema(description = "数量（≤来源销售数量）")
    private Integer quantity;
    
    @ColumnDefine(comment = "结算价", type = "decimal(18,2)", notNull = true)
    @Schema(description = "结算价（默认带协议价，可改）")
    private BigDecimal settlementPrice;
    
    @ColumnDefine(comment = "借码点位%", type = "decimal(10,4)")
    @Schema(description = "借码点位%（如果是借码且手动创建，弹窗输入；一键生成则带入销售点位）")
    private BigDecimal points;
    
    @ColumnDefine(comment = "行金额（结算价×数量）", type = "decimal(18,2)", notNull = true)
    @Schema(description = "行金额")
    private BigDecimal amount;
    
    @ColumnDefine(comment = "已开票金额", type = "decimal(18,2)", defaultValue = "0.00")
    @Schema(description = "已开票金额（累加更新）")
    private BigDecimal invoicedAmount;
    
    @ColumnDefine(comment = "已付款金额", type = "decimal(18,2)", defaultValue = "0.00")
    @Schema(description = "已付款金额（累加更新）")
    private BigDecimal paidAmount;
    
    @ColumnDefine(comment = "备注", type = "text")
    @Schema(description = "备注")
    private String remark;
}






