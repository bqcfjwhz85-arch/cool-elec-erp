package com.cool.modules.monitor.service;

import cn.hutool.json.JSONObject;

public interface MonitorService {
    /**
     * Get dashboard data
     */
    JSONObject getDashboardData();

    /**
     * Get finance data
     */
    JSONObject getFinanceData();

    /**
     * Get approval data
     */
    JSONObject getApprovalData();

    /**
     * Get flow data
     */
    JSONObject getFlowData();

    /**
     * Get source data
     */
    JSONObject getSourceData();

    /**
     * Get product data
     */
    JSONObject getProductData();

    /**
     * Get log data
     */
    JSONObject getLogData();
}
