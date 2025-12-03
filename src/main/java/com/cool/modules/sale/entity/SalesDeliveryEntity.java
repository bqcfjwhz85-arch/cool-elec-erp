package com.cool.modules.sale.entity;

import com.cool.core.annotation.ColumnDefine;
import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.dromara.autotable.annotation.Index;

import java.time.LocalDateTime;

/**
 * 发货信息实体
 */
@Getter
@Setter
@Table(value = "erp_sales_delivery", comment = "销售订单发货信息表")
@Schema(description = "发货信息")
public class SalesDeliveryEntity extends BaseEntity<SalesDeliveryEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Index
    @ColumnDefine(comment = "订单ID", notNull = true)
    @Schema(description = "订单ID")
    private Long orderId;
    
    @ColumnDefine(comment = "发货单号", length = 50)
    @Schema(description = "发货单号")
    private String deliveryNo;
    
    @ColumnDefine(comment = "物流公司", length = 100)
    @Schema(description = "物流公司")
    private String logisticsCompany;
    
    @ColumnDefine(comment = "物流单号", length = 100)
    @Schema(description = "物流单号")
    private String trackingNo;
    
    @ColumnDefine(comment = "发货数量", notNull = true)
    @Schema(description = "发货数量")
    private Integer quantity;
    
    @ColumnDefine(comment = "发货时间", notNull = true)
    @Schema(description = "发货时间")
    private LocalDateTime deliveryTime;
    
    @ColumnDefine(comment = "发货人", length = 50)
    @Schema(description = "发货人")
    private String deliveryBy;
    
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
    
    @ColumnDefine(comment = "收货人", length = 50)
    @Schema(description = "收货人")
    private String deliveryContact;
    
    @ColumnDefine(comment = "收货电话", length = 20)
    @Schema(description = "收货电话")
    private String deliveryPhone;
    
    @ColumnDefine(comment = "状态：0-待收货 1-已收货", defaultValue = "0")
    @Schema(description = "状态：0-待收货 1-已收货")
    private Integer status;
    
    @ColumnDefine(comment = "删除原因", length = 500)
    @Schema(description = "删除原因")
    private String deleteReason;
    
    @ColumnDefine(comment = "备注", type = "text")
    @Schema(description = "备注")
    private String remark;
}






















