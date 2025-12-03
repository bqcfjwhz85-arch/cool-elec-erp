package com.cool.modules.source.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 服务商报备商品明细实体
 */
@Getter
@Setter
@Table(value = "erp_source_item", comment = "服务商报备商品明细表")
@Schema(description = "报备商品明细")
public class SourceItemEntity extends BaseEntity<SourceItemEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Schema(description = "报备单ID")
    private Long sourceId;
    
    @Schema(description = "商品SKU")
    private String productSku;
    
    @Schema(description = "商品名称")
    private String productName;
    
    @Schema(description = "品牌")
    private String brand;
    
    @Schema(description = "规格型号")
    private String specification;
    
    @Schema(description = "单位")
    private String unit;
    
    @Schema(description = "数量")
    private Integer quantity;
    
    @Schema(description = "国网价（单价）")
    private BigDecimal stateGridPrice;
    
    @Schema(description = "小计金额 = 国网价 × 数量")
    private BigDecimal subtotal;
    
    @Schema(description = "备注")
    private String remark;
}
