package com.cool.modules.source.service;

import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseService;
import com.cool.modules.source.entity.SourceEntity;
import java.util.List;

/**
 * 订单来源报备服务接口
 */
public interface SourceService extends BaseService<SourceEntity> {
    
    /**
     * OCR识别订单凭证
     * 
     * @param imageUrl 图片URL
     * @return 识别结果JSON
     */
    JSONObject recognizeVoucher(String imageUrl);
    
    /**
     * 校验报备信息
     * 
     * @param entity 报备实体
     * @return 校验结果
     */
    JSONObject validateSource(SourceEntity entity);
    
    /**
     * 审核报备单
     * 
     * @param id 报备单ID
     * @param passed 是否通过
     * @param reason 驳回原因（如果驳回）
     * @return 审核结果
     */
    boolean reviewSource(Long id, boolean passed, String reason);
    
    /**
     * 生成销售订单
     * 
     * @param sourceId 报备单ID
     * @param params 订单参数（如点位配置）
     * @return 销售订单ID
     */
    Long generateSalesOrder(Long sourceId, JSONObject params);

    /**
     * 批量生成销售订单
     *
     * @param sourceIds 报备单ID列表
     * @param params    参数
     * @return 销售订单ID
     */
    Long generateBatchSalesOrder(List<Long> sourceIds, JSONObject params);
    
    /**
     * 根据报备编号获取报备单
     * 
     * @param sourceNo 报备编号
     * @return 报备实体
     */
    SourceEntity getBySourceNo(String sourceNo);
}

