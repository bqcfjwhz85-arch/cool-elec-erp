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
 * 供应商信息实体
 */
@Getter
@Setter
@Table(value = "erp_supplier", comment = "供应商信息表")
@Schema(description = "供应商信息")
public class SupplierEntity extends BaseEntity<SupplierEntity> {

    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "供应商编码（系统自动生成）")
    private String supplierCode;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "统一社会信用代码（非必填）")
    private String socialCreditCode;

    @Schema(description = "是否区域供应商：0-否 1-是")
    private Integer isRegional;

    @Schema(description = "区域范围（区域供应商必填，多个用逗号分隔）")
    private String regionScope;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "注册地址")
    private String registeredAddress;

    @Schema(description = "开户行")
    private String bankName;

    @Schema(description = "银行账号")
    private String bankAccount;

    @Schema(description = "发票类型偏好：1-专票 2-普票 3-电子普票 4-电子专票")
    private Integer invoiceType;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Schema(description = "微信号")
    private String wechat;

    @Schema(description = "座机电话")
    private String telephone;

    @Schema(description = "省份")
    private String deliveryProvince;

    @Schema(description = "城市")
    private String deliveryCity;

    @Schema(description = "区县")
    private String deliveryDistrict;

    @Schema(description = "详细地址（送货地址）")
    private String contactAddress;

    @Schema(description = "负责人")
    private String manager;

    @Schema(description = "附件列表(JSON)")
    @ColumnDefine(type = "text")
    private String attachments;

    @Schema(description = "备注")
    private String remark;
}
