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

/**
 * 销售订单商品明细实体
 */
@Getter
@Setter
@Table(value = "erp_sales_order_item", comment = "销售订单商品明细表")
@Schema(description = "销售订单商品明细")
public class SalesOrderItemEntity extends BaseEntity<SalesOrderItemEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Index
    @ColumnDefine(comment = "订单ID", notNull = true)
    @Schema(description = "订单ID")
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
    @Schema(description = "数量")
    private Integer quantity;
    
    @ColumnDefine(comment = "单价", type = "decimal(18,2)", notNull = true)
    @Schema(description = "单价")
    private BigDecimal price;
    
    @ColumnDefine(comment = "点位（单位：%，点位为1代表1%）", type = "decimal(10,4)")
    @Schema(description = "点位（借码订单使用，单位：%，点位为1代表1%）")
    private BigDecimal points;
    
    @ColumnDefine(comment = "小计金额", type = "decimal(18,2)", notNull = true)
    @Schema(description = "小计金额")
    private BigDecimal amount;
    
    @ColumnDefine(comment = "备注", type = "text")
    @Schema(description = "备注")
    private String remark;
}








