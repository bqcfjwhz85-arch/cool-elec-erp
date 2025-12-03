package com.cool.modules.source.service.impl;

import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.source.entity.SourceItemEntity;
import com.cool.modules.source.mapper.SourceItemMapper;
import com.cool.modules.source.service.SourceItemService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.cool.modules.source.entity.table.SourceItemEntityTableDef.SOURCE_ITEM_ENTITY;

/**
 * 服务商报备商品明细服务实现类
 */
@Service
@RequiredArgsConstructor
public class SourceItemServiceImpl extends BaseServiceImpl<SourceItemMapper, SourceItemEntity>
        implements SourceItemService {
    
    @Override
    public List<SourceItemEntity> listBySourceId(Long sourceId) {
        return list(QueryWrapper.create()
            .where(SOURCE_ITEM_ENTITY.SOURCE_ID.eq(sourceId))
            .orderBy(SOURCE_ITEM_ENTITY.ID, true));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(Long sourceId, List<SourceItemEntity> items) {
        if (items != null && !items.isEmpty()) {
            items.forEach(item -> item.setSourceId(sourceId));
            saveBatch(items);
        }
    }
}

