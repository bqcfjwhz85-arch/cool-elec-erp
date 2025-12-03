package com.cool.modules.purchase.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.purchase.entity.PurchaseOrderEntity;
import com.cool.modules.purchase.entity.ReconciliationEntity;
import com.cool.modules.purchase.entity.ReconciliationItemEntity;
import com.cool.modules.purchase.mapper.ReconciliationMapper;
import com.cool.modules.purchase.service.PurchaseOrderService;
import com.cool.modules.purchase.service.ReconciliationItemService;
import com.cool.modules.purchase.service.ReconciliationService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.cool.modules.purchase.entity.table.PurchaseOrderEntityTableDef.PURCHASE_ORDER_ENTITY;
import static com.cool.modules.purchase.entity.table.ReconciliationEntityTableDef.RECONCILIATION_ENTITY;

/**
 * 对账单服务实现类
 */
@Slf4j
@Service
public class ReconciliationServiceImpl extends BaseServiceImpl<ReconciliationMapper, ReconciliationEntity>
        implements ReconciliationService {
    
    private final PurchaseOrderService purchaseOrderService;
    private final ReconciliationItemService reconciliationItemService;
    
    public ReconciliationServiceImpl(
            PurchaseOrderService purchaseOrderService,
            ReconciliationItemService reconciliationItemService) {
        this.purchaseOrderService = purchaseOrderService;
        this.reconciliationItemService = reconciliationItemService;
    }
    
    @Override
    public ReconciliationEntity getByBillNo(String billNo) {
        return getOne(QueryWrapper.create()
            .where(RECONCILIATION_ENTITY.BILL_NO.eq(billNo)));
    }
    
    @Override
    public String generateBillNo() {
        String prefix = "BILL" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        
        // 查询当月最大序号
        QueryWrapper qw = QueryWrapper.create()
            .select(RECONCILIATION_ENTITY.BILL_NO)
            .where(RECONCILIATION_ENTITY.BILL_NO.like(prefix + "%"))
            .orderBy(RECONCILIATION_ENTITY.BILL_NO, false)
            .limit(1);
        
        ReconciliationEntity lastBill = getOne(qw);
        int sequence = 1;
        
        if (lastBill != null && StrUtil.isNotBlank(lastBill.getBillNo())) {
            String lastNo = lastBill.getBillNo();
            if (lastNo.length() >= 12) {
                sequence = Integer.parseInt(lastNo.substring(10)) + 1;
            }
        }
        
        return prefix + String.format("%02d", sequence);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReconciliationEntity generateReconciliation(JSONObject params) {
        log.info("生成对账单，params: {}", params);
        
        // 1. 获取筛选参数
        LocalDateTime startDate = params.get("startDate", LocalDateTime.class);
        LocalDateTime endDate = params.get("endDate", LocalDateTime.class);
        Long supplierId = params.getLong("supplierId");
        String supplierName = params.getStr("supplierName");
        Integer orderStatus = params.getInt("orderStatus"); // 已付款/已完成
        
        CoolPreconditions.check(startDate == null, "开始日期不能为空");
        CoolPreconditions.check(endDate == null, "结束日期不能为空");
        CoolPreconditions.check(supplierId == null, "请选择供应商");
        CoolPreconditions.check(StrUtil.isBlank(supplierName), "供应商名称不能为空");
        
        // 2. 查询符合条件的采购订单
        QueryWrapper qw = QueryWrapper.create()
            .where(PURCHASE_ORDER_ENTITY.SUPPLIER_ID.eq(supplierId))
            .and(PURCHASE_ORDER_ENTITY.PURCHASE_DATE.ge(startDate))
            .and(PURCHASE_ORDER_ENTITY.PURCHASE_DATE.le(endDate));
        
        if (orderStatus != null) {
            if (orderStatus == 1) {
                // 已付款
                qw.and(PURCHASE_ORDER_ENTITY.PAYMENT_STATUS.eq(2));
            } else if (orderStatus == 2) {
                // 已完成
                qw.and(PURCHASE_ORDER_ENTITY.STATUS.eq(2));
            }
        }
        
        List<PurchaseOrderEntity> orders = purchaseOrderService.list(qw);
        
        CoolPreconditions.check(orders == null || orders.isEmpty(), "没有符合条件的采购订单");
        
        // 3. 创建对账单
        ReconciliationEntity reconciliation = new ReconciliationEntity();
        reconciliation.setBillNo(generateBillNo());
        reconciliation.setSupplierId(supplierId);
        reconciliation.setSupplierName(supplierName);
        reconciliation.setStartDate(startDate);
        reconciliation.setEndDate(endDate);
        reconciliation.setStatus(0); // 待对账
        
        // 4. 统计订单数据
        int orderCount = orders.size();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal paidAmount = BigDecimal.ZERO;
        
        List<ReconciliationItemEntity> items = new ArrayList<>();
        
        for (PurchaseOrderEntity order : orders) {
            totalAmount = totalAmount.add(order.getTotalAmount());
            paidAmount = paidAmount.add(order.getPaidAmount());
            
            // 创建对账单明细
            ReconciliationItemEntity item = new ReconciliationItemEntity();
            item.setOrderId(order.getId());
            item.setOrderNo(order.getOrderNo());
            item.setPurchaseDate(order.getPurchaseDate());
            item.setOrderAmount(order.getTotalAmount());
            item.setPaidAmount(order.getPaidAmount());
            item.setUnpaidAmount(order.getTotalAmount().subtract(order.getPaidAmount()));
            
            // 订单状态文本
            String orderStatusText = "待确认";
            if (order.getStatus() == 1) orderStatusText = "已确认";
            else if (order.getStatus() == 2) orderStatusText = "已完成";
            else if (order.getStatus() == 3) orderStatusText = "已取消";
            item.setOrderStatus(orderStatusText);
            
            items.add(item);
        }
        
        BigDecimal unpaidAmount = totalAmount.subtract(paidAmount);
        
        reconciliation.setOrderCount(orderCount);
        reconciliation.setTotalAmount(totalAmount);
        reconciliation.setPaidAmount(paidAmount);
        reconciliation.setUnpaidAmount(unpaidAmount);
        
        // 5. 保存对账单
        save(reconciliation);
        
        // 6. 保存对账单明细
        for (ReconciliationItemEntity item : items) {
            item.setReconciliationId(reconciliation.getId());
        }
        reconciliationItemService.saveBatch(items);
        
        log.info("对账单生成成功，reconciliationId: {}, billNo: {}", 
            reconciliation.getId(), reconciliation.getBillNo());
        
        return reconciliation;
    }

    @Override
    public void exportReconciliation(Long reconciliationId, jakarta.servlet.http.HttpServletResponse response) {
        log.info("导出对账单，reconciliationId: {}", reconciliationId);
        
        CoolPreconditions.check(reconciliationId == null, "对账单ID不能为空");
        
        // 1. 获取对账单信息
        ReconciliationEntity reconciliation = getById(reconciliationId);
        CoolPreconditions.check(reconciliation == null, "对账单不存在");
        
        // 2. 获取对账单明细
        QueryWrapper qw = QueryWrapper.create()
            .where(com.cool.modules.purchase.entity.table.ReconciliationItemEntityTableDef.RECONCILIATION_ITEM_ENTITY.RECONCILIATION_ID.eq(reconciliationId))
            .orderBy(com.cool.modules.purchase.entity.table.ReconciliationItemEntityTableDef.RECONCILIATION_ITEM_ENTITY.PURCHASE_DATE, true);
        List<ReconciliationItemEntity> items = reconciliationItemService.list(qw);
        
        try {
            // 3. 使用Hutool创建Excel
            cn.hutool.poi.excel.ExcelWriter writer = cn.hutool.poi.excel.ExcelUtil.getWriter(true);
            
            // 写入对账单基本信息
            writer.writeCellValue(0, 0, "对账单编号:");
            writer.writeCellValue(1, 0, reconciliation.getBillNo());
            writer.writeCellValue(0, 1, "供应商名称:");
            writer.writeCellValue(1, 1, reconciliation.getSupplierName());
            
            writer.writeCellValue(3, 0, "开始日期:");
            writer.writeCellValue(4, 0, reconciliation.getStartDate() != null ? 
                reconciliation.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
            writer.writeCellValue(3, 1, "结束日期:");
            writer.writeCellValue(4, 1, reconciliation.getEndDate() != null ? 
                reconciliation.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
            
            writer.writeCellValue(6, 0, "订单数量:");
            writer.writeCellValue(7, 0, reconciliation.getOrderCount() + " 笔");
            writer.writeCellValue(6, 1, "对账状态:");
            String statusText = reconciliation.getStatus() == 0 ? "待对账" : 
                               (reconciliation.getStatus() == 1 ? "已对账" : "有差异");
            writer.writeCellValue(7, 1, statusText);
            
            writer.writeCellValue(0, 3, "总金额:");
            writer.writeCellValue(1, 3, "¥" + reconciliation.getTotalAmount());
            writer.writeCellValue(3, 3, "已付款金额:");
            writer.writeCellValue(4, 3, "¥" + reconciliation.getPaidAmount());
            writer.writeCellValue(6, 3, "未付款金额:");
            writer.writeCellValue(7, 3, "¥" + reconciliation.getUnpaidAmount());
            
            // 空行
            int currentRow = 5;
            
            // 4. 写入订单明细表头
            writer.writeCellValue(0, currentRow, "采购单号");
            writer.writeCellValue(1, currentRow, "采购日期");
            writer.writeCellValue(2, currentRow, "订单金额");
            writer.writeCellValue(3, currentRow, "已付款金额");
            writer.writeCellValue(4, currentRow, "未付款金额");
            writer.writeCellValue(5, currentRow, "订单状态");
            writer.writeCellValue(6, currentRow, "备注");
            
            // 5. 写入明细数据
            currentRow++;
            for (ReconciliationItemEntity item : items) {
                writer.writeCellValue(0, currentRow, item.getOrderNo());
                writer.writeCellValue(1, currentRow, item.getPurchaseDate() != null ? 
                    item.getPurchaseDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                writer.writeCellValue(2, currentRow, "¥" + item.getOrderAmount());
                writer.writeCellValue(3, currentRow, "¥" + item.getPaidAmount());
                writer.writeCellValue(4, currentRow, "¥" + item.getUnpaidAmount());
                writer.writeCellValue(5, currentRow, item.getOrderStatus());
                writer.writeCellValue(6, currentRow, item.getRemark() != null ? item.getRemark() : "");
                currentRow++;
            }
            
            // 6. 输出到响应流
            writer.flush(response.getOutputStream(), true);
            writer.close();
            
            log.info("对账单导出成功，reconciliationId: {}, 明细数量: {}", reconciliationId, items.size());
            
        } catch (Exception e) {
            log.error("导出对账单失败", e);
            throw new RuntimeException("导出对账单失败: " + e.getMessage());
        }
    }
}






