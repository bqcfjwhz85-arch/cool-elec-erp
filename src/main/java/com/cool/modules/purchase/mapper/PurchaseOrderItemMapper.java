package com.cool.modules.purchase.mapper;

import com.cool.modules.purchase.entity.PurchaseOrderItemEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购订单商品明细 Mapper
 */
@Mapper
public interface PurchaseOrderItemMapper extends BaseMapper<PurchaseOrderItemEntity> {
}

