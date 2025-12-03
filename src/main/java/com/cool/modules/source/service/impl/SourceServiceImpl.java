package com.cool.modules.source.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.base.ModifyEnum;
import com.cool.core.exception.CoolException;
import com.cool.core.exception.CoolPreconditions;
import com.cool.core.util.CoolSecurityUtil;
import com.cool.modules.config.entity.ApprovalRecordEntity;
import com.cool.modules.config.entity.ProductEntity;
import com.cool.modules.config.entity.ProviderEntity;
import com.cool.modules.config.service.PlatformService;
import com.cool.modules.config.service.PriceConfigService;
import com.cool.modules.config.service.ProviderService;
import com.cool.modules.customer.entity.CustomerEntity;
import com.cool.modules.customer.service.CustomerService;
import com.cool.modules.product.service.ProductService;
import com.cool.modules.sale.entity.SalesOrderEntity;
import com.cool.modules.sale.service.SalesOrderService;
import com.cool.modules.source.entity.SourceEntity;
import com.cool.modules.source.entity.SourceItemEntity;
import com.cool.modules.source.mapper.SourceMapper;
import com.cool.modules.source.service.SourceService;
import com.cool.modules.source.service.SourceItemService;
import com.cool.modules.config.entity.PlatformEntity;
import com.cool.modules.config.service.ApprovalProcessService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.cool.modules.source.entity.table.SourceEntityTableDef.SOURCE_ENTITY;

/**
 * 订单来源报备服务实现类
 */
@Slf4j
@Service
public class SourceServiceImpl extends BaseServiceImpl<SourceMapper, SourceEntity> 
        implements SourceService {
    
    private final ProductService productService;
    private final CustomerService customerService;
    private final PriceConfigService priceConfigService;
    private final ProviderService providerService;
    private final PlatformService platformService;
    private final SourceItemService sourceItemService;
    private final SalesOrderService salesOrderService;
    private final ApprovalProcessService approvalProcessService;
    
    // 使用构造函数注入，并对循环依赖的服务使用@Lazy
    public SourceServiceImpl(
            ProductService productService,
            CustomerService customerService,
            PriceConfigService priceConfigService,
            ProviderService providerService,
            PlatformService platformService,
            SourceItemService sourceItemService,
            @Lazy SalesOrderService salesOrderService,
            @Lazy ApprovalProcessService approvalProcessService) {
        this.productService = productService;
        this.customerService = customerService;
        this.priceConfigService = priceConfigService;
        this.providerService = providerService;
        this.platformService = platformService;
        this.sourceItemService = sourceItemService;
        this.salesOrderService = salesOrderService;
        this.approvalProcessService = approvalProcessService;
    }
    
    @Override
    public JSONObject recognizeVoucher(String imageUrl) {
        // TODO: 集成OCR服务进行图片识别
        // 这里提供模拟实现，实际应该调用OCR服务
        JSONObject result = new JSONObject();
        result.set("success", true);
        result.set("confidence", 0.95);
        result.set("customerName", "示例客户名称");
        result.set("productName", "示例商品名称");
        result.set("quantity", 100);
        result.set("netPrice", 1000.00);
        result.set("totalAmount", 100000.00);
        result.set("message", "OCR功能需要集成第三方服务");
        return result;
    }
    
    @Override
    public JSONObject validateSource(SourceEntity entity) {
        JSONObject result = new JSONObject();
        result.set("valid", true);
        StringBuilder errors = new StringBuilder();
        
        // 1. 校验客户是否存在
        if (ObjUtil.isNotEmpty(entity.getCustomerId())) {
            CustomerEntity customer = customerService.getById(entity.getCustomerId());
            if (customer == null) {
                errors.append("客户不存在，请联系管理员添加；");
                result.set("valid", false);
            }
        } else if (ObjUtil.isNotEmpty(entity.getCustomerName())) {
            // 根据客户名称查找
            CustomerEntity customer = customerService.getByName(entity.getCustomerName());
            if (customer == null) {
                errors.append("客户【").append(entity.getCustomerName()).append("】不存在，请联系管理员添加；");
                result.set("valid", false);
            } else {
                entity.setCustomerId(customer.getId());
            }
        }
        
        // 2. 校验商品是否存在
        if (ObjUtil.isNotEmpty(entity.getProductSku())) {
            ProductEntity product = productService.getBySku(entity.getProductSku());
            if (product == null) {
                errors.append("商品SKU【").append(entity.getProductSku()).append("】不存在；");
                result.set("valid", false);
            }
        }
        
        // 3. 校验价格计算是否正确：国网价 × 数量 = 总金额
        if (entity.getNetPrice() != null && entity.getQuantity() != null && entity.getTotalAmount() != null) {
            BigDecimal calculated = entity.getNetPrice().multiply(new BigDecimal(entity.getQuantity()));
            if (calculated.compareTo(entity.getTotalAmount()) != 0) {
                errors.append("金额计算错误：国网价(").append(entity.getNetPrice())
                      .append(") × 数量(").append(entity.getQuantity())
                      .append(") ≠ 总金额(").append(entity.getTotalAmount()).append(")；");
                result.set("valid", false);
            }
        }
        
        // 4. 设置校验结果
        entity.setValidateStatus(result.getBool("valid") ? 1 : 2);
        entity.setValidateError(errors.toString());
        result.set("errors", errors.toString());
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewSource(Long id, boolean passed, String reason) {
        SourceEntity entity = getById(id);
        CoolPreconditions.check(entity == null, "报备单不存在");
        CoolPreconditions.check(entity.getStatus() != 0, "报备单状态错误，无法审核");
        
        // 设置审核信息
        entity.setStatus(passed ? 1 : 2);
        entity.setReviewerId(CoolSecurityUtil.getCurrentUserId());
        entity.setReviewerName(CoolSecurityUtil.getAdminUsername());
        entity.setReviewTime(LocalDateTime.now());
        
        if (!passed) {
            CoolPreconditions.check(ObjUtil.isEmpty(reason), "驳回必须填写原因");
            entity.setRejectReason(reason);
        }
        
        return updateById(entity);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateSalesOrder(Long sourceId, JSONObject params) {
        log.info("开始从服务商报备生成销售订单，sourceId: {}", sourceId);
        
        // 调用销售订单服务生成订单
        SalesOrderEntity salesOrder = salesOrderService.generateFromReport(sourceId, params);
        
        CoolPreconditions.check(salesOrder == null, "销售订单生成失败");
        
        log.info("服务商报备生成销售订单成功，sourceId: {}, salesOrderId: {}, salesOrderNo: {}", 
            sourceId, salesOrder.getId(), salesOrder.getOrderNo());
        
        return salesOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateBatchSalesOrder(List<Long> sourceIds, JSONObject params) {
        log.info("开始批量从服务商报备生成销售订单，sourceIds: {}", sourceIds);
        
        // 调用销售订单服务生成订单
        SalesOrderEntity salesOrder = salesOrderService.generateFromReports(sourceIds, params);
        
        CoolPreconditions.check(salesOrder == null, "销售订单生成失败");
        
        log.info("批量服务商报备生成销售订单成功，sourceIds: {}, salesOrderId: {}, salesOrderNo: {}", 
            sourceIds, salesOrder.getId(), salesOrder.getOrderNo());
        
        return salesOrder.getId();
    }
    
    @Override
    public SourceEntity getBySourceNo(String sourceNo) {
        return getOne(QueryWrapper.create()
            .where(SOURCE_ENTITY.SOURCE_NO.eq(sourceNo)));
    }
    
    @Override
    public Object info(Long id) {
        // 查询主表数据
        SourceEntity entity = getById(id);
        if (entity != null) {
            // 查询关联的商品明细
            List<SourceItemEntity> items = sourceItemService.listBySourceId(id);
            entity.setItems(items);
        }
        return entity;
    }
    
    @Override
    public Object page(JSONObject requestParams, Page<SourceEntity> page, QueryWrapper queryWrapper) {
        // 先调用父类的分页查询
        Object result = super.page(requestParams, page, queryWrapper);
        
        // 聚合商品信息到主表字段
        enrichSourceListWithItems(result);
        
        return result;
    }
    
    @Override
    public Object list(JSONObject requestParams, QueryWrapper queryWrapper) {
        // 先调用父类的列表查询
        Object result = super.list(requestParams, queryWrapper);
        
        // 聚合商品信息到主表字段
        enrichSourceListWithItems(result);
        
        return result;
    }
    
    /**
     * 为报备单列表聚合商品信息
     * 从子表查询每个报备单的第一个商品信息，填充到主表的brand、productName、quantity字段
     */
    private void enrichSourceListWithItems(Object result) {
        if (result == null) {
            return;
        }
        
        List<SourceEntity> sourceList = null;
        
        // 判断result类型，提取数据列表
        if (result instanceof com.mybatisflex.core.paginate.Page) {
            com.mybatisflex.core.paginate.Page<?> page = (com.mybatisflex.core.paginate.Page<?>) result;
            if (!page.getRecords().isEmpty() && page.getRecords().get(0) instanceof SourceEntity) {
                sourceList = (List<SourceEntity>) page.getRecords();
            }
        } else if (result instanceof List) {
            List<?> list = (List<?>) result;
            if (!list.isEmpty() && list.get(0) instanceof SourceEntity) {
                sourceList = (List<SourceEntity>) list;
            }
        }
        
        if (sourceList == null || sourceList.isEmpty()) {
            return;
        }
        
        // 批量查询所有报备单的商品明细（一次性查询，优化性能）
        List<Long> sourceIds = sourceList.stream()
            .map(SourceEntity::getId)
            .toList();
        
        // 查询所有商品明细
        List<SourceItemEntity> allItems = sourceItemService.list(
            QueryWrapper.create()
                .where(com.cool.modules.source.entity.table.SourceItemEntityTableDef.SOURCE_ITEM_ENTITY.SOURCE_ID.in(sourceIds))
                .orderBy(com.cool.modules.source.entity.table.SourceItemEntityTableDef.SOURCE_ITEM_ENTITY.SOURCE_ID, true)
                .orderBy(com.cool.modules.source.entity.table.SourceItemEntityTableDef.SOURCE_ITEM_ENTITY.ID, true)
        );
        
        // 按sourceId分组
        java.util.Map<Long, List<SourceItemEntity>> itemsMap = allItems.stream()
            .collect(java.util.stream.Collectors.groupingBy(SourceItemEntity::getSourceId));
        
        // 为每个报备单填充第一个商品的信息
        for (SourceEntity source : sourceList) {
            List<SourceItemEntity> items = itemsMap.get(source.getId());
            if (items != null && !items.isEmpty()) {
                SourceItemEntity firstItem = items.get(0);
                
                // 填充商品信息到主表字段
                source.setBrand(firstItem.getBrand());
                source.setProductName(firstItem.getProductName());
                source.setQuantity(firstItem.getQuantity());
                
                // 如果有多个商品，在商品名称后追加数量提示
                if (items.size() > 1) {
                    source.setProductName(firstItem.getProductName() + " 等" + items.size() + "个商品");
                    // 数量显示为总数量
                    int totalQuantity = items.stream()
                        .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                        .sum();
                    source.setQuantity(totalQuantity);
                }
            }
        }
    }
    
    @Override
    public void modifyBefore(JSONObject requestParams, SourceEntity entity, ModifyEnum type) {
        if (type == ModifyEnum.ADD) {
            // 生成报备编号：SR + 年月日 + 序号
            String dateStr = DateUtil.format(LocalDateTime.now(), "yyyyMMdd");
            String sourceNo = "SR" + dateStr + String.format("%04d", 
                count(QueryWrapper.create()
                    .where(SOURCE_ENTITY.SOURCE_NO.like("SR" + dateStr + "%"))) + 1);
            entity.setSourceNo(sourceNo);
            
            // 设置初始状态
            if (entity.getStatus() == null) {
                entity.setStatus(0); // 待审核
            }
        }
        
        // 新增或修改时填充关联信息
        if (type == ModifyEnum.ADD || type == ModifyEnum.UPDATE) {
            // 校验服务商
            if (entity.getProviderId() == null) {
                throw new CoolException("请选择服务商");
            }
            // 填充服务商信息
            ProviderEntity provider = providerService.getById(entity.getProviderId());
            if (provider != null) {
                entity.setProviderName(provider.getProviderName());
                entity.setIsRegional(provider.getIsRegional());
            }
            
            // 填充客户信息
            if (entity.getCustomerId() != null) {
                CustomerEntity customer = customerService.getById(entity.getCustomerId());
                if (customer != null) {
                    entity.setCustomerName(customer.getCustomerName());
                }
            } else if (ObjUtil.isNotEmpty(entity.getCustomerName())) {
                CustomerEntity customer = customerService.getByName(entity.getCustomerName());
                if (customer != null) {
                    entity.setCustomerId(customer.getId());
                }
            }
            
            // 填充平台信息
            if (entity.getPlatformId() != null) {
                PlatformEntity platform = platformService.getById(entity.getPlatformId());
                if (platform != null) {
                    entity.setPlatformName(platform.getPlatformName());
                }
            }
        }
        
        // 更新时校验ID
        if (type == ModifyEnum.UPDATE) {
            CoolPreconditions.check(entity.getId() == null, "更新操作必须提供记录ID");
            
            // 如果当前是审核驳回状态，且用户进行了修改提交，则重置为待审核
            SourceEntity oldEntity = getById(entity.getId());
            if (oldEntity != null && oldEntity.getStatus() == 2) {
                entity.setStatus(0); // 重置为待审核
                entity.setRejectReason(""); // 清空驳回原因
            }
        }
        
        // 新增或修改时进行校验
        if (type == ModifyEnum.ADD || type == ModifyEnum.UPDATE) {
            // 自动校验报备信息
            JSONObject validateResult = validateSource(entity);
            if (!validateResult.getBool("valid")) {
                // 如果校验失败，记录错误信息但允许保存（后续可以修改）
                System.out.println("报备单校验失败：" + validateResult.getStr("errors"));
            }
        }
    }
    
    @Override
    public void modifyAfter(JSONObject requestParams, SourceEntity entity, ModifyEnum type) {
        // 新增后处理逻辑
        if (type == ModifyEnum.ADD) {
            // 保存商品明细
            if (entity.getItems() != null && !entity.getItems().isEmpty()) {
                sourceItemService.batchSave(entity.getId(), entity.getItems());
                log.info("报备单创建成功，编号：{}，明细数量：{}", entity.getSourceNo(), entity.getItems().size());
            } else {
                log.info("报备单创建成功，编号：{}", entity.getSourceNo());
            }
            
            // 启动审批流程
            try {
                Long applicantId = CoolSecurityUtil.getCurrentUserId();
                String applicantName = CoolSecurityUtil.getAdminUsername();
                
                boolean started = approvalProcessService.startApproval(
                    3, // instanceType: 3-服务商报备
                    entity.getId(),
                    entity.getSourceNo(),
                    applicantId,
                    applicantName
                );
                
                if (started) {
                    log.info("服务商报备审批流程启动成功，报备单ID: {}, 报备单号: {}", entity.getId(), entity.getSourceNo());
                } else {
                    log.warn("服务商报备审批流程启动失败（可能未配置审批流），报备单ID: {}, 报备单号: {}", entity.getId(), entity.getSourceNo());
                }
            } catch (Exception e) {
                // 审批流程启动失败不影响业务数据保存，只记录日志
                log.error("启动服务商报备审批流程异常，报备单ID: {}, 报备单号: {}", entity.getId(), entity.getSourceNo(), e);
            }
        }
        
        // 更新后处理逻辑
        if (type == ModifyEnum.UPDATE) {
            // 删除旧的商品明细
            if (entity.getItems() != null) {
                // 先删除原有的明细
                sourceItemService.remove(QueryWrapper.create()
                    .where(com.cool.modules.source.entity.table.SourceItemEntityTableDef.SOURCE_ITEM_ENTITY.SOURCE_ID.eq(entity.getId())));
                
                // 重新保存新的明细
                if (!entity.getItems().isEmpty()) {
                    sourceItemService.batchSave(entity.getId(), entity.getItems());
                    System.out.println("报备单更新成功，ID：" + entity.getId() + "，明细数量：" + entity.getItems().size());
                }
            }
            
            // 如果订单状态被重置为待审核（0），说明是驳回后重新提交，需要重新启动审批流程
            if (entity.getStatus() != null && entity.getStatus() == 0) {
                // 检查是否已经有待审批的记录，避免重复创建
                List<ApprovalRecordEntity> pendingRecords = approvalProcessService.getApprovalProgress(3, entity.getId());
                boolean hasPending = pendingRecords.stream().anyMatch(r -> r.getStatus() == 0);
                
                if (!hasPending) {
                    log.info("服务商报备重新提交，重启审批流程，报备单ID: {}", entity.getId());
                    try {
                        Long applicantId = CoolSecurityUtil.getCurrentUserId();
                        String applicantName = CoolSecurityUtil.getAdminUsername();
                        
                        approvalProcessService.startApproval(
                            3, // instanceType: 3-服务商报备
                            entity.getId(),
                            entity.getSourceNo(),
                            applicantId,
                            applicantName
                        );
                    } catch (Exception e) {
                        log.error("重启服务商报备审批流程异常，报备单ID: {}", entity.getId(), e);
                    }
                }
            }
        }
    }
}

