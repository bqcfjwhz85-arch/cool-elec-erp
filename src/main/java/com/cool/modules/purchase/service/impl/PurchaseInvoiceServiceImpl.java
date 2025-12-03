package com.cool.modules.purchase.service.impl;

import com.cool.core.base.BaseServiceImpl;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.purchase.entity.PurchaseInvoiceEntity;
import com.cool.modules.purchase.mapper.PurchaseInvoiceMapper;
import com.cool.modules.purchase.service.PurchaseInvoiceService;
import com.cool.modules.purchase.service.PurchaseOrderService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.cool.modules.purchase.entity.table.PurchaseInvoiceEntityTableDef.PURCHASE_INVOICE_ENTITY;

/**
 * 采购发票服务实现类
 */
@Slf4j
@Service
public class PurchaseInvoiceServiceImpl extends BaseServiceImpl<PurchaseInvoiceMapper, PurchaseInvoiceEntity>
        implements PurchaseInvoiceService {
    
    private final PurchaseOrderService purchaseOrderService;
    
    public PurchaseInvoiceServiceImpl(@Lazy PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }
    
    @Override
    public List<PurchaseInvoiceEntity> listByOrderId(Long orderId) {
        return list(QueryWrapper.create()
            .where(PURCHASE_INVOICE_ENTITY.ORDER_ID.eq(orderId))
            .orderBy(PURCHASE_INVOICE_ENTITY.INVOICE_DATE, false));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWithReason(Long id, String reason) {
        PurchaseInvoiceEntity invoice = getById(id);
        CoolPreconditions.check(invoice == null, "发票信息不存在");
        
        // 保存删除原因
        invoice.setDeleteReason(reason);
        updateById(invoice);
        
        // 删除发票
        removeById(id);
        
        // 更新订单开票状态
        purchaseOrderService.updateInvoiceStatus(invoice.getOrderId());
        
        log.info("删除采购发票成功，invoiceId: {}, reason: {}", id, reason);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(PurchaseInvoiceEntity entity) {
        boolean result = super.save(entity);
        if (result && entity.getOrderId() != null) {
            // 更新订单开票状态
            purchaseOrderService.updateInvoiceStatus(entity.getOrderId());
        }
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(PurchaseInvoiceEntity entity) {
        boolean result = super.updateById(entity);
        if (result && entity.getOrderId() != null) {
            // 更新订单开票状态
            purchaseOrderService.updateInvoiceStatus(entity.getOrderId());
        }
        return result;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        PurchaseInvoiceEntity invoice = getById(id);
        CoolPreconditions.check(invoice == null, "发票信息不存在");
        
        invoice.setStatus(status);
        updateById(invoice);
        
        // 更新订单开票状态
        purchaseOrderService.updateInvoiceStatus(invoice.getOrderId());
        
        log.info("变更采购发票状态成功，invoiceId: {}, status: {}", id, status);
    }
}






