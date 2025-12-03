package com.cool.modules.purchase.service.impl;

import com.cool.core.base.BaseServiceImpl;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.purchase.entity.PurchasePaymentEntity;
import com.cool.modules.purchase.mapper.PurchasePaymentMapper;
import com.cool.modules.purchase.service.PurchaseOrderService;
import com.cool.modules.purchase.service.PurchasePaymentService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.cool.modules.purchase.entity.table.PurchasePaymentEntityTableDef.PURCHASE_PAYMENT_ENTITY;

/**
 * 采购付款服务实现类
 */
@Slf4j
@Service
public class PurchasePaymentServiceImpl extends BaseServiceImpl<PurchasePaymentMapper, PurchasePaymentEntity>
        implements PurchasePaymentService {

    private final PurchaseOrderService purchaseOrderService;

    public PurchasePaymentServiceImpl(@Lazy PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @Override
    public List<PurchasePaymentEntity> listByOrderId(Long orderId) {
        return list(QueryWrapper.create()
            .where(PURCHASE_PAYMENT_ENTITY.ORDER_ID.eq(orderId))
            .orderBy(PURCHASE_PAYMENT_ENTITY.PAYMENT_DATE, false));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWithReason(Long id, String reason) {
        PurchasePaymentEntity payment = getById(id);
        CoolPreconditions.check(payment == null, "付款信息不存在");

        Long orderId = payment.getOrderId();

        // 保存删除原因后执行删除
        payment.setDeleteReason(reason);
        updateById(payment);

        // 执行删除
        removeById(id);

        // 更新订单付款状态
        if (orderId != null) {
            purchaseOrderService.updatePaymentStatus(orderId);
        }

        log.info("删除采购付款信息，paymentId: {}, reason: {}", id, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(PurchasePaymentEntity entity) {
        boolean result = super.save(entity);
        if (result && entity.getOrderId() != null) {
            // 更新订单付款状态
            purchaseOrderService.updatePaymentStatus(entity.getOrderId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(PurchasePaymentEntity entity) {
        boolean result = super.updateById(entity);
        if (result && entity.getOrderId() != null) {
            // 更新订单付款状态
            purchaseOrderService.updatePaymentStatus(entity.getOrderId());
        }
        return result;
    }
}
