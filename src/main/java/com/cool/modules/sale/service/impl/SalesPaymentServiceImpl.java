package com.cool.modules.sale.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.base.ModifyEnum;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.sale.entity.SalesPaymentEntity;
import com.cool.modules.sale.mapper.SalesPaymentMapper;
import com.cool.modules.sale.service.SalesOrderService;
import com.cool.modules.sale.service.SalesPaymentService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.cool.modules.sale.entity.table.SalesPaymentEntityTableDef.SALES_PAYMENT_ENTITY;

/**
 * 回款信息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesPaymentServiceImpl extends BaseServiceImpl<SalesPaymentMapper, SalesPaymentEntity>
        implements SalesPaymentService {
    
    private final SalesOrderService orderService;
    
    @Override
    public List<SalesPaymentEntity> listByOrderId(Long orderId) {
        return list(QueryWrapper.create()
            .where(SALES_PAYMENT_ENTITY.ORDER_ID.eq(orderId))
            .orderBy(SALES_PAYMENT_ENTITY.PAYMENT_DATE, false));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWithReason(Long id, String reason) {
        CoolPreconditions.check(StrUtil.isBlank(reason), "删除原因不能为空");
        
        SalesPaymentEntity payment = getById(id);
        CoolPreconditions.check(payment == null, "回款信息不存在");
        
        payment.setDeleteReason(reason);
        updateById(payment);
        
        // 删除回款信息
        removeById(id);
        
        // 更新订单回款状态
        orderService.updatePaymentStatus(payment.getOrderId());
        
        log.info("删除回款信息成功，id: {}, reason: {}", id, reason);
    }
    
    @Override
    public void modifyAfter(JSONObject requestParams, SalesPaymentEntity entity, ModifyEnum type) {
        // 新增、修改、删除后都需要更新订单回款状态
        if (entity.getOrderId() != null) {
            orderService.updatePaymentStatus(entity.getOrderId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchPayment(List<Long> orderIds, Integer paymentMethod, String paymentDate, 
                            String paymentAccount, String transactionNo, String voucherUrl, String remark, Boolean confirm) {
        CoolPreconditions.check(orderIds == null || orderIds.isEmpty(), "请选择订单");
        CoolPreconditions.check(paymentMethod == null, "请选择回款方式");
        CoolPreconditions.check(StrUtil.isBlank(paymentDate), "请选择回款日期");

        for (Long orderId : orderIds) {
            // 获取订单信息
            com.cool.modules.sale.entity.SalesOrderEntity order = orderService.getById(orderId);
            if (order == null) {
                continue;
            }

            // 计算待回款金额
            java.math.BigDecimal totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal paidAmount = order.getPaidAmount() != null ? order.getPaidAmount() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal unpaidAmount = totalAmount.subtract(paidAmount);

            // 如果待回款金额大于0，则创建回款记录
            if (unpaidAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
                SalesPaymentEntity payment = new SalesPaymentEntity();
                payment.setOrderId(orderId);
                payment.setPaymentMethod(paymentMethod);
                payment.setPaymentDate(java.time.LocalDate.parse(paymentDate, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                payment.setAmount(unpaidAmount);
                payment.setPaymentAccount(paymentAccount);  // 新增：设置回款账户
                payment.setTransactionNo(transactionNo);    // 新增：设置交易流水号
                payment.setVoucherUrl(voucherUrl);          // 新增：设置回款凭证
                payment.setRemark(remark);
                
                if (Boolean.TRUE.equals(confirm)) {
                    payment.setStatus(1); // 已确认
                    payment.setConfirmTime(java.time.LocalDate.now());
                } else {
                    payment.setStatus(0); // 待确认
                }
                
                // 生成回款单号 (可以使用 UUID 或者类似规则)
                payment.setPaymentNo("PAY" + cn.hutool.core.util.IdUtil.simpleUUID().toUpperCase());

                save(payment);
                
                // 更新订单状态
                orderService.updatePaymentStatus(orderId);
            }
        }
    }
}

