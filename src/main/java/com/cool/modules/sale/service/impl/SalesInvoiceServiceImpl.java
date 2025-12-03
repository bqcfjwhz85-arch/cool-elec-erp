package com.cool.modules.sale.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.base.ModifyEnum;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.sale.entity.SalesInvoiceEntity;
import com.cool.modules.sale.mapper.SalesInvoiceMapper;
import com.cool.modules.sale.service.SalesInvoiceService;
import com.cool.modules.sale.service.SalesOrderService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.cool.modules.sale.entity.table.SalesInvoiceEntityTableDef.SALES_INVOICE_ENTITY;

/**
 * 销售发票信息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesInvoiceServiceImpl extends BaseServiceImpl<SalesInvoiceMapper, SalesInvoiceEntity>
        implements SalesInvoiceService {
    
    private final SalesOrderService orderService;
    
    @Override
    public List<SalesInvoiceEntity> listByOrderId(Long orderId) {
        return list(QueryWrapper.create()
            .where(SALES_INVOICE_ENTITY.ORDER_ID.eq(orderId))
            .orderBy(SALES_INVOICE_ENTITY.INVOICE_DATE, false));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWithReason(Long id, String reason) {
        CoolPreconditions.check(StrUtil.isBlank(reason), "删除原因不能为空");
        
        SalesInvoiceEntity invoice = getById(id);
        CoolPreconditions.check(invoice == null, "发票信息不存在");
        
        // TODO: 校验未关联付款
        
        invoice.setDeleteReason(reason);
        updateById(invoice);
        
        // 删除发票信息
        removeById(id);
        
        // 更新订单开票状态
        orderService.updateInvoiceStatus(invoice.getOrderId());
        
        log.info("删除发票信息成功，id: {}, reason: {}", id, reason);
    }

    @Override
    public void modifyAfter(JSONObject requestParams, SalesInvoiceEntity entity, ModifyEnum type) {
        // 新增、修改、删除后都需要更新订单开票状态
        if (entity.getOrderId() != null) {
            orderService.updateInvoiceStatus(entity.getOrderId());
        }
    }
}






















