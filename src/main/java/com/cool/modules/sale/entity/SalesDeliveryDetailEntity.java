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

/**
 * 销售订单发货明细实体
 */
@Getter
@Setter
@Table(value = "erp_sales_delivery_detail", comment = "销售订单发货明细表")
@Schema(description = "发货明细")
public class SalesDeliveryDetailEntity extends BaseEntity<SalesDeliveryDetailEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Index
    @ColumnDefine(comment = "发货记录ID", notNull = true)
    @Schema(description = "发货记录ID")
    private Long deliveryId;
    
    @Index
    @ColumnDefine(comment = "订单商品ID", notNull = true)
    @Schema(description = "订单商品ID")
    private Long orderItemId;
    
    @ColumnDefine(comment = "本次发货数量", notNull = true)
    @Schema(description = "本次发货数量")
    private Integer quantity;
}
