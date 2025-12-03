package com.cool.modules.purchase.service.impl;

import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.purchase.entity.PurchaseOrderItemEntity;
import com.cool.modules.purchase.mapper.PurchaseOrderItemMapper;
import com.cool.modules.purchase.service.PurchaseOrderItemService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.cool.modules.purchase.entity.table.PurchaseOrderItemEntityTableDef.PURCHASE_ORDER_ITEM_ENTITY;

/**
 * 采购订单商品明细服务实现类
 */
@Slf4j
@Service
public class PurchaseOrderItemServiceImpl extends BaseServiceImpl<PurchaseOrderItemMapper, PurchaseOrderItemEntity>
        implements PurchaseOrderItemService {
    
    @Override
    public List<PurchaseOrderItemEntity> listByOrderId(Long orderId) {
        return list(QueryWrapper.create()
            .where(PURCHASE_ORDER_ITEM_ENTITY.ORDER_ID.eq(orderId)));
    }
    
    @Override
    public void deleteByOrderId(Long orderId) {
        remove(QueryWrapper.create()
            .where(PURCHASE_ORDER_ITEM_ENTITY.ORDER_ID.eq(orderId)));
    }
}






