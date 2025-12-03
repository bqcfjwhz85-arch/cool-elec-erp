package com.cool.modules.purchase.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.exception.CoolPreconditions;
import com.cool.core.request.CrudOption;
import com.cool.modules.purchase.entity.PurchaseOrderEntity;
import com.cool.modules.purchase.service.PurchaseOrderService;
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
import static com.cool.modules.purchase.entity.table.PurchaseOrderEntityTableDef.PURCHASE_ORDER_ENTITY;
import static com.cool.modules.purchase.entity.table.PurchaseOrderItemEntityTableDef.PURCHASE_ORDER_ITEM_ENTITY;
import static com.mybatisflex.core.query.QueryMethods.groupConcat;
import static com.mybatisflex.core.query.QueryMethods.select;
import com.mybatisflex.core.query.SelectQueryColumn;

/**
 * 采购订单管理Controller
 */
@Tag(name = "采购订单管理", description = "采购订单管理")
@CoolRestController(
    value = "/admin/purchase/order",
    api = {"add", "delete", "update", "page", "list", "info"}
)
@RequiredArgsConstructor
public class AdminPurchaseOrderController extends BaseController<PurchaseOrderService, PurchaseOrderEntity> {
    
    private final PurchaseOrderService purchaseOrderService;
    private final com.cool.modules.purchase.service.PurchaseOrderItemService purchaseOrderItemService;
    private final com.cool.modules.purchase.service.PurchaseInvoiceService purchaseInvoiceService;
    private final com.cool.modules.purchase.service.PurchasePaymentService purchasePaymentService;
    private final com.cool.modules.config.service.ContractPrintService contractPrintService;
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 分页查询配置
        setPageOption(createOp()
            .fieldEq(PURCHASE_ORDER_ENTITY.SOURCE_TYPE, PURCHASE_ORDER_ENTITY.ORDER_TYPE, 
                    PURCHASE_ORDER_ENTITY.STATUS, PURCHASE_ORDER_ENTITY.APPROVAL_STATUS, 
                    PURCHASE_ORDER_ENTITY.PAYMENT_STATUS, PURCHASE_ORDER_ENTITY.INVOICE_STATUS,
                    PURCHASE_ORDER_ENTITY.SUPPLIER_ID, PURCHASE_ORDER_ENTITY.SALES_ORDER_ID)
            .keyWordLikeFields(PURCHASE_ORDER_ENTITY.ORDER_NO, PURCHASE_ORDER_ENTITY.SALES_ORDER_NO,
                              PURCHASE_ORDER_ENTITY.SUPPLIER_NAME)
            .select(
                PURCHASE_ORDER_ENTITY.ID, PURCHASE_ORDER_ENTITY.ORDER_NO, 
                PURCHASE_ORDER_ENTITY.SOURCE_TYPE, PURCHASE_ORDER_ENTITY.SALES_ORDER_NO,
                PURCHASE_ORDER_ENTITY.SUPPLIER_NAME, PURCHASE_ORDER_ENTITY.ORDER_TYPE,
                PURCHASE_ORDER_ENTITY.TOTAL_AMOUNT, PURCHASE_ORDER_ENTITY.SALES_AMOUNT, 
                PURCHASE_ORDER_ENTITY.PROFIT,
                PURCHASE_ORDER_ENTITY.STATUS, PURCHASE_ORDER_ENTITY.PAYMENT_STATUS, 
                PURCHASE_ORDER_ENTITY.INVOICE_STATUS, PURCHASE_ORDER_ENTITY.APPROVAL_STATUS,
                PURCHASE_ORDER_ENTITY.REJECT_REASON,
                PURCHASE_ORDER_ENTITY.CREATE_TIME, PURCHASE_ORDER_ENTITY.CREATE_BY,
                // 子查询获取报备商品名称
                new SelectQueryColumn(
                    select(groupConcat(PURCHASE_ORDER_ITEM_ENTITY.PRODUCT_NAME))
                        .from(PURCHASE_ORDER_ITEM_ENTITY)
                        .where(PURCHASE_ORDER_ITEM_ENTITY.ORDER_ID.eq(PURCHASE_ORDER_ENTITY.ID))
                ).as("productNames")
            )
        );
        
        // 列表查询配置
        setListOption(createOp()
            .fieldEq(PURCHASE_ORDER_ENTITY.STATUS, PURCHASE_ORDER_ENTITY.SUPPLIER_ID)
        );
        
        // Info查询配置 - 自动加载关联数据
        setInfoOption(createOp()
            .transformValue((obj) -> {
                if (obj instanceof PurchaseOrderEntity) {
                    PurchaseOrderEntity entity = (PurchaseOrderEntity) obj;
                    if (entity.getId() != null) {
                        // 加载商品明细
                        entity.setItems(purchaseOrderItemService.listByOrderId(entity.getId()));
                        // 加载发票信息
                        entity.setInvoices(purchaseInvoiceService.listByOrderId(entity.getId()));
                        // 加载付款信息
                        entity.setPayments(purchasePaymentService.listByOrderId(entity.getId()));
                    }
                }
            })
        );
    }
    
    @Operation(summary = "根据订单号获取订单", description = "根据订单号获取订单详情")
    @GetMapping("/getByOrderNo")
    public Object getByOrderNo(@Parameter(description = "订单号") @RequestParam String orderNo) {
        PurchaseOrderEntity order = service.getByOrderNo(orderNo);
        CoolPreconditions.check(order == null, "订单不存在");
        return ok(order);
    }
    
    @Operation(summary = "生成订单号", description = "生成新的订单号")
    @GetMapping("/generateOrderNo")
    public Object generateOrderNo() {
        String orderNo = service.generateOrderNo();
        return ok(orderNo);
    }
    
    @Operation(summary = "从销售订单生成采购订单", description = "一键生成或手动创建采购订单")
    @PostMapping("/generateFromSalesOrder")
    public Object generateFromSalesOrder(@RequestBody JSONObject params) {
        Long salesOrderId = params.getLong("salesOrderId");
        CoolPreconditions.check(salesOrderId == null, "销售订单ID不能为空");
        
        PurchaseOrderEntity order = service.generateFromSalesOrder(salesOrderId, params);
        return ok(order);
    }
    
    @Operation(summary = "更新发票状态", description = "更新订单发票状态")
    @PostMapping("/updateInvoiceStatus")
    public Object updateInvoiceStatus(@RequestBody JSONObject params) {
        Long orderId = params.getLong("orderId");
        CoolPreconditions.check(orderId == null, "订单ID不能为空");
        
        service.updateInvoiceStatus(orderId);
        return ok("更新发票状态成功");
    }
    
    @Operation(summary = "更新付款状态", description = "更新订单付款状态")
    @PostMapping("/updatePaymentStatus")
    public Object updatePaymentStatus(@RequestBody JSONObject params) {
        Long orderId = params.getLong("orderId");
        CoolPreconditions.check(orderId == null, "订单ID不能为空");
        
        service.updatePaymentStatus(orderId);
        return ok("更新付款状态成功");
    }
    
    @Operation(summary = "获取订单详情（包含关联数据）", description = "获取订单详情，包含商品明细、发票和付款信息")
    @GetMapping("/detail")
    public Object detail(@RequestParam Long id) {
        CoolPreconditions.check(id == null, "订单ID不能为空");
        
        PurchaseOrderEntity order = purchaseOrderService.getById(id);
        CoolPreconditions.check(order == null, "订单不存在");
        
        // 加载商品明细
        order.setItems(purchaseOrderItemService.listByOrderId(id));
        
        // 加载发票信息
        order.setInvoices(purchaseInvoiceService.listByOrderId(id));
        
        // 加载付款信息
        order.setPayments(purchasePaymentService.listByOrderId(id));
        
        return ok(order);
    }
    
    @Operation(summary = "打印合同", description = "生成采购合同PDF")
    @GetMapping("/printContract")
    public void printContract(
            @Parameter(description = "订单ID") @RequestParam Long orderId,
            @Parameter(description = "模板ID(可选)") @RequestParam(required = false) Long templateId,
            jakarta.servlet.http.HttpServletResponse response) {
        try {
            CoolPreconditions.check(orderId == null, "订单ID不能为空");
            
            byte[] pdfBytes = contractPrintService.generatePurchaseContractPdf(orderId, templateId);
            
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=purchase_contract_" + orderId + ".pdf");
            response.setContentLength(pdfBytes.length);
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new RuntimeException("生成合同PDF失败: " + e.getMessage(), e);
        }
    }
}
