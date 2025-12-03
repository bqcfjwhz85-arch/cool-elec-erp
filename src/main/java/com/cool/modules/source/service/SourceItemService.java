package com.cool.modules.source.service;

import com.cool.core.base.BaseService;
import com.cool.modules.source.entity.SourceItemEntity;

import java.util.List;

/**
 * 服务商报备商品明细服务接口
 */
public interface SourceItemService extends BaseService<SourceItemEntity> {
    
    /**
     * 根据报备单ID查询商品明细列表
     * 
     * @param sourceId 报备单ID
     * @return 商品明细列表
     */
    List<SourceItemEntity> listBySourceId(Long sourceId);
    
    /**
     * 批量保存商品明细
     * 
     * @param sourceId 报备单ID
     * @param items 商品明细列表
     */
    void batchSave(Long sourceId, List<SourceItemEntity> items);
}

