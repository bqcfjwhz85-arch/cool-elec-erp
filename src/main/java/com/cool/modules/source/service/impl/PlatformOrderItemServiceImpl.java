package com.cool.modules.source.service.impl;

import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.source.entity.PlatformOrderItemEntity;
import com.cool.modules.source.mapper.PlatformOrderItemMapper;
import com.cool.modules.source.service.PlatformOrderItemService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.cool.modules.source.entity.table.PlatformOrderItemEntityTableDef.PLATFORM_ORDER_ITEM_ENTITY;

/**
 * 平台订单商品明细服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformOrderItemServiceImpl extends BaseServiceImpl<PlatformOrderItemMapper, PlatformOrderItemEntity>
        implements PlatformOrderItemService {
    
    @Override
    public List<PlatformOrderItemEntity> listByPlatformOrderId(Long platformOrderId) {
        return list(QueryWrapper.create()
            .where(PLATFORM_ORDER_ITEM_ENTITY.PLATFORM_ORDER_ID.eq(platformOrderId))
            .orderBy(PLATFORM_ORDER_ITEM_ENTITY.ID, true));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(Long platformOrderId, List<PlatformOrderItemEntity> items) {
        if (items != null && !items.isEmpty()) {
            items.forEach(item -> item.setPlatformOrderId(platformOrderId));
            saveBatch(items);
            log.info("批量保存平台订单商品明细成功，platformOrderId: {}, 明细数量: {}", platformOrderId, items.size());
        }
    }
}

