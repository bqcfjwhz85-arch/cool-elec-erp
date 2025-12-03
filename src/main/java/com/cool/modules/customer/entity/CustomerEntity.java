package com.cool.modules.customer.entity;

import com.cool.core.annotation.ColumnDefine;
import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.RelationOneToMany;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import org.dromara.autotable.annotation.Index;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 客户信息实体
 */
@Getter
@Setter
@Table(value = "erp_customer", comment = "客户信息表")
@Schema(description = "客户信息")
public class CustomerEntity extends BaseEntity<CustomerEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Index
    @ColumnDefine(comment = "客户编码（唯一）", length = 50, notNull = true)
    @Schema(description = "客户编码（唯一）")
    private String customerCode;
    
    @ColumnDefine(comment = "客户名称", length = 200, notNull = true)
    @Schema(description = "客户名称")
    private String customerName;
    
    @ColumnDefine(comment = "联系人", length = 50)
    @Schema(description = "联系人")
    private String contactPerson;
    
    @ColumnDefine(comment = "联系电话", length = 20)
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @ColumnDefine(comment = "联系地址", length = 200)
    @Schema(description = "联系地址")
    private String contactAddress;
    
    @ColumnDefine(comment = "所属区域", length = 50)
    @Schema(description = "所属区域")
    private String region;
    
    @ColumnDefine(comment = "收货地址", length = 500)
    @Schema(description = "收货地址")
    private String deliveryAddress;
    
    @ColumnDefine(comment = "收货省份", length = 50)
    @Schema(description = "收货省份")
    private String deliveryProvince;
    
    @ColumnDefine(comment = "收货城市", length = 50)
    @Schema(description = "收货城市")
    private String deliveryCity;
    
    @ColumnDefine(comment = "收货区县", length = 50)
    @Schema(description = "收货区县")
    private String deliveryDistrict;
    
    @ColumnDefine(comment = "客户类型：1-企业 2-个人", defaultValue = "1")
    @Schema(description = "客户类型：1-企业 2-个人")
    private Integer customerType;
    
    @ColumnDefine(comment = "状态：0-禁用 1-启用", defaultValue = "1")
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;
    
    @ColumnDefine(comment = "备注", type = "text")
    @Schema(description = "备注")
    private String remark;
    
    // 关联开票信息
    @Column(ignore = true)
    @RelationOneToMany(selfField = "id", targetField = "customerId")
    @Schema(description = "开票信息列表")
    private List<CustomerInvoiceEntity> invoiceInfoList;
}

