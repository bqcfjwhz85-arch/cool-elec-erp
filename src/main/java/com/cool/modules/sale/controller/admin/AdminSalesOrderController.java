package com.cool.modules.sale.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.sale.entity.SalesOrderEntity;
import com.cool.modules.sale.service.SalesOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import static com.cool.core.request.R.ok;
import static com.cool.modules.sale.entity.table.SalesOrderEntityTableDef.SALES_ORDER_ENTITY;
import static com.cool.modules.sale.entity.table.SalesOrderItemEntityTableDef.SALES_ORDER_ITEM_ENTITY;
import static com.mybatisflex.core.query.QueryMethods.groupConcat;
import static com.mybatisflex.core.query.QueryMethods.select;
import com.mybatisflex.core.query.SelectQueryColumn;

/**
 * 销售订单管理Controller
 */
@Tag(name = "销售订单管理", description = "销售订单管理")
@CoolRestController(
    value = "/admin/sale/order",
    api = {"add", "delete", "update", "page", "list", "info"}
)
@RequiredArgsConstructor
public class AdminSalesOrderController extends BaseController<SalesOrderService, SalesOrderEntity> {
    
    private final com.cool.modules.config.service.ContractPrintService contractPrintService;
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 分页查询配置
        setPageOption(createOp()
            .fieldEq(SALES_ORDER_ENTITY.SOURCE_TYPE, SALES_ORDER_ENTITY.ORDER_TYPE, 
                    SALES_ORDER_ENTITY.STATUS, SALES_ORDER_ENTITY.APPROVAL_STATUS, 
                    SALES_ORDER_ENTITY.DELIVERY_STATUS, SALES_ORDER_ENTITY.INVOICE_STATUS, 
                    SALES_ORDER_ENTITY.PAYMENT_STATUS, SALES_ORDER_ENTITY.FLOW_STATUS, 
                    SALES_ORDER_ENTITY.CUSTOMER_ID, SALES_ORDER_ENTITY.PROVIDER_ID, 
                    SALES_ORDER_ENTITY.PLATFORM_ID)
            .keyWordLikeFields(SALES_ORDER_ENTITY.ORDER_NO, SALES_ORDER_ENTITY.CUSTOMER_NAME, 
                              SALES_ORDER_ENTITY.PROVIDER_NAME, SALES_ORDER_ENTITY.PLATFORM_NAME, 
                              SALES_ORDER_ENTITY.PLATFORM_ORDER_NO, SALES_ORDER_ENTITY.REPORT_NO)
            .select(
                SALES_ORDER_ENTITY.ID, SALES_ORDER_ENTITY.ORDER_NO, SALES_ORDER_ENTITY.SOURCE_TYPE, 
                SALES_ORDER_ENTITY.ORDER_TYPE, SALES_ORDER_ENTITY.IS_REGIONAL,
                SALES_ORDER_ENTITY.CUSTOMER_ID, SALES_ORDER_ENTITY.CUSTOMER_NAME, 
                SALES_ORDER_ENTITY.PROVIDER_ID, SALES_ORDER_ENTITY.PROVIDER_NAME,
                SALES_ORDER_ENTITY.PLATFORM_ID, SALES_ORDER_ENTITY.PLATFORM_NAME, 
                SALES_ORDER_ENTITY.PLATFORM_ORDER_NO, SALES_ORDER_ENTITY.REPORT_NO,
                SALES_ORDER_ENTITY.TOTAL_AMOUNT, SALES_ORDER_ENTITY.TOTAL_QUANTITY, 
                SALES_ORDER_ENTITY.POINTS,
                SALES_ORDER_ENTITY.STATUS, SALES_ORDER_ENTITY.APPROVAL_STATUS, 
                SALES_ORDER_ENTITY.DELIVERY_STATUS, SALES_ORDER_ENTITY.INVOICE_STATUS, 
                SALES_ORDER_ENTITY.PAYMENT_STATUS, SALES_ORDER_ENTITY.FLOW_STATUS,
                SALES_ORDER_ENTITY.DELIVERED_QUANTITY, SALES_ORDER_ENTITY.INVOICED_AMOUNT, 
                SALES_ORDER_ENTITY.PAID_AMOUNT,
                SALES_ORDER_ENTITY.CREATE_TIME, SALES_ORDER_ENTITY.UPDATE_TIME, 
                SALES_ORDER_ENTITY.FLOW_TIME, SALES_ORDER_ENTITY.INVOICE_TYPE,
                SALES_ORDER_ENTITY.SOURCE_ID,
                // 子查询获取报备商品名称
                new SelectQueryColumn(
                    select(groupConcat(SALES_ORDER_ITEM_ENTITY.PRODUCT_NAME))
                        .from(SALES_ORDER_ITEM_ENTITY)
                        .where(SALES_ORDER_ITEM_ENTITY.ORDER_ID.eq(SALES_ORDER_ENTITY.ID))
                ).as("productNames")
            )
        );
        
        // 列表查询配置
        setListOption(createOp()
            .fieldEq(SALES_ORDER_ENTITY.STATUS, SALES_ORDER_ENTITY.ORDER_TYPE)
        );
    }
    
    @Operation(summary = "根据订单号获取订单", description = "根据订单号获取订单详情")
    @GetMapping("/getByOrderNo")
    public Object getByOrderNo(@Parameter(description = "订单号") @RequestParam String orderNo) {
        SalesOrderEntity order = service.getByOrderNo(orderNo);
        CoolPreconditions.check(order == null, "订单不存在");
        return ok(order);
    }
    
    @Operation(summary = "生成订单号", description = "生成新的订单号")
    @GetMapping("/generateOrderNo")
    public Object generateOrderNo() {
        String orderNo = service.generateOrderNo();
        return ok(orderNo);
    }
    
    @Operation(summary = "从服务商报备生成订单", description = "从服务商报备生成销售订单")
    @PostMapping("/generateFromReport")
    public Object generateFromReport(@RequestBody JSONObject params) {
        Long reportId = params.getLong("reportId");
        CoolPreconditions.check(reportId == null, "报备ID不能为空");
        
        SalesOrderEntity order = service.generateFromReport(reportId, params);
        return ok(order);
    }
    
    @Operation(summary = "从平台订单生成订单", description = "从平台订单生成销售订单")
    @PostMapping("/generateFromPlatformOrder")
    public Object generateFromPlatformOrder(@RequestBody JSONObject params) {
        Long platformOrderId = params.getLong("platformOrderId");
        CoolPreconditions.check(platformOrderId == null, "平台订单ID不能为空");
        
        SalesOrderEntity order = service.generateFromPlatformOrder(platformOrderId);
        return ok(order);
    }
    
    @Operation(summary = "启动审批流程", description = "启动销售订单审批流程")
    @PostMapping("/startApproval")
    public Object startApproval(@RequestBody JSONObject params) {
        Long orderId = params.getLong("orderId");
        CoolPreconditions.check(orderId == null, "订单ID不能为空");
        
        service.startApproval(orderId);
        return ok("启动审批流程成功");
    }
    
    @Operation(summary = "流转订单", description = "将订单流转至财务处理")
    @PostMapping("/flowOrder")
    public Object flowOrder(@RequestBody JSONObject params) {
        Long orderId = params.getLong("orderId");
        CoolPreconditions.check(orderId == null, "订单ID不能为空");
        
        Long receiverId = params.getLong("receiverId");
        
        service.flowOrder(orderId, receiverId);
        return ok("订单流转成功");
    }
    
    @Operation(summary = "更新发货状态", description = "更新订单发货状态")
    @PostMapping("/updateDeliveryStatus")
    public Object updateDeliveryStatus(@RequestBody JSONObject params) {
        Long orderId = params.getLong("orderId");
        CoolPreconditions.check(orderId == null, "订单ID不能为空");
        
        service.updateDeliveryStatus(orderId);
        return ok("更新发货状态成功");
    }
    
    @Operation(summary = "更新开票状态", description = "更新订单开票状态")
    @PostMapping("/updateInvoiceStatus")
    public Object updateInvoiceStatus(@RequestBody JSONObject params) {
        Long orderId = params.getLong("orderId");
        CoolPreconditions.check(orderId == null, "订单ID不能为空");
        
        service.updateInvoiceStatus(orderId);
        return ok("更新开票状态成功");
    }
    
    @Operation(summary = "更新回款状态", description = "更新订单回款状态")
    @PostMapping("/updatePaymentStatus")
    public Object updatePaymentStatus(@RequestBody JSONObject params) {
        Long orderId = params.getLong("orderId");
        CoolPreconditions.check(orderId == null, "订单ID不能为空");
        
        service.updatePaymentStatus(orderId);
        return ok("更新回款状态成功");
    }
    
    @Operation(summary = "打印合同", description = "生成销售合同PDF")
    @GetMapping("/printContract")
    public void printContract(
            @Parameter(description = "订单ID") @RequestParam Long orderId,
            @Parameter(description = "模板ID(可选)") @RequestParam(required = false) Long templateId,
            jakarta.servlet.http.HttpServletResponse response) {
        try {
            CoolPreconditions.check(orderId == null, "订单ID不能为空");
            
            byte[] pdfBytes = contractPrintService.generateSalesContractPdf(orderId, templateId);
            
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=sales_contract_" + orderId + ".pdf");
            response.setContentLength(pdfBytes.length);
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new RuntimeException("生成合同PDF失败: " + e.getMessage(), e);
        }
    }
}


