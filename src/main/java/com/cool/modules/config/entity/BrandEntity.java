package com.cool.modules.config.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 品牌信息实体
 */
@Getter
@Setter
@Table(value = "erp_brand", comment = "品牌信息表")
@Schema(description = "品牌信息")
public class BrandEntity extends BaseEntity<BrandEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Schema(description = "品牌编码（唯一）")
    private String brandCode;
    
    @Schema(description = "品牌名称")
    private String brandName;
    
    @Schema(description = "品牌英文名")
    private String brandNameEn;
    
    @Schema(description = "品牌Logo URL")
    private String brandLogo;
    
    @Schema(description = "制造商")
    private String manufacturer;
    
    @Schema(description = "国家/地区")
    private String country;
    
    @Schema(description = "主营类别")
    private String category;
    
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;
    
    @Schema(description = "排序")
    private Integer sortOrder;
    
    @Schema(description = "备注")
    private String remark;
}




