package com.cool.modules.purchase.service;

import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseService;
import com.cool.modules.purchase.entity.ReconciliationEntity;

/**
 * 对账单服务接口
 */
public interface ReconciliationService extends BaseService<ReconciliationEntity> {
    
    /**
     * 根据对账单编号获取对账单
     * 
     * @param billNo 对账单编号
     * @return 对账单信息
     */
    ReconciliationEntity getByBillNo(String billNo);
    
    /**
     * 生成对账单编号
     * 
     * @return 对账单编号（BILL+年月+序号）
     */
    String generateBillNo();
    
    /**
     * 生成对账单
     * 
     * @param params 筛选参数（时间范围、供应商名称、订单状态等）
     * @return 对账单
     */
    ReconciliationEntity generateReconciliation(JSONObject params);
    
    /**
     * 导出对账单
     * 
     * @param reconciliationId 对账单ID
     * @param response 响应对象
     */
    void exportReconciliation(Long reconciliationId, jakarta.servlet.http.HttpServletResponse response);
}






