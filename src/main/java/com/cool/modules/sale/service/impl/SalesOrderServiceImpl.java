package com.cool.modules.sale.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.base.ModifyEnum;
import com.cool.core.exception.CoolException;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.config.entity.ApprovalRecordEntity;
import com.cool.modules.config.service.PriceConfigService;
import com.cool.modules.sale.entity.SalesDeliveryEntity;
import com.cool.modules.sale.entity.SalesInvoiceEntity;
import com.cool.modules.sale.entity.SalesOrderEntity;
import com.cool.modules.sale.entity.SalesOrderItemEntity;
import com.cool.modules.sale.entity.SalesPaymentEntity;
import com.cool.modules.sale.mapper.SalesOrderMapper;
import com.cool.modules.sale.service.SalesDeliveryService;
import com.cool.modules.sale.service.SalesInvoiceService;
import com.cool.modules.sale.service.SalesOrderItemService;
import com.cool.modules.sale.service.SalesOrderService;
import com.cool.modules.sale.service.SalesPaymentService;
import com.cool.modules.source.entity.PlatformOrderEntity;
import com.cool.modules.source.entity.PlatformOrderItemEntity;
import com.cool.modules.source.entity.SourceEntity;
import com.cool.modules.source.entity.SourceItemEntity;
import com.cool.modules.source.service.PlatformOrderItemService;
import com.cool.modules.source.service.PlatformOrderService;
import com.cool.modules.source.service.SourceItemService;
import com.cool.modules.source.service.SourceService;
import com.cool.modules.config.service.ApprovalProcessService;
import com.cool.modules.config.service.PointConfigService;
import com.cool.core.util.CoolSecurityUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.cool.modules.sale.entity.table.SalesOrderEntityTableDef.SALES_ORDER_ENTITY;

/**
 * 销售订单服务实现类
 */
@Slf4j
@Service
public class SalesOrderServiceImpl extends BaseServiceImpl<SalesOrderMapper, SalesOrderEntity>
        implements SalesOrderService {
    
    private final PriceConfigService priceConfigService;
    private final SalesOrderItemService salesOrderItemService;
    private final SalesDeliveryService deliveryService;
    private final SalesInvoiceService invoiceService;
    private final SalesPaymentService paymentService;
    private final PlatformOrderService platformOrderService;
    private final PlatformOrderItemService platformOrderItemService;
    private final SourceService sourceService;
    private final SourceItemService sourceItemService;
    private final ApprovalProcessService approvalProcessService;
    
    private final PointConfigService pointConfigService;
    
    // 使用构造函数注入，并对循环依赖的服务使用@Lazy
    public SalesOrderServiceImpl(
            PriceConfigService priceConfigService,
            SalesOrderItemService salesOrderItemService,
            @Lazy SalesDeliveryService deliveryService,
            @Lazy SalesInvoiceService invoiceService,
            @Lazy SalesPaymentService paymentService,
            @Lazy PlatformOrderService platformOrderService,
            @Lazy PlatformOrderItemService platformOrderItemService,
            @Lazy SourceService sourceService,
            @Lazy SourceItemService sourceItemService,
            @Lazy ApprovalProcessService approvalProcessService,
            @Lazy PointConfigService pointConfigService) {
        this.priceConfigService = priceConfigService;
        this.salesOrderItemService = salesOrderItemService;
        this.deliveryService = deliveryService;
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
        this.platformOrderService = platformOrderService;
        this.platformOrderItemService = platformOrderItemService;
        this.sourceService = sourceService;
        this.sourceItemService = sourceItemService;
        this.approvalProcessService = approvalProcessService;
        this.pointConfigService = pointConfigService;
    }
    
    @Override
    public SalesOrderEntity getByOrderNo(String orderNo) {
        return getOne(QueryWrapper.create()
            .where(SALES_ORDER_ENTITY.ORDER_NO.eq(orderNo)));
    }
    
    @Override
    public String generateOrderNo() {
        String prefix = "SO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 查询当天最大序号
        QueryWrapper qw = QueryWrapper.create()
            .select(SALES_ORDER_ENTITY.ORDER_NO)
            .where(SALES_ORDER_ENTITY.ORDER_NO.like(prefix + "%"))
            .orderBy(SALES_ORDER_ENTITY.ORDER_NO, false)
            .limit(1);
        
        SalesOrderEntity lastOrder = getOne(qw);
        int sequence = 1;
        
        if (lastOrder != null && StrUtil.isNotBlank(lastOrder.getOrderNo())) {
            String lastNo = lastOrder.getOrderNo();
            if (lastNo.length() >= 14) {
                sequence = Integer.parseInt(lastNo.substring(10)) + 1;
            }
        }
        
        return prefix + String.format("%04d", sequence);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalesOrderEntity generateFromReport(Long reportId, JSONObject params) {
        log.info("从服务商报备生成销售订单，reportId: {}", reportId);
        
        // 1. 获取报备单信息
        SourceEntity source = sourceService.getById(reportId);
        CoolPreconditions.check(source == null, "服务商报备单不存在");
        CoolPreconditions.check(source.getSalesOrderId() != null, "报备单已生成销售订单");
        
        // 获取报备单明细
        List<SourceItemEntity> sourceItems = sourceItemService.listBySourceId(reportId);
        CoolPreconditions.check(sourceItems == null || sourceItems.isEmpty(), "报备单商品明细不能为空");
        
        // 2. 创建销售订单
        SalesOrderEntity salesOrder = new SalesOrderEntity();
        salesOrder.setOrderNo(generateOrderNo());
        salesOrder.setSourceType(1);  // 来源类型：1-服务商报备
        salesOrder.setSourceId(reportId);
        // 订单类型映射：orderMode 0-借码 -> orderType 1-借码，orderMode 1-实供 -> orderType 2-实供
        salesOrder.setOrderType(source.getOrderMode() == 0 ? 1 : 2);
        salesOrder.setIsRegional(source.getIsRegional());  // 是否区域订单
        salesOrder.setCustomerId(source.getCustomerId());
        salesOrder.setCustomerName(source.getCustomerName());
        salesOrder.setProviderId(source.getProviderId());
        salesOrder.setProviderName(source.getProviderName());
        salesOrder.setRegionCode(source.getRegionCode());
        salesOrder.setPlatformId(source.getPlatformId());
        salesOrder.setPlatformName(source.getPlatformName());
        salesOrder.setReportNo(source.getSourceNo());  // 保存报备单号
        salesOrder.setCreateMode(1);  // 创建方式：1-自动创建
        salesOrder.setStatus(1);  // 订单状态：1-已确认
        salesOrder.setApprovalStatus(1);  // 审批状态：1-审批通过（服务商报备自动通过）
        salesOrder.setDeliveryStatus(0);
        salesOrder.setInvoiceStatus(0);
        salesOrder.setPaymentStatus(0);
        salesOrder.setFlowStatus(0);
        salesOrder.setInvoiceType(1);  // 默认增值税专用发票
        salesOrder.setRemark(source.getRemark());
        
        // 3. 创建订单商品明细并计算金额
        List<SalesOrderItemEntity> salesItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        
        for (SourceItemEntity sourceItem : sourceItems) {
            SalesOrderItemEntity salesItem = new SalesOrderItemEntity();
            salesItem.setProductSku(sourceItem.getProductSku());
            salesItem.setProductName(sourceItem.getProductName());
            salesItem.setBrand(sourceItem.getBrand());
            salesItem.setSpecification(sourceItem.getSpecification());
            salesItem.setUnit(sourceItem.getUnit());
            salesItem.setQuantity(sourceItem.getQuantity());
            
            // 根据订单方式计算金额
            BigDecimal amount;
            if (source.getOrderMode() == 0) {
                // 借码订单：使用国网价 × 数量 × 点位
                // 从params中获取点位，如果没有则使用默认点位
                BigDecimal points = params != null ? params.getBigDecimal("points") : null;
                if (points == null) {
                    // 使用配置的服务商借码默认点位
                    points = pointConfigService.getPoints(2, null, source.getProviderId(), 
                        null, null, null);
                    
                    // 如果配置也为空，则给个兜底默认值
                    if (points == null) {
                        points = new BigDecimal("10"); // 兜底默认点位10%
                    }
                }
                salesItem.setPrice(sourceItem.getStateGridPrice());
                salesItem.setPoints(points);
                amount = sourceItem.getStateGridPrice()
                    .multiply(BigDecimal.valueOf(sourceItem.getQuantity()))
                    .multiply(points.divide(BigDecimal.valueOf(100)));
                
                // 设置订单的点位（借码订单必须保存点位信息）
                salesOrder.setPoints(points);
            } else {
                // 实供订单：直接使用报备单的小计金额
                amount = sourceItem.getSubtotal() != null ? sourceItem.getSubtotal() : 
                    sourceItem.getStateGridPrice().multiply(BigDecimal.valueOf(sourceItem.getQuantity()));
                salesItem.setPrice(sourceItem.getStateGridPrice());
                salesItem.setPoints(null);
            }
            
            salesItem.setAmount(amount);
            salesItem.setRemark(sourceItem.getRemark());
            
            salesItems.add(salesItem);
            totalAmount = totalAmount.add(amount);
            totalQuantity += sourceItem.getQuantity();
        }
        
        salesOrder.setTotalAmount(totalAmount);
        salesOrder.setTotalQuantity(totalQuantity);
        salesOrder.setItems(salesItems);
        
        // 4. 保存销售订单（主表）
        save(salesOrder);
        
        // 5. 保存订单明细
        if (!salesItems.isEmpty()) {
            salesOrderItemService.batchSave(salesOrder.getId(), salesItems);
        }
        
        // 6. 更新报备单状态
        source.setSalesOrderId(salesOrder.getId());
        source.setSalesOrderNo(salesOrder.getOrderNo());
        source.setStatus(3);  // 状态：3-已生成订单
        sourceService.updateById(source);
        
        log.info("服务商报备生成销售订单成功，reportId: {}, salesOrderId: {}, salesOrderNo: {}", 
            reportId, salesOrder.getId(), salesOrder.getOrderNo());
        
        return salesOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalesOrderEntity generateFromReports(List<Long> reportIds, JSONObject params) {
        log.info("批量从服务商报备生成销售订单，reportIds: {}", reportIds);
        CoolPreconditions.check(reportIds == null || reportIds.isEmpty(), "请选择报备单");

        // 1. 获取所有报备单信息
        List<SourceEntity> sources = sourceService.listByIds(reportIds);
        CoolPreconditions.check(sources == null || sources.size() != reportIds.size(), "部分报备单不存在");

        // 2. 校验一致性（服务商、客户、供货方式）
        SourceEntity firstSource = sources.get(0);
        Long providerId = firstSource.getProviderId();
        Long customerId = firstSource.getCustomerId();
        Integer orderMode = firstSource.getOrderMode();
        Integer isRegional = firstSource.getIsRegional();
        Long platformId = firstSource.getPlatformId();

        for (SourceEntity source : sources) {
            CoolPreconditions.check(source.getSalesOrderId() != null, 
                "报备单【" + source.getSourceNo() + "】已生成销售订单");
            CoolPreconditions.check(!ObjUtil.equals(source.getProviderId(), providerId),
                "所选报备单必须属于同一服务商");
            CoolPreconditions.check(!ObjUtil.equals(source.getCustomerId(), customerId), 
                "所选报备单必须属于同一客户");
            CoolPreconditions.check(!ObjUtil.equals(source.getOrderMode(), orderMode), 
                "所选报备单的供货方式必须一致");
            // 平台也最好一致，虽然业务上可能允许不同平台合并，但为了数据准确性建议一致，或者取第一个
            // 这里暂不强制平台一致，但如果平台不一致，订单头部的平台信息只能存一个
        }

        // 3. 创建销售订单
        SalesOrderEntity salesOrder = new SalesOrderEntity();
        salesOrder.setOrderNo(generateOrderNo());
        salesOrder.setSourceType(1);  // 来源类型：1-服务商报备
        // 批量生成时，SourceId存第一个，或者可以考虑存关联表，这里暂存第一个
        salesOrder.setSourceId(firstSource.getId());
        salesOrder.setOrderType(orderMode == 0 ? 1 : 2);
        salesOrder.setIsRegional(isRegional);
        salesOrder.setCustomerId(customerId);
        salesOrder.setCustomerName(firstSource.getCustomerName());
        salesOrder.setProviderId(providerId);
        salesOrder.setProviderName(firstSource.getProviderName());
        salesOrder.setRegionCode(firstSource.getRegionCode());
        salesOrder.setPlatformId(platformId);
        salesOrder.setPlatformName(firstSource.getPlatformName());
        // 报备单号拼接
        String reportNos = sources.stream().map(SourceEntity::getSourceNo).reduce((a, b) -> a + "," + b).orElse("");
        if (reportNos.length() > 500) reportNos = reportNos.substring(0, 497) + "...";
        salesOrder.setReportNo(reportNos);
        
        salesOrder.setCreateMode(1);  // 自动创建
        salesOrder.setStatus(1);  // 已确认
        salesOrder.setApprovalStatus(1);  // 审批通过
        salesOrder.setDeliveryStatus(0);
        salesOrder.setInvoiceStatus(0);
        salesOrder.setPaymentStatus(0);
        salesOrder.setFlowStatus(0);
        salesOrder.setInvoiceType(1);
        salesOrder.setRemark(params.getStr("remark")); // 使用参数中的备注

        // 4. 聚合所有报备单的商品明细
        List<SalesOrderItemEntity> salesItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;

        // 获取点位（如果是借码模式）
        BigDecimal points = null;
        if (orderMode == 0) {
            points = params != null ? params.getBigDecimal("points") : null;
            if (points == null) {
                points = pointConfigService.getPoints(2, null, providerId, null, null, null);
                if (points == null) points = new BigDecimal("10");
            }
            salesOrder.setPoints(points);
        }

        for (SourceEntity source : sources) {
            List<SourceItemEntity> sourceItems = sourceItemService.listBySourceId(source.getId());
            if (sourceItems == null) continue;

            for (SourceItemEntity sourceItem : sourceItems) {
                SalesOrderItemEntity salesItem = new SalesOrderItemEntity();
                salesItem.setProductSku(sourceItem.getProductSku());
                salesItem.setProductName(sourceItem.getProductName());
                salesItem.setBrand(sourceItem.getBrand());
                salesItem.setSpecification(sourceItem.getSpecification());
                salesItem.setUnit(sourceItem.getUnit());
                salesItem.setQuantity(sourceItem.getQuantity());
                
                BigDecimal amount;
                if (orderMode == 0) {
                    // 借码
                    salesItem.setPrice(sourceItem.getStateGridPrice());
                    salesItem.setPoints(points);
                    amount = sourceItem.getStateGridPrice()
                        .multiply(BigDecimal.valueOf(sourceItem.getQuantity()))
                        .multiply(points.divide(BigDecimal.valueOf(100)));
                } else {
                    // 实供
                    amount = sourceItem.getSubtotal() != null ? sourceItem.getSubtotal() : 
                        sourceItem.getStateGridPrice().multiply(BigDecimal.valueOf(sourceItem.getQuantity()));
                    salesItem.setPrice(sourceItem.getStateGridPrice());
                    salesItem.setPoints(null);
                }
                
                salesItem.setAmount(amount);
                // 备注带上原报备单号，方便追溯
                salesItem.setRemark((StrUtil.isNotBlank(sourceItem.getRemark()) ? sourceItem.getRemark() + " " : "") 
                    + "(" + source.getSourceNo() + ")");
                
                salesItems.add(salesItem);
                totalAmount = totalAmount.add(amount);
                totalQuantity += sourceItem.getQuantity();
            }
        }

        salesOrder.setTotalAmount(totalAmount);
        salesOrder.setTotalQuantity(totalQuantity);
        salesOrder.setItems(salesItems);

        // 5. 保存销售订单
        save(salesOrder);
        if (!salesItems.isEmpty()) {
            salesOrderItemService.batchSave(salesOrder.getId(), salesItems);
        }

        // 6. 更新所有报备单状态
        for (SourceEntity source : sources) {
            source.setSalesOrderId(salesOrder.getId());
            source.setSalesOrderNo(salesOrder.getOrderNo());
            source.setStatus(3);
            sourceService.updateById(source);
        }

        log.info("批量生成销售订单成功，reportIds: {}, salesOrderId: {}", reportIds, salesOrder.getId());
        return salesOrder;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalesOrderEntity generateFromPlatformOrder(Long platformOrderId) {
        log.info("从平台订单生成销售订单，platformOrderId: {}", platformOrderId);
        
        // 1. 获取平台订单信息（包含商品明细）
        PlatformOrderEntity platformOrder = platformOrderService.getById(platformOrderId);
        CoolPreconditions.check(platformOrder == null, "平台订单不存在");
        CoolPreconditions.check(platformOrder.getStatus() != 0, "平台订单已生成销售订单");
        
        // 获取平台订单明细
        List<PlatformOrderItemEntity> platformItems = platformOrderItemService.listByPlatformOrderId(platformOrderId);
        CoolPreconditions.check(platformItems == null || platformItems.isEmpty(), "平台订单商品明细不能为空");
        
        // 2. 创建销售订单（平台订单固定为实供方式 orderType=2）
        SalesOrderEntity salesOrder = new SalesOrderEntity();
        salesOrder.setOrderNo(generateOrderNo());
        salesOrder.setSourceType(2);  // 来源类型：2-平台订单
        salesOrder.setSourceId(platformOrderId);
        salesOrder.setOrderType(2);  // 订单类型：2-实供
        salesOrder.setIsRegional(0);  // 平台订单默认非区域
        salesOrder.setCustomerId(platformOrder.getCustomerId());
        salesOrder.setCustomerName(platformOrder.getCustomerName());
        salesOrder.setPlatformId(platformOrder.getPlatformId());
        salesOrder.setPlatformName(platformOrder.getPlatformName());
        salesOrder.setPlatformOrderNo(platformOrder.getPlatformOrderNo());
        salesOrder.setPoints(platformOrder.getPlatformPoints());  // 平台点位
        salesOrder.setCreateMode(1);  // 创建方式：1-自动创建
        salesOrder.setStatus(1);  // 订单状态：1-已确认
        salesOrder.setApprovalStatus(1);  // 审批状态：1-审批通过（平台订单自动通过）
        salesOrder.setDeliveryStatus(0);
        salesOrder.setInvoiceStatus(0);
        salesOrder.setPaymentStatus(0);
        salesOrder.setFlowStatus(0);
        salesOrder.setInvoiceType(1);  // 默认增值税专用发票
        
        // 收货信息
        salesOrder.setDeliveryAddress(platformOrder.getShippingAddress());
        salesOrder.setDeliveryContact(platformOrder.getConsignee());
        salesOrder.setDeliveryPhone(platformOrder.getContactPhone());
        salesOrder.setRemark(platformOrder.getRemark());
        
        // 3. 创建订单商品明细
        List<SalesOrderItemEntity> salesItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        
        for (PlatformOrderItemEntity platformItem : platformItems) {
            SalesOrderItemEntity salesItem = new SalesOrderItemEntity();
            salesItem.setProductSku(platformItem.getProductSku());
            salesItem.setProductName(platformItem.getProductName());
            salesItem.setBrand(platformItem.getBrand());
            salesItem.setSpecification(platformItem.getSpecification());
            salesItem.setUnit(platformItem.getUnit());
            salesItem.setQuantity(platformItem.getQuantity());
            
            // 价格使用国网价
            salesItem.setPrice(platformItem.getStateGridPrice());
            
            // 点位：优先使用商品专属点位，否则使用平台点位
            BigDecimal itemPoints = platformItem.getProductPoints();
            if (itemPoints == null || itemPoints.compareTo(BigDecimal.ZERO) == 0) {
                itemPoints = platformOrder.getPlatformPoints();
            }
            salesItem.setPoints(itemPoints);
            
            // 计算金额：国网价 × 数量 × 点位%
            BigDecimal amount = platformItem.getStateGridPrice()
                .multiply(BigDecimal.valueOf(platformItem.getQuantity()))
                .multiply(itemPoints.divide(BigDecimal.valueOf(100)));
            salesItem.setAmount(amount);
            salesItem.setRemark(platformItem.getRemark());
            
            salesItems.add(salesItem);
            totalAmount = totalAmount.add(amount);
            totalQuantity += platformItem.getQuantity();
        }
        
        salesOrder.setTotalAmount(totalAmount);
        salesOrder.setTotalQuantity(totalQuantity);
        salesOrder.setItems(salesItems);
        
        // 4. 保存销售订单（主表）
        save(salesOrder);
        
        // 5. 保存订单明细
        if (!salesItems.isEmpty()) {
            salesOrderItemService.batchSave(salesOrder.getId(), salesItems);
        }
        
        // 6. 更新平台订单状态
        platformOrder.setSalesOrderId(salesOrder.getId());
        platformOrder.setSalesOrderNo(salesOrder.getOrderNo());
        platformOrder.setStatus(1);  // 状态：1-已生成订单
        platformOrderService.updateById(platformOrder);
        
        log.info("平台订单生成销售订单成功，platformOrderId: {}, salesOrderId: {}, salesOrderNo: {}", 
            platformOrderId, salesOrder.getId(), salesOrder.getOrderNo());
        
        return salesOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalesOrderEntity generateFromPlatformOrders(List<Long> platformOrderIds, JSONObject params) {
        log.info("批量从平台订单生成销售订单，platformOrderIds: {}", platformOrderIds);
        CoolPreconditions.check(platformOrderIds == null || platformOrderIds.isEmpty(), "请选择平台订单");

        // 1. 获取所有平台订单信息
        List<PlatformOrderEntity> platformOrders = platformOrderService.listByIds(platformOrderIds);
        CoolPreconditions.check(platformOrders == null || platformOrders.size() != platformOrderIds.size(), "部分平台订单不存在");

        // 2. 校验一致性（客户、平台）
        PlatformOrderEntity firstOrder = platformOrders.get(0);
        Long customerId = firstOrder.getCustomerId();
        Long platformId = firstOrder.getPlatformId();

        for (PlatformOrderEntity order : platformOrders) {
            CoolPreconditions.check(order.getStatus() != 0, 
                "平台订单【" + order.getPlatformOrderNo() + "】已生成销售订单");
            CoolPreconditions.check(!ObjUtil.equals(order.getCustomerId(), customerId), 
                "所选平台订单必须属于同一客户");
            CoolPreconditions.check(!ObjUtil.equals(order.getPlatformId(), platformId), 
                "所选平台订单必须属于同一平台");
        }

        // 3. 创建销售订单
        SalesOrderEntity salesOrder = new SalesOrderEntity();
        salesOrder.setOrderNo(generateOrderNo());
        salesOrder.setSourceType(2);  // 来源类型：2-平台订单
        // 批量生成时，SourceId存第一个
        salesOrder.setSourceId(firstOrder.getId());
        salesOrder.setOrderType(2);  // 订单类型：2-实供
        salesOrder.setIsRegional(0);  // 平台订单默认非区域
        salesOrder.setCustomerId(customerId);
        salesOrder.setCustomerName(firstOrder.getCustomerName());
        salesOrder.setPlatformId(platformId);
        salesOrder.setPlatformName(firstOrder.getPlatformName());
        
        // 平台订单号拼接
        String platformOrderNos = platformOrders.stream().map(PlatformOrderEntity::getPlatformOrderNo).reduce((a, b) -> a + "," + b).orElse("");
        if (platformOrderNos.length() > 500) platformOrderNos = platformOrderNos.substring(0, 497) + "...";
        salesOrder.setPlatformOrderNo(platformOrderNos);
        
        salesOrder.setPoints(firstOrder.getPlatformPoints());  // 使用第一个订单的平台点位，或者应该重新计算？这里暂用第一个
        salesOrder.setCreateMode(1);  // 自动创建
        salesOrder.setStatus(1);  // 已确认
        salesOrder.setApprovalStatus(1);  // 审批通过
        salesOrder.setDeliveryStatus(0);
        salesOrder.setInvoiceStatus(0);
        salesOrder.setPaymentStatus(0);
        salesOrder.setFlowStatus(0);
        salesOrder.setInvoiceType(1);
        
        // 收货信息使用第一个订单的，或者由用户在前端确认（目前逻辑是自动生成，暂取第一个）
        salesOrder.setDeliveryAddress(firstOrder.getShippingAddress());
        salesOrder.setDeliveryContact(firstOrder.getConsignee());
        salesOrder.setDeliveryPhone(firstOrder.getContactPhone());
        salesOrder.setRemark(params.getStr("remark")); // 使用参数中的备注

        // 4. 聚合所有平台订单的商品明细
        List<SalesOrderItemEntity> salesItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (PlatformOrderEntity order : platformOrders) {
            List<PlatformOrderItemEntity> platformItems = platformOrderItemService.listByPlatformOrderId(order.getId());
            if (platformItems == null) continue;

            for (PlatformOrderItemEntity platformItem : platformItems) {
                SalesOrderItemEntity salesItem = new SalesOrderItemEntity();
                salesItem.setProductSku(platformItem.getProductSku());
                salesItem.setProductName(platformItem.getProductName());
                salesItem.setBrand(platformItem.getBrand());
                salesItem.setSpecification(platformItem.getSpecification());
                salesItem.setUnit(platformItem.getUnit());
                salesItem.setQuantity(platformItem.getQuantity());
                
                // 价格使用国网价
                salesItem.setPrice(platformItem.getStateGridPrice());
                
                // 点位
                BigDecimal itemPoints = platformItem.getProductPoints();
                if (itemPoints == null || itemPoints.compareTo(BigDecimal.ZERO) == 0) {
                    itemPoints = order.getPlatformPoints();
                }
                salesItem.setPoints(itemPoints);
                
                // 计算金额
                BigDecimal amount = platformItem.getStateGridPrice()
                    .multiply(BigDecimal.valueOf(platformItem.getQuantity()))
                    .multiply(itemPoints.divide(BigDecimal.valueOf(100)));
                salesItem.setAmount(amount);
                // 备注带上原平台订单号
                salesItem.setRemark((StrUtil.isNotBlank(platformItem.getRemark()) ? platformItem.getRemark() + " " : "") 
                    + "(" + order.getPlatformOrderNo() + ")");
                
                salesItems.add(salesItem);
                totalAmount = totalAmount.add(amount);
                totalQuantity += platformItem.getQuantity();
            }
        }

        salesOrder.setTotalAmount(totalAmount);
        salesOrder.setTotalQuantity(totalQuantity);
        salesOrder.setItems(salesItems);

        // 5. 保存销售订单
        save(salesOrder);
        if (!salesItems.isEmpty()) {
            salesOrderItemService.batchSave(salesOrder.getId(), salesItems);
        }

        // 6. 更新所有平台订单状态
        for (PlatformOrderEntity order : platformOrders) {
            order.setSalesOrderId(salesOrder.getId());
            order.setSalesOrderNo(salesOrder.getOrderNo());
            order.setStatus(1);
            platformOrderService.updateById(order);
        }

        log.info("批量生成销售订单成功，platformOrderIds: {}, salesOrderId: {}", platformOrderIds, salesOrder.getId());
        return salesOrder;
    }
    
    @Override
    public void calculateOrderAmount(SalesOrderEntity order) {
        CoolPreconditions.check(order == null, "订单信息不能为空");
        CoolPreconditions.check(order.getItems() == null || order.getItems().isEmpty(), 
            "订单商品明细不能为空");
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        
        for (SalesOrderItemEntity item : order.getItems()) {
            // 借码订单 (orderType=1) 或 平台订单 (sourceType=2) 使用点位计价
            // 注意：平台订单虽然 orderType=2 (实供)，但其计价模式是 "国网价 × 点位"，所以这里特殊处理
            boolean isPointPricing = order.getOrderType() == 1 || Integer.valueOf(2).equals(order.getSourceType());

            if (isPointPricing) {
                // 获取国网价（对于平台订单，通常 item.price 已经是国网价，但为了保险可以重新查）
                // 如果 item.price 已经有值且正确，可以直接用。这里假设 item.getProductSku() 有效
                BigDecimal stateGridPrice = priceConfigService.getPrice(
                    item.getProductSku(), 1, null, null
                );
                
                // 优先使用商品行点位，其次使用订单头点位
                BigDecimal points = item.getPoints();
                if (points == null) {
                    points = order.getPoints();
                }
                
                // 如果是平台订单且点位仍为空，可能需要给个默认值或者报错
                // 借码订单必须有点位
                if (order.getOrderType() == 1) {
                    CoolPreconditions.check(points == null, "借码订单必须配置点位");
                } else if (points == null) {
                    // 平台订单如果没点位，默认按100%（原价）
                    points = new BigDecimal("100");
                }
                
                item.setPrice(stateGridPrice);
                item.setPoints(points); // 确保 item 也有点位
                item.setAmount(stateGridPrice
                    .multiply(new BigDecimal(item.getQuantity()))
                    .multiply(points.divide(BigDecimal.valueOf(100)))
                    .setScale(2, RoundingMode.HALF_UP));
            }
            // 普通实供订单 (orderType=2 且 sourceType!=2)：区域价或服务商价 × 数量
            else {
                Integer priceType;
                String regionCode = null;
                Long providerId = null;
                
                // 区域订单用区域价
                if (order.getIsRegional() != null && order.getIsRegional() == 1) {
                    priceType = 2;
                    regionCode = order.getRegionCode();
                } else {
                    // 非区域订单用服务商价
                    priceType = 3;
                    providerId = order.getProviderId();
                }
                
                BigDecimal price = priceConfigService.getPrice(
                    item.getProductSku(), priceType, regionCode, providerId
                );
                
                item.setPrice(price);
                // 实供订单清空点位，避免混淆
                item.setPoints(null);
                item.setAmount(price
                    .multiply(new BigDecimal(item.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP));
            }
            
            totalAmount = totalAmount.add(item.getAmount());
            totalQuantity += item.getQuantity();
        }
        
        order.setTotalAmount(totalAmount);
        order.setTotalQuantity(totalQuantity);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startApproval(Long orderId) {
        SalesOrderEntity order = getById(orderId);
        CoolPreconditions.check(order == null, "销售订单不存在");
        
        try {
            Long applicantId = CoolSecurityUtil.getCurrentUserId();
            String applicantName = CoolSecurityUtil.getAdminUsername();
            
            boolean started = approvalProcessService.startApproval(
                1, // instanceType: 1-销售订单
                order.getId(),
                order.getOrderNo(),
                applicantId,
                applicantName
            );
            
            if (started) {
                log.info("销售订单审批流程启动成功，订单ID: {}, 订单号: {}", order.getId(), order.getOrderNo());
            } else {
                log.warn("销售订单审批流程启动失败（可能未配置审批流），订单ID: {}, 订单号: {}", order.getId(), order.getOrderNo());
            }
        } catch (Exception e) {
            log.error("启动销售订单审批流程异常，订单ID: {}, 订单号: {}", order.getId(), order.getOrderNo(), e);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void flowOrder(Long orderId, Long receiverId) {
        SalesOrderEntity order = getById(orderId);
        CoolPreconditions.check(order == null, "订单不存在");
        CoolPreconditions.check(order.getApprovalStatus() != 1, "订单未审核通过，不能流转");
        
        // TODO: 订单流转
        // 1. 获取流转配置
        // 2. 创建流转记录
        // 3. 更新订单流转状态
        // 4. 发送通知给接收人
        
        order.setFlowStatus(1); // 已流转
        order.setFlowReceiverId(receiverId);
        order.setFlowTime(LocalDateTime.now());
        updateById(order);
        
        log.info("订单流转成功，orderId: {}, receiverId: {}", orderId, receiverId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeliveryStatus(Long orderId) {
        List<SalesDeliveryEntity> deliveries = deliveryService.listByOrderId(orderId);
        SalesOrderEntity order = getById(orderId);
        
        if (deliveries == null || deliveries.isEmpty()) {
            order.setDeliveryStatus(0); // 未发货
            order.setDeliveredQuantity(0);
        } else {
            int deliveredQuantity = deliveries.stream()
                .mapToInt(SalesDeliveryEntity::getQuantity)
                .sum();
            
            order.setDeliveredQuantity(deliveredQuantity);
            
            if (deliveredQuantity >= order.getTotalQuantity()) {
                order.setDeliveryStatus(2); // 已发货
            } else if (deliveredQuantity > 0) {
                order.setDeliveryStatus(1); // 部分发货
            } else {
                order.setDeliveryStatus(0); // 未发货
            }
        }
        
        updateById(order);
        
        // 更新订单整体状态
        updateOrderStatus(orderId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInvoiceStatus(Long orderId) {
        List<SalesInvoiceEntity> invoices = invoiceService.listByOrderId(orderId);
        SalesOrderEntity order = getById(orderId);
        
        if (invoices == null || invoices.isEmpty()) {
            // 没有发票记录，状态为未开票
            order.setInvoiceStatus(0);
            order.setInvoicedAmount(BigDecimal.ZERO);
        } else {
            // 统计已开票和未开票的数量
            long invoicedCount = invoices.stream()
                .filter(invoice -> invoice.getStatus() != null && invoice.getStatus() == 1)
                .count();
            long uninvoicedCount = invoices.stream()
                .filter(invoice -> invoice.getStatus() == null || invoice.getStatus() == 0)
                .count();
            
            // 计算已开票金额（只统计状态为已开票的）
            BigDecimal invoicedAmount = invoices.stream()
                .filter(invoice -> invoice.getStatus() != null && invoice.getStatus() == 1)
                .map(SalesInvoiceEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            order.setInvoicedAmount(invoicedAmount);
            
            // 根据发票状态判断订单开票状态
            if (invoicedCount > 0 && uninvoicedCount == 0) {
                // 所有发票都已开票
                order.setInvoiceStatus(2);
            } else if (invoicedCount > 0 && uninvoicedCount > 0) {
                // 部分已开票，部分未开票
                order.setInvoiceStatus(1);
            } else {
                // 没有已开票的发票
                order.setInvoiceStatus(0);
            }
        }
        
        updateById(order);
        
        // 更新订单整体状态
        updateOrderStatus(orderId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePaymentStatus(Long orderId) {
        List<SalesPaymentEntity> payments = paymentService.listByOrderId(orderId);
        SalesOrderEntity order = getById(orderId);
        
        if (payments == null || payments.isEmpty()) {
            order.setPaymentStatus(0); // 未回款
            order.setPaidAmount(BigDecimal.ZERO);
        } else {
            // 统计已确认的回款金额
            BigDecimal paidAmount = payments.stream()
                .filter(p -> p.getStatus() == 1) // 只统计已确认的回款
                .map(SalesPaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            order.setPaidAmount(paidAmount);
            
            // 检查是否有待确认的回款记录
            boolean hasUnconfirmedPayment = payments.stream()
                .anyMatch(p -> p.getStatus() == 0);
            
            // 只有当所有回款都已确认，且已确认金额达到订单总额时，才标记为已回款
            if (!hasUnconfirmedPayment && paidAmount.compareTo(order.getTotalAmount()) >= 0) {
                order.setPaymentStatus(2); // 已回款
            } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                order.setPaymentStatus(1); // 部分回款
            } else {
                order.setPaymentStatus(0); // 未回款
            }
        }
        
        updateById(order);
        
        // 更新订单整体状态
        updateOrderStatus(orderId);
    }
    
    /**
     * 更新订单整体状态
     * 借码订单：已回款 -> 已完成
     * 实供订单：已发货 + 已开票 + 已回款 -> 已完成
     */
    private void updateOrderStatus(Long orderId) {
        SalesOrderEntity order = getById(orderId);
        if (order == null) return;
        
        // 已取消的订单不处理
        if (order.getStatus() == 4) return;
        
        boolean isCompleted = false;
        
        if (order.getOrderType() == 1) {
            // 借码订单：只要已回款即视为完成（不需要发货和开票）
            if (order.getPaymentStatus() == 2) {
                isCompleted = true;
            }
        } else {
            // 实供订单：需要已发货、已开票、已回款
            if (order.getDeliveryStatus() == 2 && 
                order.getInvoiceStatus() == 2 && 
                order.getPaymentStatus() == 2) {
                isCompleted = true;
            }
        }
        
        if (isCompleted) {
            if (order.getStatus() != 3) {
                order.setStatus(3); // 已完成
                updateById(order);
            }
        } else {
            // 如果之前是已完成，但现在条件不满足了，则回退到已确认
            // 注意：这里可能会覆盖"采购中"的状态，但"采购中"目前没有明确的自动触发逻辑，
            // 且如果已经到了可能完成的阶段，回退到已确认也是合理的兜底
            if (order.getStatus() == 3) {
                order.setStatus(1); // 已确认
                updateById(order);
            }
        }
    }
    
    @Override
    public void modifyBefore(JSONObject requestParams, SalesOrderEntity entity, ModifyEnum type) {
        // 新增前校验
        if (type == ModifyEnum.ADD) {
            // 生成订单号
            if (StrUtil.isBlank(entity.getOrderNo())) {
                entity.setOrderNo(generateOrderNo());
            }
            
            // 校验订单号唯一性
            QueryWrapper qw = QueryWrapper.create()
                .where(SALES_ORDER_ENTITY.ORDER_NO.eq(entity.getOrderNo()));
            SalesOrderEntity exists = getOne(qw);
            CoolPreconditions.check(exists != null, "订单号已存在");
            // 设置默认值
            // 创建方式：若未传入，则默认手动创建销售合同（3）
            if (entity.getCreateMode() == null) {
                entity.setCreateMode(3);
            }
            // 默认订单来源类型：手动创建（3）
            if (entity.getSourceType() == null) {
                entity.setSourceType(3);
            }
            // 校验服务商（报备订单必填）
            if (Integer.valueOf(1).equals(entity.getSourceType()) && entity.getProviderId() == null) {
                throw new CoolException("服务商报备订单必须选择服务商");
            }
            if (entity.getStatus() == null) {
                entity.setStatus(0); // 待确认
            }
            if (entity.getApprovalStatus() == null) {
                entity.setApprovalStatus(Integer.valueOf(1).equals(entity.getCreateMode()) ? 1 : 0); // 自动创建免审核
            }
            // 发票类型：若未传入，则默认增值税专用发票（1）
            if (entity.getInvoiceType() == null) {
                entity.setInvoiceType(1);
            }
        }
        
        // 修改前校验
        if (type == ModifyEnum.UPDATE) {
            SalesOrderEntity oldEntity = getById(entity.getId());
            CoolPreconditions.check(oldEntity == null, "订单不存在");
            
            // 已完成或已取消的订单不能修改
            CoolPreconditions.check(oldEntity.getStatus() == 3 || oldEntity.getStatus() == 4, 
                "已完成或已取消的订单不能修改");

            // 如果当前是审核驳回状态，且用户进行了修改提交，则重置为待审核
            if (oldEntity.getApprovalStatus() != null && oldEntity.getApprovalStatus() == 2) {
                entity.setApprovalStatus(0); // 重置为待审核
                entity.setRejectReason(""); // 清空驳回原因
            }
        }
    }
    
    @Override
    public void modifyAfter(JSONObject requestParams, SalesOrderEntity entity, ModifyEnum type) {
        // 新增后处理
        if (type == ModifyEnum.ADD) {
            // 保存订单明细
            if (entity.getItems() != null && !entity.getItems().isEmpty()) {
                salesOrderItemService.batchSave(entity.getId(), entity.getItems());
            }
            
            // 如果是手动创建（2 或 3），启动审批流程
            if (Integer.valueOf(2).equals(entity.getCreateMode()) || Integer.valueOf(3).equals(entity.getCreateMode())) {
                startApproval(entity.getId());
            }
        }
        
        // 修改后处理
        if (type == ModifyEnum.UPDATE) {
            // 如果订单状态被重置为待审核（0），说明是驳回后重新提交，需要重新启动审批流程
            if (entity.getApprovalStatus() != null && entity.getApprovalStatus() == 0) {
                // 检查是否已经有待审批的记录，避免重复创建
                List<ApprovalRecordEntity> pendingRecords = approvalProcessService.getApprovalProgress(1, entity.getId());
                boolean hasPending = pendingRecords.stream().anyMatch(r -> r.getStatus() == 0);
                
                if (!hasPending) {
                    log.info("销售订单重新提交，重启审批流程，订单ID: {}", entity.getId());
                    startApproval(entity.getId());
                }
            }
        }
    }
    
    @Override
    public Object info(Long id) {
        // 查询主表数据
        SalesOrderEntity entity = getById(id);
        if (entity != null) {
            // 查询关联的商品明细
            List<SalesOrderItemEntity> items = salesOrderItemService.listByOrderId(id);
            entity.setItems(items);
        }
        return entity;
    }
}

