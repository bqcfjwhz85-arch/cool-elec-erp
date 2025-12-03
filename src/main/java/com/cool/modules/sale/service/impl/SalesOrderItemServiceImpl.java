package com.cool.modules.sale.service.impl;

import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.sale.entity.SalesOrderItemEntity;
import com.cool.modules.sale.mapper.SalesOrderItemMapper;
import com.cool.modules.sale.service.SalesOrderItemService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.cool.modules.sale.entity.table.SalesOrderItemEntityTableDef.SALES_ORDER_ITEM_ENTITY;

/**
 * 销售订单商品明细服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderItemServiceImpl extends BaseServiceImpl<SalesOrderItemMapper, SalesOrderItemEntity>
        implements SalesOrderItemService {
    
    @Override
    public List<SalesOrderItemEntity> listByOrderId(Long orderId) {
        return list(QueryWrapper.create()
            .where(SALES_ORDER_ITEM_ENTITY.ORDER_ID.eq(orderId))
            .orderBy(SALES_ORDER_ITEM_ENTITY.ID, true));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(Long orderId, List<SalesOrderItemEntity> items) {
        if (items != null && !items.isEmpty()) {
            items.forEach(item -> item.setOrderId(orderId));
            saveBatch(items);
        }
    }
}






















