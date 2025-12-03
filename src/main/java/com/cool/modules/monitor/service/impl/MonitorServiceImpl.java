package com.cool.modules.monitor.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONObject;
import com.cool.modules.config.entity.ApprovalRecordEntity;
import com.cool.modules.config.service.ApprovalRecordService;
import com.cool.modules.monitor.service.MonitorService;
import com.cool.modules.purchase.entity.PurchaseOrderEntity;
import com.cool.modules.purchase.service.PurchaseOrderService;
import com.cool.modules.sale.entity.SalesOrderEntity;
import com.cool.modules.sale.service.SalesOrderService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.cool.modules.base.entity.sys.table.BaseSysLogEntityTableDef.BASE_SYS_LOG_ENTITY;
import static com.cool.modules.sale.entity.table.SalesOrderEntityTableDef.SALES_ORDER_ENTITY;
import static com.cool.modules.purchase.entity.table.PurchaseOrderEntityTableDef.PURCHASE_ORDER_ENTITY;
import static com.cool.modules.config.entity.table.ApprovalRecordEntityTableDef.APPROVAL_RECORD_ENTITY;
import static com.cool.modules.source.entity.table.SourceEntityTableDef.SOURCE_ENTITY;
import static com.cool.modules.sale.entity.table.SalesOrderItemEntityTableDef.SALES_ORDER_ITEM_ENTITY;
import com.cool.modules.source.service.SourceService;
import com.cool.modules.sale.service.SalesOrderItemService;
import com.cool.modules.base.service.sys.BaseSysLogService;
import com.cool.modules.source.entity.SourceEntity;
import com.cool.modules.sale.entity.SalesOrderItemEntity;
import com.cool.modules.base.entity.sys.BaseSysLogEntity;

@Service
@RequiredArgsConstructor
public class MonitorServiceImpl implements MonitorService {

    private final SalesOrderService salesOrderService;
    private final PurchaseOrderService purchaseOrderService;
    private final ApprovalRecordService approvalRecordService;
    private final SourceService sourceService;
    private final SalesOrderItemService salesOrderItemService;
    private final BaseSysLogService baseSysLogService;

    @Override
    public JSONObject getDashboardData() {
        JSONObject data = new JSONObject();

        // 1. Cards Data
        List<JSONObject> cards = new ArrayList<>();
        
        // Card 1: Pending Sales Orders (Approval Status = 0)
        long pendingSales = salesOrderService.count(QueryWrapper.create()
                .where(SALES_ORDER_ENTITY.APPROVAL_STATUS.eq(0)));
        cards.add(createCard("待审批销售订单", pendingSales, "笔", "warning", "待办", 0));

        // Card 2: Month Sales Total
        Date beginOfMonth = DateUtil.beginOfMonth(new Date());
        List<SalesOrderEntity> monthSales = salesOrderService.list(QueryWrapper.create()
                .where(SALES_ORDER_ENTITY.CREATE_TIME.ge(beginOfMonth)));
        BigDecimal monthSalesTotal = monthSales.stream()
                .map(SalesOrderEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cards.add(createCard("本月销售总额", monthSalesTotal, "元", "success", "月度", 0));

        // Card 3: Pending Purchase Orders
        long pendingPurchase = purchaseOrderService.count(QueryWrapper.create()
                .where(PURCHASE_ORDER_ENTITY.APPROVAL_STATUS.eq(0)));
        cards.add(createCard("待审批采购订单", pendingPurchase, "笔", "warning", "待办", 0));

        // Card 4: Month Purchase Total
        List<PurchaseOrderEntity> monthPurchase = purchaseOrderService.list(QueryWrapper.create()
                .where(PURCHASE_ORDER_ENTITY.CREATE_TIME.ge(beginOfMonth)));
        BigDecimal monthPurchaseTotal = monthPurchase.stream()
                .map(PurchaseOrderEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cards.add(createCard("本月采购总额", monthPurchaseTotal, "元", "danger", "月度", 0));

        data.put("cards", cards);

        // 2. Sales Order Status Distribution
        // Status: 0-Pending 1-Confirmed 2-Purchasing 3-Completed 4-Cancelled
        List<SalesOrderEntity> allSales = salesOrderService.list();
        Map<Integer, Long> salesStatusMap = allSales.stream()
                .collect(Collectors.groupingBy(SalesOrderEntity::getStatus, Collectors.counting()));
        
        List<JSONObject> saleSeries = new ArrayList<>();
        saleSeries.add(createPieData("待确认", salesStatusMap.getOrDefault(0, 0L)));
        saleSeries.add(createPieData("已确认", salesStatusMap.getOrDefault(1, 0L)));
        saleSeries.add(createPieData("采购中", salesStatusMap.getOrDefault(2, 0L)));
        saleSeries.add(createPieData("已完成", salesStatusMap.getOrDefault(3, 0L)));
        saleSeries.add(createPieData("已取消", salesStatusMap.getOrDefault(4, 0L)));
        
        JSONObject saleOption = new JSONObject();
        saleOption.put("series", Collections.singletonList(new JSONObject().put("data", saleSeries)));
        data.put("saleOption", saleOption);

        // 3. Purchase Order Status Distribution
        // Status: 0-Pending 1-Confirmed 2-Completed 3-Cancelled
        List<PurchaseOrderEntity> allPurchase = purchaseOrderService.list();
        Map<Integer, Long> purchaseStatusMap = allPurchase.stream()
                .collect(Collectors.groupingBy(PurchaseOrderEntity::getStatus, Collectors.counting()));

        List<JSONObject> purchaseSeries = new ArrayList<>();
        purchaseSeries.add(createPieData("待确认", purchaseStatusMap.getOrDefault(0, 0L)));
        purchaseSeries.add(createPieData("已确认", purchaseStatusMap.getOrDefault(1, 0L)));
        purchaseSeries.add(createPieData("已完成", purchaseStatusMap.getOrDefault(2, 0L)));
        purchaseSeries.add(createPieData("已取消", purchaseStatusMap.getOrDefault(3, 0L)));

        JSONObject purchaseOption = new JSONObject();
        purchaseOption.put("series", Collections.singletonList(new JSONObject().put("data", purchaseSeries)));
        data.put("purchaseOption", purchaseOption);

        // 4. Monthly Trend (Last 6 Months)
        JSONObject trendOption = new JSONObject();
        List<String> months = new ArrayList<>();
        List<BigDecimal> salesAmounts = new ArrayList<>();
        List<Long> salesCounts = new ArrayList<>();
        List<BigDecimal> purchaseAmounts = new ArrayList<>();
        List<Long> purchaseCounts = new ArrayList<>();
        
        // Query data for last 6 months
        Date sixMonthsAgo = DateUtil.offsetMonth(new Date(), -5);
        List<SalesOrderEntity> recentSales = salesOrderService.list(QueryWrapper.create()
                .where(SALES_ORDER_ENTITY.CREATE_TIME.ge(sixMonthsAgo)));
        List<PurchaseOrderEntity> recentPurchase = purchaseOrderService.list(QueryWrapper.create()
                .where(PURCHASE_ORDER_ENTITY.CREATE_TIME.ge(sixMonthsAgo)));
        
        for (int i = 5; i >= 0; i--) {
            Date monthStart = DateUtil.beginOfMonth(DateUtil.offsetMonth(new Date(), -i));
            Date monthEnd = DateUtil.endOfMonth(DateUtil.offsetMonth(new Date(), -i));
            months.add(DateUtil.format(monthStart, "MM月"));
            
            // Sales
            List<SalesOrderEntity> monthSalesData = recentSales.stream()
                    .filter(o -> DateUtil.isIn(o.getCreateTime(), monthStart, monthEnd))
                    .collect(Collectors.toList());
            BigDecimal salesAmount = monthSalesData.stream()
                    .map(SalesOrderEntity::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            salesAmounts.add(salesAmount);
            salesCounts.add((long) monthSalesData.size());
            
            // Purchase
            List<PurchaseOrderEntity> monthPurchaseData = recentPurchase.stream()
                    .filter(o -> DateUtil.isIn(o.getCreateTime(), monthStart, monthEnd))
                    .collect(Collectors.toList());
            BigDecimal purchaseAmount = monthPurchaseData.stream()
                    .map(PurchaseOrderEntity::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            purchaseAmounts.add(purchaseAmount);
            purchaseCounts.add((long) monthPurchaseData.size());
        }
        
        JSONObject xAxis = new JSONObject();
        xAxis.put("data", months);
        trendOption.put("xAxis", Collections.singletonList(xAxis));
        
        List<JSONObject> trendSeries = new ArrayList<>();
        trendSeries.add(new JSONObject().put("name", "销售金额").put("data", salesAmounts));
        trendSeries.add(new JSONObject().put("name", "销售订单数").put("data", salesCounts));
        trendSeries.add(new JSONObject().put("name", "采购金额").put("data", purchaseAmounts));
        trendSeries.add(new JSONObject().put("name", "采购订单数").put("data", purchaseCounts));
        trendOption.put("series", trendSeries);
        
        data.put("trendOption", trendOption);

        // 5. Category Statistics (Sales by Brand)
        List<SalesOrderItemEntity> allItems = salesOrderItemService.list();
        
        // Group by brand and sum quantity
        Map<String, Integer> brandSales = new HashMap<>();
        for (SalesOrderItemEntity item : allItems) {
            if (item.getBrand() != null && item.getQuantity() != null) {
                brandSales.merge(item.getBrand(), item.getQuantity(), Integer::sum);
            }
        }
        
        // Convert to pie chart data
        List<JSONObject> categorySeries = brandSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10) // Top 10 brands
                .map(e -> createPieData(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        
        JSONObject categoryOption = new JSONObject();
        categoryOption.put("series", Collections.singletonList(new JSONObject().put("data", categorySeries)));
        data.put("categoryOption", categoryOption);

        return data;
    }

    @Override
    public JSONObject getFinanceData() {
        JSONObject data = new JSONObject();
        
        // 1. Cards
        List<JSONObject> cards = new ArrayList<>();
        
        // Total Sales (Completed)
        List<SalesOrderEntity> completedSales = salesOrderService.list(QueryWrapper.create()
                .where(SALES_ORDER_ENTITY.STATUS.eq(3))); // 3-Completed
        BigDecimal totalSales = completedSales.stream()
                .map(SalesOrderEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cards.add(createFinanceCard("累计销售总额", totalSales, "#67c23a", "所有已完成销售订单"));

        // Total Purchase (Completed)
        List<PurchaseOrderEntity> completedPurchase = purchaseOrderService.list(QueryWrapper.create()
                .where(PURCHASE_ORDER_ENTITY.STATUS.eq(2))); // 2-Completed
        BigDecimal totalPurchase = completedPurchase.stream()
                .map(PurchaseOrderEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cards.add(createFinanceCard("累计采购总额", totalPurchase, "#f56c6c", "所有已完成采购订单"));

        // Pending Payment (Sales: Total - Paid)
        // Logic: Sales orders that are not fully paid
        List<SalesOrderEntity> unpaidSales = salesOrderService.list(QueryWrapper.create()
                .where(SALES_ORDER_ENTITY.PAYMENT_STATUS.ne(2))); // 2-Paid
        BigDecimal pendingCollection = unpaidSales.stream()
                .map(o -> o.getTotalAmount().subtract(o.getPaidAmount() != null ? o.getPaidAmount() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cards.add(createFinanceCard("待回款金额", pendingCollection, "#e6a23c", "已发货未全额回款"));

        // Pending Payment (Purchase: Total - Paid)
        List<PurchaseOrderEntity> unpaidPurchase = purchaseOrderService.list(QueryWrapper.create()
                .where(PURCHASE_ORDER_ENTITY.PAYMENT_STATUS.ne(2)));
        BigDecimal pendingPayment = unpaidPurchase.stream()
                .map(o -> o.getTotalAmount().subtract(o.getPaidAmount() != null ? o.getPaidAmount() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cards.add(createFinanceCard("待付款金额", pendingPayment, "#409eff", "已收货未全额付款"));

        data.put("cards", cards);

        // 2. Trend (Last 6 months)
        // Simplified: Just mock or simple aggregation. Let's do simple aggregation.
        // Group by month.
        // This is expensive to do in memory if data is large, but for now it's fine.
        // Better approach: SQL Group By. But sticking to Service API.
        
        // ... (Skipping complex trend logic for brevity, using placeholder or simple logic)
        // Let's just return empty trend for now or simple mock if too complex to implement efficiently here.
        // Actually, let's implement a simple one.
        
        JSONObject trendOption = new JSONObject();
        // X Axis
        List<String> months = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            months.add(DateUtil.format(DateUtil.offsetMonth(new Date(), -i), "MM月"));
        }
        
        // Data
        // We need to query by month. 
        // To avoid N queries, we fetch all data for last 6 months and group in memory.
        Date sixMonthsAgo = DateUtil.offsetMonth(new Date(), -5);
        
        List<SalesOrderEntity> recentSales = salesOrderService.list(QueryWrapper.create()
                .where(SALES_ORDER_ENTITY.CREATE_TIME.ge(sixMonthsAgo)));
        List<PurchaseOrderEntity> recentPurchase = purchaseOrderService.list(QueryWrapper.create()
                .where(PURCHASE_ORDER_ENTITY.CREATE_TIME.ge(sixMonthsAgo)));

        List<BigDecimal> salesData = new ArrayList<>();
        List<BigDecimal> purchaseData = new ArrayList<>();
        List<BigDecimal> profitData = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            Date monthStart = DateUtil.beginOfMonth(DateUtil.offsetMonth(new Date(), -i));
            Date monthEnd = DateUtil.endOfMonth(DateUtil.offsetMonth(new Date(), -i));
            
            BigDecimal s = recentSales.stream()
                    .filter(o -> DateUtil.isIn(o.getCreateTime(), monthStart, monthEnd))
                    .map(SalesOrderEntity::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal p = recentPurchase.stream()
                    .filter(o -> DateUtil.isIn(o.getCreateTime(), monthStart, monthEnd))
                    .map(PurchaseOrderEntity::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            salesData.add(s);
            purchaseData.add(p);
            profitData.add(s.subtract(p));
        }

        JSONObject xAxis = new JSONObject();
        xAxis.put("data", months);
        trendOption.put("xAxis", Collections.singletonList(xAxis));
        
        List<JSONObject> series = new ArrayList<>();
        series.add(new JSONObject().put("data", salesData));
        series.add(new JSONObject().put("data", purchaseData));
        series.add(new JSONObject().put("data", profitData));
        trendOption.put("series", series);
        
        data.put("trendOption", trendOption);

        // 3. Pending Option (Pie)
        List<JSONObject> pendingSeries = new ArrayList<>();
        pendingSeries.add(createPieData("待回款", pendingCollection));
        pendingSeries.add(createPieData("待付款", pendingPayment));
        
        JSONObject pendingOption = new JSONObject();
        pendingOption.put("series", Collections.singletonList(new JSONObject().put("data", pendingSeries)));
        data.put("pendingOption", pendingOption);
        
        // 4. Profit Analysis
        // Gross Profit = Total Sales - Total Purchase
        BigDecimal grossProfit = totalSales.subtract(totalPurchase);
        cards.add(createFinanceCard("累计毛利", grossProfit, "#67c23a", "销售总额 - 采购总额"));
        
        // Profit Margin = Gross Profit / Total Sales
        BigDecimal profitMargin = BigDecimal.ZERO;
        if (totalSales.compareTo(BigDecimal.ZERO) > 0) {
            profitMargin = grossProfit.divide(totalSales, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        }
        cards.add(createFinanceCard("毛利率", profitMargin, "#409eff", "毛利 / 销售总额", "%"));
        
        // 5. Revenue Composition (Top 5 Customers)
        List<SalesOrderEntity> allSales = salesOrderService.list(QueryWrapper.create()
                .where(SALES_ORDER_ENTITY.STATUS.eq(3))); // Completed
        
        Map<String, BigDecimal> customerSales = allSales.stream()
                .filter(o -> o.getCustomerName() != null)
                .collect(Collectors.groupingBy(SalesOrderEntity::getCustomerName, 
                        Collectors.reducing(BigDecimal.ZERO, SalesOrderEntity::getTotalAmount, BigDecimal::add)));
        
        List<JSONObject> revenueSeries = customerSales.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(e -> createPieData(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
                
        JSONObject revenueOption = new JSONObject();
        revenueOption.put("series", Collections.singletonList(new JSONObject().put("data", revenueSeries)));
        data.put("revenueOption", revenueOption);
        
        // 6. Cost Composition (Top 5 Suppliers)
        List<PurchaseOrderEntity> allPurchase = purchaseOrderService.list(QueryWrapper.create()
                .where(PURCHASE_ORDER_ENTITY.STATUS.eq(2))); // Completed
                
        Map<String, BigDecimal> supplierPurchase = allPurchase.stream()
                .filter(o -> o.getSupplierName() != null)
                .collect(Collectors.groupingBy(PurchaseOrderEntity::getSupplierName, 
                        Collectors.reducing(BigDecimal.ZERO, PurchaseOrderEntity::getTotalAmount, BigDecimal::add)));
                        
        List<JSONObject> costSeries = supplierPurchase.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(e -> createPieData(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
                
        JSONObject costOption = new JSONObject();
        costOption.put("series", Collections.singletonList(new JSONObject().put("data", costSeries)));
        data.put("costOption", costOption);

        return data;
    }

    @Override
    public JSONObject getApprovalData() {
        JSONObject data = new JSONObject();
        
        // Count Option
        JSONObject countOption = new JSONObject();
        countOption.put("xAxis", Collections.singletonList(new JSONObject().put("data", new ArrayList<>())));
        countOption.put("series", Collections.singletonList(new JSONObject().put("data", new ArrayList<>())));
        data.put("countOption", countOption);

        // Time Option
        JSONObject timeOption = new JSONObject();
        timeOption.put("xAxis", Collections.singletonList(new JSONObject().put("data", new ArrayList<>())));
        timeOption.put("series", Collections.singletonList(new JSONObject().put("data", new ArrayList<>())));
        data.put("timeOption", timeOption);

        // Status Option
        JSONObject statusOption = new JSONObject();
        statusOption.put("series", Collections.singletonList(new JSONObject().put("data", new ArrayList<>())));
        data.put("statusOption", statusOption);
        
        return data;
    }

    @Override
    public JSONObject getFlowData() {
        JSONObject data = new JSONObject();
        
        // Node Option
        JSONObject nodeOption = new JSONObject();
        nodeOption.put("xAxis", Collections.singletonList(new JSONObject().put("data", new ArrayList<>())));
        nodeOption.put("series", Collections.singletonList(new JSONObject().put("data", new ArrayList<>())));
        data.put("nodeOption", nodeOption);
        
        // Logs
        data.put("logs", new ArrayList<>());
        
        return data;
    }

    @Override
    public JSONObject getSourceData() {
        JSONObject data = new JSONObject();
        
        // 1. Cards
        List<JSONObject> cards = new ArrayList<>();
        
        // Total Source Orders
        long total = sourceService.count();
        cards.add(createCard("累计报备", total, "笔", "primary", "总数", 0));
        
        // Pending
        long pending = sourceService.count(QueryWrapper.create()
                .where(SOURCE_ENTITY.STATUS.eq(0)));
        cards.add(createCard("待审核", pending, "笔", "warning", "待办", 0));
        
        // Passed
        long passed = sourceService.count(QueryWrapper.create()
                .where(SOURCE_ENTITY.STATUS.eq(1)));
        cards.add(createCard("已通过", passed, "笔", "success", "已办", 0));
        
        // Rejected
        long rejected = sourceService.count(QueryWrapper.create()
                .where(SOURCE_ENTITY.STATUS.eq(2)));
        cards.add(createCard("已驳回", rejected, "笔", "danger", "已办", 0));
        
        data.put("cards", cards);
        
        // 2. Status Distribution
        List<SourceEntity> all = sourceService.list();
        Map<Integer, Long> statusMap = all.stream()
                .collect(Collectors.groupingBy(SourceEntity::getStatus, Collectors.counting()));
        
        List<JSONObject> series = new ArrayList<>();
        series.add(createPieData("待审核", statusMap.getOrDefault(0, 0L)));
        series.add(createPieData("已通过", statusMap.getOrDefault(1, 0L)));
        series.add(createPieData("已驳回", statusMap.getOrDefault(2, 0L)));
        
        JSONObject statusOption = new JSONObject();
        statusOption.put("series", Collections.singletonList(new JSONObject().put("data", series)));
        data.put("statusOption", statusOption);
        
        // 3. Trend (Last 15 Days)
        JSONObject trendOption = new JSONObject();
        List<String> days = new ArrayList<>();
        List<Long> trendData = new ArrayList<>();
        
        for (int i = 14; i >= 0; i--) {
            Date dayStart = DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), -i));
            Date dayEnd = DateUtil.endOfDay(DateUtil.offsetDay(new Date(), -i));
            days.add(DateUtil.format(dayStart, "MM-dd"));
            
            long count = sourceService.count(QueryWrapper.create()
                    .where(SOURCE_ENTITY.CREATE_TIME.between(dayStart, dayEnd)));
            trendData.add(count);
        }
        
        trendOption.put("xAxis", Collections.singletonList(new JSONObject().put("data", days)));
        trendOption.put("series", Collections.singletonList(new JSONObject().put("data", trendData)));
        data.put("trendOption", trendOption);
        
        // 4. Top Service Providers
        // Group by Provider Name and Count
        Map<String, Long> providerMap = all.stream()
                .filter(e -> e.getProviderName() != null)
                .collect(Collectors.groupingBy(SourceEntity::getProviderName, Collectors.counting()));
        
        List<JSONObject> rankList = providerMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    JSONObject item = new JSONObject();
                    item.put("name", e.getKey());
                    item.put("value", e.getValue());
                    return item;
                })
                .collect(Collectors.toList());
        
        data.put("rankList", rankList);
        
        return data;
    }

    @Override
    public JSONObject getProductData() {
        JSONObject data = new JSONObject();
        
        // Top 10 Selling Products
        // Group by Product SKU/Name and Sum Quantity
        List<SalesOrderItemEntity> allItems = salesOrderItemService.list();
        
        // If no items found, return empty list immediately
        if (allItems == null || allItems.isEmpty()) {
            data.put("list", new ArrayList<>());
            return data;
        }
        
        Map<String, Integer> productSales = allItems.stream()
                .filter(item -> item.getProductName() != null && item.getQuantity() != null)
                .collect(Collectors.groupingBy(
                        item -> item.getProductName() + (item.getSpecification() != null ? " (" + item.getSpecification() + ")" : ""),
                        Collectors.summingInt(SalesOrderItemEntity::getQuantity)
                ));
        
        // Sort by quantity desc
        List<Map.Entry<String, Integer>> sorted = productSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
        
        List<JSONObject> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sorted) {
            JSONObject item = new JSONObject();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            list.add(item);
        }
        
        data.put("list", list);
        return data;
    }



    @Override
    public JSONObject getLogData() {
        JSONObject data = new JSONObject();
        
        // Recent 20 Logs
        List<BaseSysLogEntity> logs = baseSysLogService.list(QueryWrapper.create()
                .orderBy(BASE_SYS_LOG_ENTITY.CREATE_TIME.desc())
                .limit(20));
        
        data.put("list", logs);
        return data;
    }

    private JSONObject createCard(String label, Object value, String unit, String type, String tag, double trend) {
        JSONObject card = new JSONObject();
        card.put("label", label);
        card.put("value", value);
        card.put("unit", unit);
        card.put("type", type);
        card.put("tag", tag);
        card.put("trend", trend);
        return card;
    }
    
    private JSONObject createFinanceCard(String label, BigDecimal value, String color, String desc) {
        return createFinanceCard(label, value, color, desc, "");
    }

    private JSONObject createFinanceCard(String label, BigDecimal value, String color, String desc, String suffix) {
        JSONObject card = new JSONObject();
        card.put("label", label);
        if ("%".equals(suffix)) {
            card.put("value", NumberUtil.decimalFormat("#0.00", value) + "%");
        } else {
            card.put("value", "¥" + NumberUtil.decimalFormat(",###.00", value));
        }
        card.put("color", color);
        card.put("desc", desc);
        return card;
    }
    
    private JSONObject createPieData(String name, Object value) {
        JSONObject item = new JSONObject();
        item.put("name", name);
        item.put("value", value);
        return item;
    }
}
