package com.cool.modules.config.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 商品类别实体
 */
@Getter
@Setter
@Table(value = "erp_product_category", comment = "商品类别表")
@Schema(description = "商品类别")
public class ProductCategoryEntity extends BaseEntity<ProductCategoryEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Schema(description = "类别编码（唯一）")
    private String categoryCode;
    
    @Schema(description = "类别名称")
    private String categoryName;
    
    @Schema(description = "父级ID（支持树形结构）")
    private Long parentId;
    
    @Schema(description = "父级名称")
    private String parentName;
    
    @Schema(description = "层级（1-一级 2-二级 3-三级）")
    private Integer level;
    
    @Schema(description = "类别路径（如：1/2/5）")
    private String categoryPath;
    
    @Schema(description = "类别图标")
    private String categoryIcon;
    
    @Schema(description = "类别描述")
    private String description;
    
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;
    
    @Schema(description = "排序")
    private Integer sortOrder;
    
    @Schema(description = "备注")
    private String remark;
}




