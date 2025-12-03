package com.cool.modules.config.entity;

import com.cool.core.annotation.ColumnDefine;
import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import org.dromara.autotable.annotation.Index;
import lombok.Getter;
import lombok.Setter;

/**
 * 平台信息实体
 */
@Getter
@Setter
@Table(value = "erp_platform", comment = "平台信息表")
@Schema(description = "平台信息")
public class PlatformEntity extends BaseEntity<PlatformEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Index
    @ColumnDefine(comment = "平台编码（唯一）", length = 50, notNull = true)
    @Schema(description = "平台编码（唯一）")
    private String platformCode;
    
    @ColumnDefine(comment = "平台名称", length = 100, notNull = true)
    @Schema(description = "平台名称")
    private String platformName;
    
    @ColumnDefine(comment = "平台网址", length = 200)
    @Schema(description = "平台网址")
    private String platformUrl;
    
    @ColumnDefine(comment = "默认点位", defaultValue = "0")
    @Schema(description = "默认点位")
    private Integer defaultPoint;
    
    @ColumnDefine(comment = "联系人", length = 50)
    @Schema(description = "联系人")
    private String contactPerson;
    
    @ColumnDefine(comment = "联系电话", length = 20)
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @ColumnDefine(comment = "联系地址", length = 200)
    @Schema(description = "联系地址")
    private String contactAddress;
    
    @ColumnDefine(comment = "订单流转模式：1-自动 2-手动", defaultValue = "1")
    @Schema(description = "订单流转模式：1-自动 2-手动")
    private Integer flowMode;
    
    @ColumnDefine(comment = "状态：0-禁用 1-启用", defaultValue = "1")
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;
    
    @ColumnDefine(comment = "备注", type = "text")
    @Schema(description = "备注")
    private String remark;

    @Schema(description = "自定义数据(JSON)")
    @ColumnDefine(type = "text")
    private String customData;
}
