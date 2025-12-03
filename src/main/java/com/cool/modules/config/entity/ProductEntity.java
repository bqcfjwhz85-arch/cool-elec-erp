package com.cool.modules.config.entity;

import com.cool.core.annotation.ColumnDefine;
import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import com.cool.modules.config.entity.SupplierEntity;

/**
 * 商品信息实体
 */
@Getter
@Setter
@Table(value = "erp_product", comment = "商品信息表")
@Schema(description = "商品信息")
public class ProductEntity extends BaseEntity<ProductEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Schema(description = "商品SKU（唯一）")
    private String productSku;
    
    @Schema(description = "商品名称")
    private String productName;
    
    @Schema(description = "品牌ID（关联erp_brand.id）")
    private Long brandId;
    
    @Schema(description = "型号ID（关联erp_product_model.id）")
    private Long modelId;
    
    @Schema(description = "品牌名称（已废弃，建议使用brandId）")
    @Deprecated
    private String brand;
    
    @Schema(description = "型号名称（已废弃，建议使用modelId）")
    @Deprecated
    private String model;
    
    @Schema(description = "规格")
    private String specification;
    
    @Schema(description = "单位")
    private String unit;
    
    @Schema(description = "类别ID（关联erp_product_category.id）")
    private Long categoryId;
    
    @Schema(description = "商品类别名称（已废弃，建议使用categoryId）")
    @Deprecated
    private String category;
    
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;
    
    @Schema(description = "备注")
    private String remark;

    @Schema(description = "在架平台ID集合（逗号分隔）")
    private String platformIds;

    @Schema(description = "在架平台名称集合（逗号分隔）")
    @Column(ignore = true)
    private String platformNames;

    @Schema(description = "自定义数据(JSON)")
    @ColumnDefine(type = "text")
    private String customData;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商信息")
    @Column(ignore = true)
    private SupplierEntity supplier;

    @Schema(description = "国网价")
    @Column(ignore = true)
    private java.math.BigDecimal price;

    @Schema(description = "供应商ID集合")
    @Column(ignore = true)
    private java.util.List<Long> supplierIds;
}

