package com.cool.modules.source.service;

import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseService;
import com.cool.modules.source.entity.PlatformOrderEntity;

import java.util.List;

/**
 * 平台订单服务接口
 */
public interface PlatformOrderService extends BaseService<PlatformOrderEntity> {
    
    /**
     * 根据平台订单编号获取订单
     * 
     * @param platformOrderNo 平台订单编号
     * @return 平台订单实体
     */
    PlatformOrderEntity getByPlatformOrderNo(String platformOrderNo);
    
    /**
     * 生成销售订单
     * 
     * @param platformOrderId 平台订单ID
     * @param params 订单参数
     * @return 销售订单ID
     */
    Long generateSalesOrder(Long platformOrderId, JSONObject params);

    /**
     * 批量生成销售订单
     *
     * @param ids    平台订单ID列表
     * @param params 参数
     * @return 销售订单ID
     */
    Long generateBatchSalesOrder(List<Long> ids, JSONObject params);
    
    /**
     * 批量导入平台订单
     * 
     * @param orders 订单数据列表
     * @return 导入结果
     */
    JSONObject batchImport(JSONObject orders);
    
    /**
     * 查询订单详情（包含商品明细）
     * 
     * @param id 订单ID
     * @return 订单详情
     */
    Object info(Long id);
}

