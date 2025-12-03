package com.cool.modules.config.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.cool.core.exception.CoolPreconditions;
import com.cool.core.util.PdfUtil;
import com.cool.modules.config.entity.ContractTemplateEntity;
import com.cool.modules.config.service.ContractPrintService;
import com.cool.modules.config.service.ContractTemplateService;
import com.cool.modules.purchase.entity.PurchaseOrderEntity;
import com.cool.modules.purchase.entity.PurchaseOrderItemEntity;
import com.cool.modules.purchase.service.PurchaseOrderItemService;
import com.cool.modules.purchase.service.PurchaseOrderService;
import com.cool.modules.sale.entity.SalesOrderEntity;
import com.cool.modules.sale.entity.SalesOrderItemEntity;
import com.cool.modules.sale.service.SalesOrderItemService;
import com.cool.modules.sale.service.SalesOrderService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.cool.modules.config.entity.table.ContractTemplateEntityTableDef.CONTRACT_TEMPLATE_ENTITY;

/**
 * 合同打印服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractPrintServiceImpl implements ContractPrintService {

    private final ContractTemplateService contractTemplateService;
    private final SalesOrderService salesOrderService;
    private final SalesOrderItemService salesOrderItemService;
    private final PurchaseOrderService purchaseOrderService;
    private final PurchaseOrderItemService purchaseOrderItemService;

    @Override
    public byte[] generateSalesContractPdf(Long orderId, Long templateId) {
        // 1. 获取订单信息
        SalesOrderEntity order = salesOrderService.getById(orderId);
        CoolPreconditions.check(order == null, "订单不存在");

        // 2. 获取订单明细
        List<SalesOrderItemEntity> items = salesOrderItemService.listByOrderId(orderId);

        // 3. 获取合同模板
        ContractTemplateEntity template = getTemplate(templateId, 1, order.getOrderType());

        // 4. 填充模板变量
        String html = fillSalesContractTemplate(template.getTemplateContent(), order, items);

        // 5. 转换为PDF
        return PdfUtil.htmlToPdf(PdfUtil.wrapHtml(html));
    }

    @Override
    public byte[] generatePurchaseContractPdf(Long orderId, Long templateId) {
        // 1. 获取订单信息
        PurchaseOrderEntity order = purchaseOrderService.getById(orderId);
        CoolPreconditions.check(order == null, "订单不存在");

        // 2. 获取订单明细
        List<PurchaseOrderItemEntity> items = purchaseOrderItemService.listByOrderId(orderId);

        // 3. 获取合同模板
        ContractTemplateEntity template = getTemplate(templateId, 2, order.getOrderType());

        // 4. 填充模板变量
        String html = fillPurchaseContractTemplate(template.getTemplateContent(), order, items);

        // 5. 转换为PDF
        return PdfUtil.htmlToPdf(PdfUtil.wrapHtml(html));
    }

    /**
     * 获取合同模板
     *
     * @param templateId 模板ID
     * @param templateType 模板类型 1-销售 2-采购
     * @param orderType 订单类型
     * @return 合同模板
     */
    private ContractTemplateEntity getTemplate(Long templateId, Integer templateType, Integer orderType) {
        if (templateId != null) {
            ContractTemplateEntity template = contractTemplateService.getById(templateId);
            CoolPreconditions.check(template == null, "合同模板不存在");
            return template;
        }

        // 查找默认模板：优先按订单类型匹配，其次通用模板
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(CONTRACT_TEMPLATE_ENTITY.TEMPLATE_TYPE.eq(templateType))
                .and(CONTRACT_TEMPLATE_ENTITY.ENABLED.eq(1))
                .and(CONTRACT_TEMPLATE_ENTITY.APPLICABLE_TYPE.eq(orderType)
                        .or(CONTRACT_TEMPLATE_ENTITY.APPLICABLE_TYPE.eq(3)))
                .orderBy(CONTRACT_TEMPLATE_ENTITY.APPLICABLE_TYPE.asc())
                .orderBy(CONTRACT_TEMPLATE_ENTITY.IS_DEFAULT.desc())
                .limit(1);

        ContractTemplateEntity template = contractTemplateService.getOne(queryWrapper);
        CoolPreconditions.check(template == null, "未找到可用的合同模板");
        return template;
    }

    /**
     * 填充销售合同模板
     */
    private String fillSalesContractTemplate(String template, SalesOrderEntity order, List<SalesOrderItemEntity> items) {
        String html = template;

        // 基本信息
        html = html.replace("{{orderNo}}", StrUtil.nullToDefault(order.getOrderNo(), ""));
        html = html.replace("{{contractNo}}", generateContractNo("SC", order.getId()));
        html = html.replace("{{orderType}}", getOrderTypeText(order.getOrderType()));
        html = html.replace("{{sourceType}}", getSourceTypeText(order.getSourceType()));
        html = html.replace("{{createTime}}", DateUtil.formatDateTime(order.getCreateTime()));
        html = html.replace("{{remark}}", StrUtil.nullToDefault(order.getRemark(), ""));

        // 客户信息
        html = html.replace("{{customerName}}", StrUtil.nullToDefault(order.getCustomerName(), ""));
        html = html.replace("{{customerContact}}", StrUtil.nullToDefault(order.getDeliveryContact(), ""));
        html = html.replace("{{customerPhone}}", StrUtil.nullToDefault(order.getDeliveryPhone(), ""));
        html = html.replace("{{deliveryAddress}}", StrUtil.nullToDefault(order.getDeliveryAddress(), ""));
        html = html.replace("{{deliveryContact}}", StrUtil.nullToDefault(order.getDeliveryContact(), ""));
        html = html.replace("{{deliveryPhone}}", StrUtil.nullToDefault(order.getDeliveryPhone(), ""));

        // 金额信息
        html = html.replace("{{totalAmount}}", formatAmount(order.getTotalAmount()));
        html = html.replace("{{totalQuantity}}", String.valueOf(order.getTotalQuantity() != null ? order.getTotalQuantity() : 0));
        html = html.replace("{{providerName}}", StrUtil.nullToDefault(order.getProviderName(), ""));
        html = html.replace("{{platformName}}", StrUtil.nullToDefault(order.getPlatformName(), ""));

        // 商品明细 - 处理循环
        html = fillItemsLoop(html, items);

        return html;
    }

    /**
     * 填充采购合同模板
     */
    private String fillPurchaseContractTemplate(String template, PurchaseOrderEntity order, List<PurchaseOrderItemEntity> items) {
        String html = template;

        // 基本信息
        html = html.replace("{{orderNo}}", StrUtil.nullToDefault(order.getOrderNo(), ""));
        html = html.replace("{{contractNo}}", generateContractNo("PC", order.getId()));
        html = html.replace("{{orderType}}", getOrderTypeText(order.getOrderType()));
        html = html.replace("{{sourceType}}", getSourceTypeText(order.getSourceType()));
        html = html.replace("{{createTime}}", DateUtil.formatDateTime(order.getCreateTime()));
        html = html.replace("{{remark}}", StrUtil.nullToDefault(order.getRemark(), ""));

        // 供应商信息
        html = html.replace("{{supplierName}}", StrUtil.nullToDefault(order.getSupplierName(), ""));
        html = html.replace("{{supplierContact}}", "");
        html = html.replace("{{supplierPhone}}", "");
        html = html.replace("{{supplierAddress}}", "");

        // 金额信息
        html = html.replace("{{totalAmount}}", formatAmount(order.getTotalAmount()));
        html = html.replace("{{totalQuantity}}", String.valueOf(order.getTotalQuantity() != null ? order.getTotalQuantity() : 0));
        html = html.replace("{{salesAmount}}", formatAmount(order.getSalesAmount()));
        html = html.replace("{{profit}}", formatAmount(order.getProfit()));

        // 商品明细 - 处理循环
        html = fillItemsLoop(html, items);

        return html;
    }

    /**
     * 填充商品明细循环
     */
    private String fillItemsLoop(String html, List<?> items) {
        // 查找 {{#each items}} ... {{/each}} 循环块
        String loopStart = "{{#each items}}";
        String loopEnd = "{{/each}}";

        int startIndex = html.indexOf(loopStart);
        int endIndex = html.indexOf(loopEnd);

        if (startIndex == -1 || endIndex == -1) {
            return html;
        }

        String beforeLoop = html.substring(0, startIndex);
        String loopTemplate = html.substring(startIndex + loopStart.length(), endIndex);
        String afterLoop = html.substring(endIndex + loopEnd.length());

        StringBuilder itemsHtml = new StringBuilder();
        int index = 1;

        for (Object item : items) {
            String itemHtml = loopTemplate;
            itemHtml = itemHtml.replace("{{@index}}", String.valueOf(index++));

            if (item instanceof SalesOrderItemEntity) {
                SalesOrderItemEntity salesItem = (SalesOrderItemEntity) item;
                itemHtml = itemHtml.replace("{{this.productSku}}", StrUtil.nullToDefault(salesItem.getProductSku(), ""));
                itemHtml = itemHtml.replace("{{this.productName}}", StrUtil.nullToDefault(salesItem.getProductName(), ""));
                itemHtml = itemHtml.replace("{{this.brand}}", StrUtil.nullToDefault(salesItem.getBrand(), ""));
                itemHtml = itemHtml.replace("{{this.specification}}", StrUtil.nullToDefault(salesItem.getSpecification(), ""));
                itemHtml = itemHtml.replace("{{this.unit}}", StrUtil.nullToDefault(salesItem.getUnit(), ""));
                itemHtml = itemHtml.replace("{{this.quantity}}", String.valueOf(salesItem.getQuantity()));
                itemHtml = itemHtml.replace("{{this.price}}", formatAmount(salesItem.getPrice()));
                itemHtml = itemHtml.replace("{{this.amount}}", formatAmount(salesItem.getAmount()));
            } else if (item instanceof PurchaseOrderItemEntity) {
                PurchaseOrderItemEntity purchaseItem = (PurchaseOrderItemEntity) item;
                itemHtml = itemHtml.replace("{{this.productSku}}", StrUtil.nullToDefault(purchaseItem.getProductSku(), ""));
                itemHtml = itemHtml.replace("{{this.productName}}", StrUtil.nullToDefault(purchaseItem.getProductName(), ""));
                itemHtml = itemHtml.replace("{{this.brand}}", StrUtil.nullToDefault(purchaseItem.getBrand(), ""));
                itemHtml = itemHtml.replace("{{this.specification}}", StrUtil.nullToDefault(purchaseItem.getSpecification(), ""));
                itemHtml = itemHtml.replace("{{this.unit}}", StrUtil.nullToDefault(purchaseItem.getUnit(), ""));
                itemHtml = itemHtml.replace("{{this.quantity}}", String.valueOf(purchaseItem.getQuantity()));
                itemHtml = itemHtml.replace("{{this.price}}", formatAmount(purchaseItem.getSettlementPrice()));
                itemHtml = itemHtml.replace("{{this.amount}}", formatAmount(purchaseItem.getAmount()));
            }

            itemsHtml.append(itemHtml);
        }

        return beforeLoop + itemsHtml + afterLoop;
    }

    /**
     * 生成合同编号
     */
    private String generateContractNo(String prefix, Long orderId) {
        String date = DateUtil.format(DateUtil.date(), "yyyyMM");
        return String.format("%s%s%06d", prefix, date, orderId % 1000000);
    }

    /**
     * 获取订单类型文本
     */
    private String getOrderTypeText(Integer orderType) {
        if (orderType == null) return "";
        switch (orderType) {
            case 1: return "借码";
            case 2: return "实供";
            default: return "";
        }
    }

    /**
     * 获取订单来源文本
     */
    private String getSourceTypeText(Integer sourceType) {
        if (sourceType == null) return "";
        switch (sourceType) {
            case 1: return "服务商报备";
            case 2: return "平台订单";
            case 3: return "手动创建";
            default: return "";
        }
    }

    /**
     * 格式化金额
     */
    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%.2f", amount);
    }
}
