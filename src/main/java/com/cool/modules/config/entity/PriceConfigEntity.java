package com.cool.modules.config.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格配置实体
 */
@Getter
@Setter
@Table(value = "erp_price_config", comment = "价格配置表")
@Schema(description = "价格配置")
public class PriceConfigEntity extends BaseEntity<PriceConfigEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Schema(description = "商品SKU")
    private String productSku;
    
    @Schema(description = "商品名称")
    private String productName;
    
    @Schema(description = "价格类型：1-国网价 2-区域价 3-服务商价 4-结算价")
    private Integer priceType;
    
    @Schema(description = "价格")
    private BigDecimal price;
    
    @Schema(description = "国网价（冗余字段，priceType=1时使用）")
    private BigDecimal stateGridPrice;
    
    @Schema(description = "区域价（冗余字段，priceType=2时使用）")
    private BigDecimal regionalPrice;
    
    @Schema(description = "服务商价（冗余字段，priceType=3时使用）")
    private BigDecimal providerPrice;
    
    @Schema(description = "结算价（冗余字段，priceType=4时使用）")
    private BigDecimal settlementPrice;
    
    @Schema(description = "区域代码（区域价必填）")
    private String regionCode;
    
    @Schema(description = "区域名称")
    private String regionName;
    
    @Schema(description = "平台ID（区域价可选，NULL表示全平台通用）")
    private Long platformId;
    
    @Schema(description = "平台名称")
    private String platformName;
    
    @Schema(description = "省份名称（区域价使用）")
    private String province;
    
    @Schema(description = "是否默认：0-否 1-是")
    private Integer isDefault;
    
    // 允许前端传Boolean类型,自动转换为Integer
    @com.fasterxml.jackson.annotation.JsonSetter("isDefault")
    public void setIsDefaultFromBoolean(Object value) {
        if (value instanceof Boolean) {
            this.isDefault = (Boolean) value ? 1 : 0;
        } else if (value instanceof Integer) {
            this.isDefault = (Integer) value;
        } else if (value instanceof Number) {
            this.isDefault = ((Number) value).intValue();
        } else if (value == null) {
            this.isDefault = null;
        }
    }
    
    @Schema(description = "服务商ID（服务商专属价必填）")
    private Long providerId;
    
    @Schema(description = "服务商名称")
    private String providerName;
    
    @Schema(description = "生效时间")
    private LocalDateTime effectiveTime;
    
    @Schema(description = "失效时间")
    private LocalDateTime expiryTime;
    
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;
    
    @Schema(description = "备注")
    private String remark;
}

