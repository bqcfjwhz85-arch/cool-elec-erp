package com.cool.modules.purchase.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.config.entity.ApprovalRecordEntity;
import com.cool.modules.purchase.entity.PurchaseInvoiceEntity;
import com.cool.modules.purchase.entity.PurchaseOrderEntity;
import com.cool.modules.purchase.entity.PurchaseOrderItemEntity;
import com.cool.modules.purchase.entity.PurchasePaymentEntity;
import com.cool.modules.purchase.mapper.PurchaseOrderMapper;
import com.cool.modules.purchase.service.PurchaseInvoiceService;
import com.cool.modules.purchase.service.PurchaseOrderItemService;
import com.cool.modules.purchase.service.PurchaseOrderService;
import com.cool.modules.purchase.service.PurchasePaymentService;
import com.cool.modules.sale.entity.SalesOrderEntity;
import com.cool.modules.sale.entity.SalesOrderItemEntity;
import com.cool.modules.sale.service.SalesOrderItemService;
import com.cool.modules.sale.service.SalesOrderService;
import com.cool.modules.config.service.ApprovalProcessService;
import com.cool.core.util.CoolSecurityUtil;
import com.cool.core.base.ModifyEnum;
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

import static com.cool.modules.purchase.entity.table.PurchaseOrderEntityTableDef.PURCHASE_ORDER_ENTITY;

/**
 * 采购订单服务实现类
 */
@Slf4j
@Service
public class PurchaseOrderServiceImpl extends BaseServiceImpl<PurchaseOrderMapper, PurchaseOrderEntity>
        implements PurchaseOrderService {
    
    private final PurchaseOrderItemService purchaseOrderItemService;
    private final PurchaseInvoiceService invoiceService;
    private final PurchasePaymentService paymentService;
    private final SalesOrderService salesOrderService;
    private final SalesOrderItemService salesOrderItemService;
    private final ApprovalProcessService approvalProcessService;
    
    // 使用构造函数注入，并对循环依赖的服务使用@Lazy
    public PurchaseOrderServiceImpl(
            PurchaseOrderItemService purchaseOrderItemService,
            @Lazy PurchaseInvoiceService invoiceService,
            @Lazy PurchasePaymentService paymentService,
            @Lazy SalesOrderService salesOrderService,
            @Lazy SalesOrderItemService salesOrderItemService,
            @Lazy ApprovalProcessService approvalProcessService) {
        this.purchaseOrderItemService = purchaseOrderItemService;
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
        this.salesOrderService = salesOrderService;
        this.salesOrderItemService = salesOrderItemService;
        this.approvalProcessService = approvalProcessService;
    }
    
    @Override
    public PurchaseOrderEntity getByOrderNo(String orderNo) {
        return getOne(QueryWrapper.create()
            .where(PURCHASE_ORDER_ENTITY.ORDER_NO.eq(orderNo)));
    }
    
    @Override
    public String generateOrderNo() {
        String prefix = "PO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        
        // 查询当月最大序号
        QueryWrapper qw = QueryWrapper.create()
            .select(PURCHASE_ORDER_ENTITY.ORDER_NO)
            .where(PURCHASE_ORDER_ENTITY.ORDER_NO.like(prefix + "%"))
            .orderBy(PURCHASE_ORDER_ENTITY.ORDER_NO, false)
            .limit(1);
        
        PurchaseOrderEntity lastOrder = getOne(qw);
        int sequence = 1;
        
        if (lastOrder != null && StrUtil.isNotBlank(lastOrder.getOrderNo())) {
            String lastNo = lastOrder.getOrderNo();
            if (lastNo.length() >= 12) {
                sequence = Integer.parseInt(lastNo.substring(8)) + 1;
            }
        }
        
        return prefix + String.format("%06d", sequence);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrderEntity generateFromSalesOrder(Long salesOrderId, JSONObject params) {
        log.info("从销售订单生成采购订单，salesOrderId: {}, params: {}", salesOrderId, params);
        
        // 1. 获取销售订单信息
        SalesOrderEntity salesOrder = salesOrderService.getById(salesOrderId);
        CoolPreconditions.check(salesOrder == null, "销售订单不存在");
        CoolPreconditions.check(salesOrder.getStatus() == 4, "已取消的订单无法生成采购订单");
        
        // 获取销售订单明细
        List<SalesOrderItemEntity> salesItems = salesOrderItemService.listByOrderId(salesOrderId);
        CoolPreconditions.check(salesItems == null || salesItems.isEmpty(), "销售订单商品明细不能为空");
        
        // 2. 获取生成参数
        Long supplierId = params.getLong("supplierId");
        String supplierName = params.getStr("supplierName");
        String supplierContact = params.getStr("supplierContact");
        String supplierPhone = params.getStr("supplierPhone");
        String bankName = params.getStr("bankName");
        String bankAccount = params.getStr("bankAccount");
        Long priceAgreementId = params.getLong("priceAgreementId");
        String priceAgreementName = params.getStr("priceAgreementName");
        LocalDateTime purchaseDate = params.get("purchaseDate", LocalDateTime.class);
        String remark = params.getStr("remark");
        
        CoolPreconditions.check(supplierId == null, "请选择供应商");
        CoolPreconditions.check(StrUtil.isBlank(supplierName), "供应商名称不能为空");
        CoolPreconditions.check(purchaseDate == null, "采购日期不能为空");
        
        // 判断来源类型：1-一键生成（自动） 2-手动创建
        Integer sourceType = params.getInt("sourceType", 1);
        
        // 3. 创建采购订单
        PurchaseOrderEntity purchaseOrder = new PurchaseOrderEntity();
        purchaseOrder.setOrderNo(generateOrderNo());
        purchaseOrder.setSourceType(sourceType);
        purchaseOrder.setSalesOrderId(salesOrderId);
        purchaseOrder.setSalesOrderNo(salesOrder.getOrderNo());
        purchaseOrder.setSalesAmount(salesOrder.getTotalAmount());
        purchaseOrder.setSupplierId(supplierId);
        purchaseOrder.setSupplierName(supplierName);
        purchaseOrder.setSupplierContact(supplierContact);
        purchaseOrder.setSupplierPhone(supplierPhone);
        purchaseOrder.setBankName(bankName);
        purchaseOrder.setBankAccount(bankAccount);
        purchaseOrder.setOrderType(salesOrder.getOrderType());
        purchaseOrder.setPriceAgreementId(priceAgreementId);
        purchaseOrder.setPriceAgreementName(priceAgreementName);
        purchaseOrder.setPurchaseDate(purchaseDate);
        purchaseOrder.setRemark(remark);
        
        // 设置初始状态
        if (sourceType == 1) {
            // 一键生成，直接确认，无需审批
            purchaseOrder.setStatus(1); // 已确认
            purchaseOrder.setApprovalStatus(1); // 审核通过
        } else {
            // 手动创建，需要审批
            purchaseOrder.setStatus(0); // 待确认
            purchaseOrder.setApprovalStatus(0); // 待审核
        }
        
        purchaseOrder.setPaymentStatus(0);
        purchaseOrder.setInvoiceStatus(0);
        purchaseOrder.setPaidAmount(BigDecimal.ZERO);
        purchaseOrder.setInvoicedAmount(BigDecimal.ZERO);
        
        // 4. 处理订单商品明细
        List<PurchaseOrderItemEntity> purchaseItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        
        // 直接使用前端传来的商品明细数据
        List<JSONObject> itemsData = params.getJSONArray("items").toList(JSONObject.class);
        CoolPreconditions.check(itemsData == null || itemsData.isEmpty(), "商品明细不能为空");
        
        log.info("处理商品明细，数量: {}", itemsData.size());
        
        for (JSONObject itemData : itemsData) {
            PurchaseOrderItemEntity purchaseItem = new PurchaseOrderItemEntity();
            
            // 从参数中获取商品信息
            String productSku = itemData.getStr("productSku");
            String productName = itemData.getStr("productName");
            String brand = itemData.getStr("brand");
            String specification = itemData.getStr("specification");
            String unit = itemData.getStr("unit");
            Integer quantity = itemData.getInt("quantity");
            BigDecimal settlementPrice = itemData.getBigDecimal("settlementPrice");
            BigDecimal points = itemData.getBigDecimal("points");
            
            // 验证必填字段
            CoolPreconditions.check(StrUtil.isBlank(productSku), "商品SKU不能为空");
            CoolPreconditions.check(quantity == null || quantity <= 0, "商品数量必须大于0");
            
            // 借码订单（orderType = 1）允许结算价为0，实供订单（orderType = 2）必须大于0
            if (purchaseOrder.getOrderType() != null && purchaseOrder.getOrderType() == 1) {
                // 借码订单：结算价可以为0或大于0，但不能为负数
                CoolPreconditions.check(settlementPrice == null || settlementPrice.compareTo(BigDecimal.ZERO) < 0, 
                    String.format("商品%s的结算价不能为负数", productName));
            } else {
                // 实供订单：结算价必须大于0
                CoolPreconditions.check(settlementPrice == null || settlementPrice.compareTo(BigDecimal.ZERO) <= 0, 
                    String.format("商品%s的结算价必须大于0", productName));
            }
            
            purchaseItem.setProductSku(productSku);
            purchaseItem.setProductName(productName);
            purchaseItem.setBrand(brand);
            purchaseItem.setSpecification(specification);
            purchaseItem.setUnit(unit);
            purchaseItem.setQuantity(quantity);
            purchaseItem.setSettlementPrice(settlementPrice);
            purchaseItem.setPoints(points);
            
            // 计算行金额
            BigDecimal amount;
            if (purchaseOrder.getOrderType() != null && purchaseOrder.getOrderType() == 1 && points != null && points.compareTo(BigDecimal.ZERO) > 0) {
                // 借码订单：金额 = 数量 × 结算价 × (点位% / 100)
                amount = settlementPrice.multiply(new BigDecimal(quantity))
                    .multiply(points)
                    .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            } else {
                // 实供订单或无点位：金额 = 数量 × 结算价
                amount = settlementPrice.multiply(new BigDecimal(quantity))
                    .setScale(2, RoundingMode.HALF_UP);
            }
            purchaseItem.setAmount(amount);
            
            totalAmount = totalAmount.add(amount);
            totalQuantity += quantity;
            
            purchaseItem.setInvoicedAmount(BigDecimal.ZERO);
            purchaseItem.setPaidAmount(BigDecimal.ZERO);
            
            purchaseItems.add(purchaseItem);
            
            log.info("添加商品明细: SKU={}, 名称={}, 数量={}, 单价={}, 金额={}", 
                productSku, productName, quantity, settlementPrice, amount);
        }
        
        log.info("商品明细处理完成，总数量: {}, 总金额: {}", totalQuantity, totalAmount);
        
        // 5. 设置订单总金额和数量
        purchaseOrder.setTotalAmount(totalAmount);
        purchaseOrder.setTotalQuantity(totalQuantity);
        
        // 计算毛利
        BigDecimal profit = calculateProfit(salesOrder.getTotalAmount(), totalAmount);
        purchaseOrder.setProfit(profit);
        
        // 6. 保存订单
        save(purchaseOrder);
        
        // 7. 保存订单明细
        for (PurchaseOrderItemEntity item : purchaseItems) {
            item.setOrderId(purchaseOrder.getId());
        }
        purchaseOrderItemService.saveBatch(purchaseItems);
        
        // 8. 更新销售订单状态为"采购中"
        salesOrder.setStatus(2);
        salesOrderService.updateById(salesOrder);
        
        // 9. 如果是手动创建，启动审批流程
        if (sourceType == 2) {
            try {
                Long applicantId = CoolSecurityUtil.getCurrentUserId();
                String applicantName = CoolSecurityUtil.getAdminUsername();
                
                boolean started = approvalProcessService.startApproval(
                    2, // instanceType: 2-采购订单
                    purchaseOrder.getId(),
                    purchaseOrder.getOrderNo(),
                    applicantId,
                    applicantName
                );
                
                if (started) {
                    log.info("采购订单审批流程启动成功，订单ID: {}, 订单号: {}", purchaseOrder.getId(), purchaseOrder.getOrderNo());
                } else {
                    log.warn("采购订单审批流程启动失败（可能未配置审批流），订单ID: {}, 订单号: {}", purchaseOrder.getId(), purchaseOrder.getOrderNo());
                }
            } catch (Exception e) {
                log.error("启动采购订单审批流程异常，订单ID: {}, 订单号: {}", purchaseOrder.getId(), purchaseOrder.getOrderNo(), e);
            }
        }
        
        log.info("采购订单生成成功，purchaseOrderId: {}, purchaseOrderNo: {}", 
            purchaseOrder.getId(), purchaseOrder.getOrderNo());
        
        // 10. 加载商品明细后返回
        purchaseOrder.setItems(purchaseItems);
        log.info("设置商品明细到订单对象，items数量: {}", purchaseItems.size());
        log.info("返回的订单对象items字段: {}", purchaseOrder.getItems());
        
        return purchaseOrder;
    }
    
    /**
     * 从商品明细列表中查找指定SKU的数据
     */
    private JSONObject findItemData(List<JSONObject> itemsData, String productSku) {
        if (itemsData == null || itemsData.isEmpty()) {
            return null;
        }
        for (JSONObject item : itemsData) {
            if (productSku.equals(item.getStr("productSku"))) {
                return item;
            }
        }
        return null;
    }
    
    @Override
    public BigDecimal calculateProfit(BigDecimal salesAmount, BigDecimal purchaseAmount) {
        if (salesAmount == null || purchaseAmount == null) {
            return BigDecimal.ZERO;
        }
        return salesAmount.subtract(purchaseAmount).setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInvoiceStatus(Long orderId) {
        PurchaseOrderEntity order = getById(orderId);
        if (order == null) {
            return;
        }
        
        // 查询发票信息
        List<PurchaseInvoiceEntity> invoices = invoiceService.listByOrderId(orderId);
        
        if (invoices == null || invoices.isEmpty()) {
            // 没有发票，状态为未开票
            order.setInvoiceStatus(0);
            order.setInvoicedAmount(BigDecimal.ZERO);
        } else {
            // 计算已开票金额（排除已作废的发票）
            BigDecimal invoicedAmount = invoices.stream()
                .filter(i -> i.getStatus() == null || i.getStatus() != 2) // 排除已作废
                .map(PurchaseInvoiceEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            order.setInvoicedAmount(invoicedAmount);
            
            // 判断开票状态
            if (invoicedAmount.compareTo(BigDecimal.ZERO) == 0) {
                order.setInvoiceStatus(0); // 未开票
            } else if (invoicedAmount.compareTo(order.getTotalAmount()) >= 0) {
                order.setInvoiceStatus(2); // 已开完
            } else {
                order.setInvoiceStatus(1); // 部分开票
            }
        }
        
        updateById(order);
        
        // 如果已开票且已付清，更新订单状态为已完成，并同步更新关联的销售订单状态
        if (order.getInvoiceStatus() == 2 && order.getPaymentStatus() == 2) {
            order.setStatus(2); // 已完成
            updateById(order);
            // 同步更新对应的销售订单状态，从采购中(2)变为已完成(3)
            // 需要检查销售订单的所有状态（发货、开票、回款）都已完成
            if (order.getSalesOrderId() != null) {
                SalesOrderEntity salesOrder = salesOrderService.getById(order.getSalesOrderId());
                if (salesOrder != null && salesOrder.getStatus() != null && salesOrder.getStatus() == 2) {
                    // 检查销售订单的所有状态是否都已完成
                    boolean allCompleted = true;
                    
                    // 检查发货状态：2-已发货
                    if (salesOrder.getDeliveryStatus() == null || salesOrder.getDeliveryStatus() != 2) {
                        allCompleted = false;
                    }
                    
                    // 检查开票状态：2-已开票
                    if (salesOrder.getInvoiceStatus() == null || salesOrder.getInvoiceStatus() != 2) {
                        allCompleted = false;
                    }
                    
                    // 检查回款状态：2-已回款
                    if (salesOrder.getPaymentStatus() == null || salesOrder.getPaymentStatus() != 2) {
                        allCompleted = false;
                    }
                    
                    // 只有所有状态都已完成，才更新销售订单状态为已完成
                    if (allCompleted) {
                        salesOrder.setStatus(3); // 已完成
                        salesOrderService.updateById(salesOrder);
                        log.info("采购订单已完成且销售订单所有流程(发货、开票、回款)已完成，更新销售订单状态为已完成，采购订单ID: {}, 销售订单ID: {}", orderId, order.getSalesOrderId());
                    } else {
                        log.info("采购订单已完成，但销售订单还有未完成的流程(发货:{}, 开票:{}, 回款:{})，销售订单ID: {}", 
                            salesOrder.getDeliveryStatus(), salesOrder.getInvoiceStatus(), salesOrder.getPaymentStatus(), order.getSalesOrderId());
                    }
                }
            }
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePaymentStatus(Long orderId) {
        PurchaseOrderEntity order = getById(orderId);
        if (order == null) {
            return;
        }
        
        // 查询付款信息
        List<PurchasePaymentEntity> payments = paymentService.listByOrderId(orderId);
        
        if (payments == null || payments.isEmpty()) {
            // 没有付款，状态为未付款
            order.setPaymentStatus(0);
            order.setPaidAmount(BigDecimal.ZERO);
        } else {
            // 计算已付款金额
            BigDecimal paidAmount = payments.stream()
                .map(PurchasePaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            order.setPaidAmount(paidAmount);
            
            // 检查是否所有付款都已确认
            boolean allConfirmed = payments.stream()
                .allMatch(p -> p.getStatus() != null && p.getStatus() == 1);
            
            // 判断付款状态
            if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
                order.setPaymentStatus(0); // 未付款
            } else if (paidAmount.compareTo(order.getTotalAmount()) >= 0 && allConfirmed) {
                order.setPaymentStatus(2); // 已付清
            } else {
                order.setPaymentStatus(1); // 部分付款
            }
        }
        
        updateById(order);
        
        // 如果已开票且已付清，更新订单状态为已完成，并同步更新关联的销售订单状态
        if (order.getInvoiceStatus() == 2 && order.getPaymentStatus() == 2) {
            order.setStatus(2); // 已完成
            updateById(order);
            // 同步更新对应的销售订单状态，从采购中(2)变为已完成(3)
            // 需要检查销售订单的所有状态（发货、开票、回款）都已完成
            if (order.getSalesOrderId() != null) {
                SalesOrderEntity salesOrder = salesOrderService.getById(order.getSalesOrderId());
                if (salesOrder != null && salesOrder.getStatus() != null && salesOrder.getStatus() == 2) {
                    // 检查销售订单的所有状态是否都已完成
                    boolean allCompleted = true;
                    
                    // 检查发货状态：2-已发货
                    if (salesOrder.getDeliveryStatus() == null || salesOrder.getDeliveryStatus() != 2) {
                        allCompleted = false;
                    }
                    
                    // 检查开票状态：2-已开票
                    if (salesOrder.getInvoiceStatus() == null || salesOrder.getInvoiceStatus() != 2) {
                        allCompleted = false;
                    }
                    
                    // 检查回款状态：2-已回款
                    if (salesOrder.getPaymentStatus() == null || salesOrder.getPaymentStatus() != 2) {
                        allCompleted = false;
                    }
                    
                    // 只有所有状态都已完成，才更新销售订单状态为已完成
                    if (allCompleted) {
                        salesOrder.setStatus(3); // 已完成
                        salesOrderService.updateById(salesOrder);
                        log.info("采购订单已完成且销售订单所有流程(发货、开票、回款)已完成，更新销售订单状态为已完成，采购订单ID: {}, 销售订单ID: {}", orderId, order.getSalesOrderId());
                    } else {
                        log.info("采购订单已完成，但销售订单还有未完成的流程(发货:{}, 开票:{}, 回款:{})，销售订单ID: {}", 
                            salesOrder.getDeliveryStatus(), salesOrder.getInvoiceStatus(), salesOrder.getPaymentStatus(), order.getSalesOrderId());
                    }
                }
            }
        }
    }
    
    @Override
    public void modifyBefore(JSONObject requestParams, PurchaseOrderEntity entity, ModifyEnum type) {
        // 新增前校验
        if (type == ModifyEnum.ADD) {
            // 生成订单号
            if (StrUtil.isBlank(entity.getOrderNo())) {
                entity.setOrderNo(generateOrderNo());
            }
            
            // 校验订单号唯一性
            QueryWrapper qw = QueryWrapper.create()
                .where(PURCHASE_ORDER_ENTITY.ORDER_NO.eq(entity.getOrderNo()));
            PurchaseOrderEntity exists = getOne(qw);
            CoolPreconditions.check(exists != null, "订单号已存在");
            
            // 设置默认值
            if (entity.getSourceType() == null) {
                entity.setSourceType(2); // 默认为手动创建
            }
            if (entity.getStatus() == null) {
                entity.setStatus(0); // 默认为待确认
            }
            if (entity.getApprovalStatus() == null) {
                entity.setApprovalStatus(0); // 默认为待审核
            }
            if (entity.getPaymentStatus() == null) {
                entity.setPaymentStatus(0); // 默认为未付款
            }
            if (entity.getInvoiceStatus() == null) {
                entity.setInvoiceStatus(0); // 默认为未开票
            }
            if (entity.getTotalAmount() == null) {
                entity.setTotalAmount(BigDecimal.ZERO);
            }
            if (entity.getTotalQuantity() == null) {
                entity.setTotalQuantity(0);
            }
            if (entity.getPaidAmount() == null) {
                entity.setPaidAmount(BigDecimal.ZERO);
            }
            if (entity.getInvoicedAmount() == null) {
                entity.setInvoicedAmount(BigDecimal.ZERO);
            }
        }
        
        // 修改前校验
        if (type == ModifyEnum.UPDATE) {
            PurchaseOrderEntity oldEntity = getById(entity.getId());
            CoolPreconditions.check(oldEntity == null, "订单不存在");
            
            // 已完成或已取消的订单不能修改
            CoolPreconditions.check(oldEntity.getStatus() == 2 || oldEntity.getStatus() == 3, 
                "已完成或已取消的订单不能修改");

            // 如果当前是审核驳回状态，且用户进行了修改提交，则重置为待审核
            if (oldEntity.getApprovalStatus() != null && oldEntity.getApprovalStatus() == 2) {
                entity.setApprovalStatus(0); // 重置为待审核
                entity.setRejectReason(""); // 清空驳回原因
            }
        }
    }

    @Override
    public void modifyAfter(JSONObject requestParams, PurchaseOrderEntity entity, ModifyEnum type) {
        // 新增后处理
        if (type == ModifyEnum.ADD) {
            // 保存订单明细（如果有）
            // 注意：采购订单明细通常在 generateFromSalesOrder 中处理
            
            // 如果是手动创建（sourceType=2），启动审批流程
            if (entity.getSourceType() != null && entity.getSourceType() == 2) {
                try {
                    Long applicantId = CoolSecurityUtil.getCurrentUserId();
                    String applicantName = CoolSecurityUtil.getAdminUsername();
                    
                    boolean started = approvalProcessService.startApproval(
                        2, // instanceType: 2-采购订单
                        entity.getId(),
                        entity.getOrderNo(),
                        applicantId,
                        applicantName
                    );
                    
                    if (started) {
                        log.info("采购订单审批流程启动成功，订单ID: {}, 订单号: {}", entity.getId(), entity.getOrderNo());
                    } else {
                        log.warn("采购订单审批流程启动失败（可能未配置审批流），订单ID: {}, 订单号: {}", entity.getId(), entity.getOrderNo());
                    }
                } catch (Exception e) {
                    log.error("启动采购订单审批流程异常，订单ID: {}, 订单号: {}", entity.getId(), entity.getOrderNo(), e);
                }
            }
        }
        
        // 修改后处理
        if (type == ModifyEnum.UPDATE) {
            // 如果订单状态被重置为待审核（0），说明是驳回后重新提交，需要重新启动审批流程
            if (entity.getApprovalStatus() != null && entity.getApprovalStatus() == 0) {
                // 检查是否已经有待审批的记录，避免重复创建
                List<ApprovalRecordEntity> pendingRecords = approvalProcessService.getApprovalProgress(2, entity.getId());
                boolean hasPending = pendingRecords.stream().anyMatch(r -> r.getStatus() == 0);
                
                if (!hasPending) {
                    log.info("采购订单重新提交，重启审批流程，订单ID: {}", entity.getId());
                    try {
                        Long applicantId = CoolSecurityUtil.getCurrentUserId();
                        String applicantName = CoolSecurityUtil.getAdminUsername();
                        
                        approvalProcessService.startApproval(
                            2, // instanceType: 2-采购订单
                            entity.getId(),
                            entity.getOrderNo(),
                            applicantId,
                            applicantName
                        );
                    } catch (Exception e) {
                        log.error("重启采购订单审批流程异常，订单ID: {}", entity.getId(), e);
                    }
                }
            }
        }
    }
}

