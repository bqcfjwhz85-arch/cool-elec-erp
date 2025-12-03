package com.cool.modules.purchase.service.impl;

import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.purchase.entity.ReconciliationItemEntity;
import com.cool.modules.purchase.mapper.ReconciliationItemMapper;
import com.cool.modules.purchase.service.ReconciliationItemService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.cool.modules.purchase.entity.table.ReconciliationItemEntityTableDef.RECONCILIATION_ITEM_ENTITY;

/**
 * 对账单明细服务实现类
 */
@Slf4j
@Service
public class ReconciliationItemServiceImpl extends BaseServiceImpl<ReconciliationItemMapper, ReconciliationItemEntity>
        implements ReconciliationItemService {
    
    @Override
    public List<ReconciliationItemEntity> listByReconciliationId(Long reconciliationId) {
        return list(QueryWrapper.create()
            .where(RECONCILIATION_ITEM_ENTITY.RECONCILIATION_ID.eq(reconciliationId)));
    }
    
    @Override
    public void deleteByReconciliationId(Long reconciliationId) {
        remove(QueryWrapper.create()
            .where(RECONCILIATION_ITEM_ENTITY.RECONCILIATION_ID.eq(reconciliationId)));
    }
}






