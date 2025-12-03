package com.cool.modules.purchase.mapper;

import com.cool.modules.purchase.entity.PurchaseOrderEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购订单 Mapper
 */
@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrderEntity> {
}

