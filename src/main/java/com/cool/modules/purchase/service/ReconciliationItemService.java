package com.cool.modules.purchase.service;

import com.cool.core.base.BaseService;
import com.cool.modules.purchase.entity.ReconciliationItemEntity;

import java.util.List;

/**
 * 对账单明细服务接口
 */
public interface ReconciliationItemService extends BaseService<ReconciliationItemEntity> {
    
    /**
     * 根据对账单ID查询明细列表
     * 
     * @param reconciliationId 对账单ID
     * @return 明细列表
     */
    List<ReconciliationItemEntity> listByReconciliationId(Long reconciliationId);
    
    /**
     * 批量删除对账单明细
     * 
     * @param reconciliationId 对账单ID
     */
    void deleteByReconciliationId(Long reconciliationId);
}






