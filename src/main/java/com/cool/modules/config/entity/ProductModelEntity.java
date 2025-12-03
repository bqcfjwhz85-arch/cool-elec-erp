package com.cool.modules.config.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 商品型号实体
 */
@Getter
@Setter
@Table(value = "erp_product_model", comment = "商品型号表")
@Schema(description = "商品型号")
public class ProductModelEntity extends BaseEntity<ProductModelEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Schema(description = "型号编码（唯一）")
    private String modelCode;
    
    @Schema(description = "型号名称")
    private String modelName;
    
    @Schema(description = "品牌ID")
    private Long brandId;
    
    @Schema(description = "品牌名称（冗余）")
    private String brandName;
    
    @Schema(description = "类别ID（关联erp_product_category.id）")
    private Long categoryId;
    
    @Schema(description = "类别名称（冗余）")
    private String categoryName;
    
    @Schema(description = "商品类别（已废弃，建议使用categoryId）")
    @Deprecated
    private String category;
    
    @Schema(description = "规格参数")
    private String specification;
    
    @Schema(description = "计量单位")
    private String unit;
    
    @Schema(description = "功率")
    private String power;
    
    @Schema(description = "电压")
    private String voltage;
    
    @Schema(description = "型号说明")
    private String description;
    
    @Schema(description = "状态：0-停产 1-在产")
    private Integer status;
    
    @Schema(description = "排序")
    private Integer sortOrder;
    
    @Schema(description = "备注")
    private String remark;
}

