package com.cool.modules.config.entity;

import com.cool.core.annotation.ColumnDefine;
import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 服务商配置实体
 */
@Getter
@Setter
@Table(value = "erp_provider", comment = "服务商配置表")
@Schema(description = "服务商配置")
public class ProviderEntity extends BaseEntity<ProviderEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Schema(description = "服务商编码（唯一）")
    private String providerCode;
    
    @Schema(description = "服务商名称（营业执照全称）")
    private String providerName;
    
    @Schema(description = "简称（合同/报表里打印用）")
    private String shortName;
    
    @Schema(description = "统一社会信用代码（唯一，用于税务对账、发票抬头校验）")
    private String socialCreditCode;
    
    @Schema(description = "法定代表人")
    private String legalRepresentative;
    
    @Schema(description = "注册地址（合同模板字段）")
    private String registeredAddress;
    
    @Schema(description = "联系人")
    private String contactPerson;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "微信号")
    private String wechat;
    
    @Schema(description = "联系地址")
    private String contactAddress;
    
    @Schema(description = "收货省份")
    private String deliveryProvince;
    
    @Schema(description = "收货城市")
    private String deliveryCity;
    
    @Schema(description = "收货区县")
    private String deliveryDistrict;
    
    @Schema(description = "开户行")
    private String bankName;
    
    @Schema(description = "银行账号（前端需脱敏显示）")
    private String bankAccount;
    
    @Schema(description = "是否区域服务商：0-否 1-是")
    private Integer isRegional;
    
    @Schema(description = "默认点位（借码订单默认点位，单位：%，点位为1代表1%）")
    private java.math.BigDecimal defaultPoint;
    
    @Schema(description = "区域范围（区域服务商必填，多个用逗号分隔）")
    private String regionScope;
    
    @Schema(description = "允许报备区域（非区域服务商必填，多个用逗号分隔）")
    private String allowedRegions;
    
    @Schema(description = "允许报备商品（多个SKU用逗号分隔，为空表示全部）")
    private String allowedProducts;
    
    @Schema(description = "关联用户ID")
    private Long userId;
    
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;
    
    @Schema(description = "备注")
    private String remark;

    @Schema(description = "自定义数据(JSON)")
    @ColumnDefine(type = "text")
    private String customData;
}

