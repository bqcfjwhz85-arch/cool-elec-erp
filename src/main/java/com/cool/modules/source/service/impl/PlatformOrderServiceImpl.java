package com.cool.modules.source.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.base.ModifyEnum;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.config.entity.PlatformEntity;
import com.cool.modules.config.service.PlatformService;
import com.cool.modules.config.service.PointConfigService;
import com.cool.modules.config.entity.ProviderEntity;
import com.cool.modules.config.service.ProviderService;
import com.cool.modules.customer.entity.CustomerEntity;
import com.cool.modules.customer.service.CustomerService;
import com.cool.modules.sale.entity.SalesOrderEntity;
import com.cool.modules.sale.service.SalesOrderService;
import com.cool.modules.source.entity.PlatformOrderEntity;
import com.cool.modules.source.entity.PlatformOrderItemEntity;
import com.cool.modules.source.mapper.PlatformOrderMapper;
import com.cool.modules.source.service.PlatformOrderItemService;
import com.cool.modules.source.service.PlatformOrderService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.cool.modules.source.entity.table.PlatformOrderEntityTableDef.PLATFORM_ORDER_ENTITY;
import static com.cool.modules.source.entity.table.PlatformOrderItemEntityTableDef.PLATFORM_ORDER_ITEM_ENTITY;

/**
 * 平台订单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformOrderServiceImpl extends BaseServiceImpl<PlatformOrderMapper, PlatformOrderEntity> 
        implements PlatformOrderService {
    
    private final CustomerService customerService;
    private final PlatformService platformService;
    private final ProviderService providerService;
    private final PointConfigService pointConfigService;
    private final PlatformOrderItemService platformOrderItemService;
    private final SalesOrderService salesOrderService;
    
    @Override
    public PlatformOrderEntity getByPlatformOrderNo(String platformOrderNo) {
        return getOne(QueryWrapper.create()
            .where(PLATFORM_ORDER_ENTITY.PLATFORM_ORDER_NO.eq(platformOrderNo)));
    }
    
    @Override
    public Object info(Long id) {
        // 查询主表数据
        PlatformOrderEntity entity = getById(id);
        if (entity != null) {
            // 查询关联的商品明细
            List<PlatformOrderItemEntity> items = platformOrderItemService.listByPlatformOrderId(id);
            entity.setItems(items);
        }
        return entity;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateSalesOrder(Long platformOrderId, JSONObject params) {
        log.info("开始从平台订单生成销售订单，platformOrderId: {}", platformOrderId);
        
        // 调用销售订单服务生成订单
        SalesOrderEntity salesOrder = salesOrderService.generateFromPlatformOrder(platformOrderId);
        
        CoolPreconditions.check(salesOrder == null, "销售订单生成失败");
        
        log.info("平台订单生成销售订单成功，platformOrderId: {}, salesOrderId: {}, salesOrderNo: {}", 
            platformOrderId, salesOrder.getId(), salesOrder.getOrderNo());
        
        return salesOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateBatchSalesOrder(List<Long> ids, JSONObject params) {
        log.info("开始批量从平台订单生成销售订单，ids: {}", ids);

        // 调用销售订单服务生成订单
        SalesOrderEntity salesOrder = salesOrderService.generateFromPlatformOrders(ids, params);

        CoolPreconditions.check(salesOrder == null, "销售订单生成失败");

        log.info("批量平台订单生成销售订单成功，ids: {}, salesOrderId: {}, salesOrderNo: {}",
            ids, salesOrder.getId(), salesOrder.getOrderNo());

        return salesOrder.getId();
    }


    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public JSONObject batchImport(JSONObject orders) {
        JSONObject result = new JSONObject();
        result.set("success", 0);
        result.set("failed", 0);
        result.set("errors", new java.util.ArrayList<String>());
        
        // TODO: 实现批量导入逻辑
        // 1. 解析Excel数据
        // 2. 校验数据（客户、平台、商品等）
        // 3. 批量插入订单和明细
        // 4. 返回导入结果
        
        return result;
    }
    
    @Override
    public void modifyBefore(JSONObject requestParams, PlatformOrderEntity entity, ModifyEnum type) {
        if (type == ModifyEnum.ADD) {
            // 生成平台订单编号：PO + 年月日 + 序号
            String dateStr = DateUtil.format(LocalDateTime.now(), "yyyyMMdd");
            String platformOrderNo = "PO" + dateStr + String.format("%04d", 
                count(QueryWrapper.create()
                    .where(PLATFORM_ORDER_ENTITY.PLATFORM_ORDER_NO.like("PO" + dateStr + "%"))) + 1);
            entity.setPlatformOrderNo(platformOrderNo);
            
            // 设置初始状态
            if (entity.getStatus() == null) {
                entity.setStatus(0); // 待生成订单
            }
            
            // 校验客户信息
            if (entity.getCustomerId() != null) {
                CustomerEntity customer = customerService.getById(entity.getCustomerId());
                CoolPreconditions.check(customer == null, "客户不存在");
                entity.setCustomerName(customer.getCustomerName());
            } else if (ObjUtil.isNotEmpty(entity.getCustomerName())) {
                CustomerEntity customer = customerService.getByName(entity.getCustomerName());
                CoolPreconditions.check(customer == null, "客户【" + entity.getCustomerName() + "】不存在");
                entity.setCustomerId(customer.getId());
            }
        }
        
        // 校验平台信息（ADD和UPDATE都需要）
        if (entity.getPlatformId() != null) {
            PlatformEntity platform = platformService.getById(entity.getPlatformId());
            CoolPreconditions.check(platform == null, "平台不存在");
            entity.setPlatformName(platform.getPlatformName());
        }
        
        // 校验服务商信息（ADD和UPDATE都需要）
        if (entity.getProviderId() != null) {
            ProviderEntity provider = providerService.getById(entity.getProviderId());
            CoolPreconditions.check(provider == null, "服务商不存在");
            entity.setProviderName(provider.getProviderName());
            // 服务商所属区域由前端传入，这里不覆盖
        }

        // 自动匹配点位（核心逻辑）
        // 1. 获取平台固定点位（作为默认点位）
        if (entity.getPlatformPoints() == null && entity.getPlatformId() != null) {
            BigDecimal defaultPoints = pointConfigService.getPoints(1, entity.getPlatformId(), null, 
                entity.getProviderRegion(), null, null);
            entity.setPlatformPoints(defaultPoints);
        }

        // 计算订单总金额（如果有商品明细）
        if (entity.getItems() != null && !entity.getItems().isEmpty()) {
            BigDecimal total = BigDecimal.ZERO;
            for (PlatformOrderItemEntity item : entity.getItems()) {
                // 2. 为每个商品匹配专属点位
                if (item.getProductPoints() == null) {
                    // 尝试获取商品专属点位
                    // 注意：这里需要根据商品SKU反查品牌和型号，或者由前端传入。
                    // 假设前端传入了品牌和型号名称，或者我们只根据平台和区域匹配
                    // 为了更精确，最好能根据SKU查询商品信息，但PlatformOrderItemEntity只有SKU
                    // 暂时使用平台+区域+品牌(如果有)+型号(如果有)去匹配
                    // 如果item中有brand字段，可以使用
                    BigDecimal itemPoints = pointConfigService.getPoints(1, entity.getPlatformId(), null,
                        entity.getProviderRegion(), item.getBrand(), null); // 暂时不匹配型号，因为item里没有modelId
                    
                    // 如果找到了专属点位（且不等于默认点位），则设置
                    if (itemPoints != null) {
                        item.setProductPoints(itemPoints);
                    }
                }

                // 确定计算用的点位：商品专属 > 订单默认
                BigDecimal calculationPoints = item.getProductPoints();
                if (calculationPoints == null) {
                    calculationPoints = entity.getPlatformPoints();
                }
                
                // 如果仍未获取到点位，则默认为100%（即原价）或者报错？
                // 需求文档未明确，建议给个默认值或者不计算折扣
                if (calculationPoints == null) {
                    calculationPoints = new BigDecimal("100"); 
                }

                // 重新计算小计：国网价 × 数量 × 点位%
                if (item.getStateGridPrice() != null && item.getQuantity() != null) {
                    BigDecimal subtotal = item.getStateGridPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
                        .multiply(calculationPoints.divide(BigDecimal.valueOf(100)));
                    item.setSubtotal(subtotal);
                }

                if (item.getSubtotal() != null) {
                    total = total.add(item.getSubtotal());
                }
            }
            entity.setTotalAmount(total);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyAfter(JSONObject requestParams, PlatformOrderEntity entity, ModifyEnum type) {
        if (type == ModifyEnum.ADD) {
            // 保存商品明细
            if (entity.getItems() != null && !entity.getItems().isEmpty()) {
                platformOrderItemService.batchSave(entity.getId(), entity.getItems());
                log.info("平台订单创建成功，编号：{}，明细数量：{}", entity.getPlatformOrderNo(), entity.getItems().size());
            } else {
                log.info("平台订单创建成功，编号：{}", entity.getPlatformOrderNo());
            }
        }
        
        // 更新后处理逻辑
        if (type == ModifyEnum.UPDATE) {
            // 删除旧的商品明细
            if (entity.getItems() != null) {
                // 先删除原有的明细
                platformOrderItemService.remove(QueryWrapper.create()
                    .where(PLATFORM_ORDER_ITEM_ENTITY.PLATFORM_ORDER_ID.eq(entity.getId())));
                
                // 重新保存新的明细
                if (!entity.getItems().isEmpty()) {
                    platformOrderItemService.batchSave(entity.getId(), entity.getItems());
                    log.info("平台订单更新成功，ID：{}，明细数量：{}", entity.getId(), entity.getItems().size());
                } else {
                    log.info("平台订单更新成功，ID：{}，已清空明细", entity.getId());
                }
            } else {
                log.info("平台订单更新成功，ID：{}", entity.getId());
            }
        }
    }
}

