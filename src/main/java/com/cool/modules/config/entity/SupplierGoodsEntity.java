package com.cool.modules.config.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 供应商-商品关联表
 */
@Getter
@Setter
@Table(value = "erp_supplier_goods", comment = "供应商商品关联表")
@Schema(description = "供应商商品关联")
public class SupplierGoodsEntity extends BaseEntity<SupplierGoodsEntity> {

    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "商品ID")
    private Long goodsId;
}
