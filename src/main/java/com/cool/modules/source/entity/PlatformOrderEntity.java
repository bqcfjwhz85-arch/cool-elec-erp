package com.cool.modules.source.entity;

import com.cool.core.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.RelationOneToMany;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 平台订单实体
 */
@Getter
@Setter
@Table(value = "erp_platform_order", comment = "平台订单表")
@Schema(description = "平台订单")
public class PlatformOrderEntity extends BaseEntity<PlatformOrderEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Schema(description = "平台订单编号（唯一）")
    private String platformOrderNo;
    
    @Schema(description = "平台订单号（平台方提供）")
    private String platformNo;
    
    @Schema(description = "平台ID")
    private Long platformId;
    
    @Schema(description = "平台名称")
    private String platformName;
    
    @Schema(description = "服务商ID")
    private Long providerId;
    
    @Schema(description = "服务商名称")
    private String providerName;
    
    @Schema(description = "服务商所属区域")
    private String providerRegion;
    
    @Schema(description = "客户ID")
    private Long customerId;
    
    @Schema(description = "客户名称")
    private String customerName;
    
    @Schema(description = "客户地区")
    private String customerRegion;
    
    @Schema(description = "收货地址")
    private String shippingAddress;
    
    @Schema(description = "收货人")
    private String consignee;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;
    
    @Schema(description = "平台固定点位（单位：%，点位为1代表1%）")
    private BigDecimal platformPoints;
    
    @Schema(description = "发票类型：1-普通发票 2-专用发票")
    private Integer invoiceType;
    
    @Schema(description = "状态：0-待生成订单 1-已生成订单")
    private Integer status;
    
    @Schema(description = "关联销售订单ID")
    private Long salesOrderId;
    
    @Schema(description = "关联销售订单号")
    private String salesOrderNo;
    
    @Schema(description = "附件URL（数据库存储为逗号分隔的字符串）")
    private String attachmentUrls;
    
    @Schema(description = "备注")
    private String remark;
    
    @Schema(description = "创建人ID")
    private Long creatorId;
    
    @Schema(description = "创建人姓名")
    private String creatorName;
    
    // 一对多关联：订单商品明细
    @Column(ignore = true)
    @RelationOneToMany(selfField = "id", targetField = "platformOrderId")
    @Schema(description = "订单商品明细")
    private List<PlatformOrderItemEntity> items;
    
    /**
     * JSON序列化时，将逗号分隔的字符串转换为数组返回给前端
     */
    @JsonGetter("attachmentUrls")
    public List<String> getAttachmentUrlList() {
        if (attachmentUrls == null || attachmentUrls.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(attachmentUrls.split(","))
                .map(String::trim)
                .filter(url -> !url.isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * JSON反序列化时，接收数组并转换为逗号分隔的字符串
     */
    @JsonSetter("attachmentUrls")
    public void setAttachmentUrlList(Object value) {
        if (value == null) {
            this.attachmentUrls = null;
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) value;
            this.attachmentUrls = String.join(",", list);
        } else if (value instanceof String) {
            this.attachmentUrls = (String) value;
        }
    }
}
