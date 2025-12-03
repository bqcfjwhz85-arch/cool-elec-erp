package com.cool.modules.sale.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.sale.entity.SalesDeliveryDetailEntity;
import com.cool.modules.sale.entity.SalesDeliveryEntity;
import com.cool.modules.sale.mapper.SalesDeliveryMapper;
import com.cool.modules.sale.service.SalesDeliveryDetailService;
import com.cool.modules.sale.service.SalesDeliveryService;
import com.cool.modules.sale.service.SalesOrderService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.cool.modules.sale.entity.table.SalesDeliveryEntityTableDef.SALES_DELIVERY_ENTITY;

/**
 * 发货信息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesDeliveryServiceImpl extends BaseServiceImpl<SalesDeliveryMapper, SalesDeliveryEntity>
        implements SalesDeliveryService {
    
    private final SalesOrderService orderService;
    private final SalesDeliveryDetailService deliveryDetailService;
    
    @Override
    public List<SalesDeliveryEntity> listByOrderId(Long orderId) {
        return list(QueryWrapper.create()
            .where(SALES_DELIVERY_ENTITY.ORDER_ID.eq(orderId))
            .orderBy(SALES_DELIVERY_ENTITY.DELIVERY_TIME, false));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object addWithDetails(SalesDeliveryEntity entity, Object details) {
        log.info("=== addWithDetails called ===");
        log.info("entity: {}", entity);
        log.info("details: {}", details);
        
        // 自动生成发货单号
        if (StrUtil.isBlank(entity.getDeliveryNo())) {
            entity.setDeliveryNo(generateDeliveryNo());
        }
        
        // 保存发货主记录
        save(entity);

        log.info("发货主记录保存成功, id: {}", entity.getId());
        
        // 保存发货明细
        if (details != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> detailList = (List<Map<String, Object>>) details;
            log.info("detailList size: {}", detailList.size());
            if (!detailList.isEmpty()) {
                for (Map<String, Object> detail : detailList) {
                    SalesDeliveryDetailEntity detailEntity = new SalesDeliveryDetailEntity();
                    detailEntity.setDeliveryId(entity.getId());
                    detailEntity.setOrderItemId(Long.valueOf(detail.get("orderItemId").toString()));
                    detailEntity.setQuantity(Integer.valueOf(detail.get("quantity").toString()));
                    deliveryDetailService.save(detailEntity);
                    log.info("保存明细: deliveryId={}, orderItemId={}, quantity={}", 
                        entity.getId(), detail.get("orderItemId"), detail.get("quantity"));
                }
            }
        } else {
            log.warn("details is null!");
        }
        
        // 更新订单发货状态
        orderService.updateDeliveryStatus(entity.getOrderId());
        
        return entity.getId();
    }
    
    
    @Override
    public Map<String, Object> getInfoWithDetails(Long id) {
        log.info("=== getInfoWithDetails called, id: {} ===", id);
        
        // 获取发货主记录
        SalesDeliveryEntity delivery = getById(id);
        if (delivery == null) {
            log.warn("delivery not found for id: {}", id);
            return null;
        }
        log.info("delivery found: {}", delivery);
        
        // 查询发货明细
        List<SalesDeliveryDetailEntity> details = deliveryDetailService.listByDeliveryId(delivery.getId());
        log.info("查询到 {} 条明细记录", details.size());
        log.info("details: {}", details);
        
        // 将发货信息转换为Map并添加details字段
        Map<String, Object> result = JSONUtil.parseObj(delivery);
        result.put("details", details);
        log.info("返回结果 keys: {}", result.keySet());
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object updateWithDetails(SalesDeliveryEntity entity, Object details) {
        log.info("=== updateWithDetails called ===");
        log.info("entity: {}", entity);
        log.info("details: {}", details);
        
        // 更新发货主记录
        updateById(entity);
        log.info("发货主记录更新成功, id: {}", entity.getId());
        
        // 先删除旧的明细
        deliveryDetailService.deleteByDeliveryId(entity.getId());
        log.info("已删除旧的发货明细");
        
        // 保存新的发货明细
        if (details != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> detailList = (List<Map<String, Object>>) details;
            log.info("detailList size: {}", detailList.size());
            if (!detailList.isEmpty()) {
                for (Map<String, Object> detail : detailList) {
                    SalesDeliveryDetailEntity detailEntity = new SalesDeliveryDetailEntity();
                    detailEntity.setDeliveryId(entity.getId());
                    detailEntity.setOrderItemId(Long.valueOf(detail.get("orderItemId").toString()));
                    detailEntity.setQuantity(Integer.valueOf(detail.get("quantity").toString()));
                    deliveryDetailService.save(detailEntity);
                    log.info("保存明细: deliveryId={}, orderItemId={}, quantity={}", 
                        entity.getId(), detail.get("orderItemId"), detail.get("quantity"));
                }
            }
        } else {
            log.warn("details is null!");
        }
        
        // 更新订单发货状态
        orderService.updateDeliveryStatus(entity.getOrderId());
        
        return entity.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWithReason(Long id, String reason) {
        CoolPreconditions.check(StrUtil.isBlank(reason), "删除原因不能为空");
        
        SalesDeliveryEntity delivery = getById(id);
        CoolPreconditions.check(delivery == null, "发货信息不存在");
        
        delivery.setDeleteReason(reason);
        updateById(delivery);
        
        // 删除发货明细
        deliveryDetailService.deleteByDeliveryId(id);
        
        // 删除发货信息
        removeById(id);
        
        // 更新订单发货状态
        orderService.updateDeliveryStatus(delivery.getOrderId());
        
        log.info("删除发货信息成功，id: {}, reason: {}", id, reason);
    }

    /**
     * 生成发货单号
     * 格式：SH + yyyyMMdd + 4位流水号
     */
    private String generateDeliveryNo() {
        String prefix = "SH";
        String date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefixWithDate = prefix + date;
        
        // 查询当天最大的单号
        SalesDeliveryEntity maxEntity = getOne(query()
            .where(SALES_DELIVERY_ENTITY.DELIVERY_NO.like(prefixWithDate + "%"))
            .orderBy(SALES_DELIVERY_ENTITY.DELIVERY_NO, false)
            .limit(1));
            
        int sequence = 1;
        if (maxEntity != null && StrUtil.isNotBlank(maxEntity.getDeliveryNo())) {
            String maxNo = maxEntity.getDeliveryNo();
            // 截取最后4位
            if (maxNo.length() >= 4) {
                String seqStr = maxNo.substring(maxNo.length() - 4);
                if (StrUtil.isNumeric(seqStr)) {
                    sequence = Integer.parseInt(seqStr) + 1;
                }
            }
        }
        
        return String.format("%s%04d", prefixWithDate, sequence);
    }
}
