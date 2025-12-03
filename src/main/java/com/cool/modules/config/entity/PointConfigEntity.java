package com.cool.modules.config.entity;

import com.cool.core.annotation.ColumnDefine;
import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

/**
 * 点位配置实体
 * 点位单位：%，点位为1代表1%
 */
@Getter
@Setter
@Table(value = "erp_point_config", comment = "点位配置表（点位单位：%）")
@Schema(description = "点位配置（点位单位：%，点位为1代表1%）")
public class PointConfigEntity extends BaseEntity<PointConfigEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Schema(description = "配置类型：1-平台固定点位 2-服务商借码默认点位")
    private Integer configType;
    
    @Schema(description = "平台ID（平台固定点位必填）")
    private Long platformId;
    
    @Schema(description = "平台名称")
    private String platformName;
    
    @Schema(description = "区域代码（可选）")
    private String regionCode;
    
    @Schema(description = "区域名称")
    private String regionName;
    
    @Schema(description = "品牌ID（关联erp_brand.id）")
    private Long brandId;
    
    @Schema(description = "型号ID（关联erp_product_model.id）")
    private Long modelId;
    
    @Schema(description = "品牌名称（冗余）")
    private String brand;
    
    @Schema(description = "型号名称（冗余）")
    private String model;
    
    @Schema(description = "类别ID（关联erp_product_category.id）")
    private Long categoryId;
    
    @Schema(description = "类别名称（冗余）")
    private String categoryName;
    
    @Schema(description = "服务商ID（服务商点位必填）")
    private Long providerId;
    
    @Schema(description = "服务商名称")
    private String providerName;
    
    @Schema(description = "商品类别（已废弃，建议使用categoryId）")
    @Deprecated
    private String category;
    
    @Schema(description = "点位（单位：%，点位为1代表1%）")
    private BigDecimal points;
    
    @Schema(description = "优先级（数字越大优先级越高）")
    private Integer priority;
    
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;
    
    @Schema(description = "备注")
    private String remark;

    @Schema(description = "自定义数据(JSON)")
    @ColumnDefine(type = "text")
    private String customData;
}

