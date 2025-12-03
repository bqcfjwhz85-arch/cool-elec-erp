package com.cool.modules.purchase.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.purchase.entity.PurchaseInvoiceEntity;
import com.cool.modules.purchase.service.PurchaseInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static com.cool.core.request.R.ok;
import static com.cool.modules.purchase.entity.table.PurchaseInvoiceEntityTableDef.PURCHASE_INVOICE_ENTITY;

/**
 * 采购发票管理Controller
 */
@Tag(name = "采购发票管理", description = "采购发票管理")
@CoolRestController(
    value = "/admin/purchase/invoice",
    api = {"add", "delete", "update", "page", "list", "info"}
)
@RequiredArgsConstructor
public class AdminPurchaseInvoiceController extends BaseController<PurchaseInvoiceService, PurchaseInvoiceEntity> {

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 分页查询配置
        setPageOption(createOp()
            .fieldEq(PURCHASE_INVOICE_ENTITY.ORDER_ID, PURCHASE_INVOICE_ENTITY.STATUS)
            .keyWordLikeFields(PURCHASE_INVOICE_ENTITY.INVOICE_NO)
            .select(
                PURCHASE_INVOICE_ENTITY.ID, PURCHASE_INVOICE_ENTITY.ORDER_ID,
                PURCHASE_INVOICE_ENTITY.INVOICE_NO, PURCHASE_INVOICE_ENTITY.INVOICE_TYPE,
                PURCHASE_INVOICE_ENTITY.INVOICE_DATE, PURCHASE_INVOICE_ENTITY.AMOUNT,
                PURCHASE_INVOICE_ENTITY.TAX_AMOUNT, PURCHASE_INVOICE_ENTITY.TOTAL_AMOUNT,
                PURCHASE_INVOICE_ENTITY.SELLER_NAME, PURCHASE_INVOICE_ENTITY.SELLER_TAX_NO,
                PURCHASE_INVOICE_ENTITY.SCAN_FILE_URL, PURCHASE_INVOICE_ENTITY.STATUS,
                PURCHASE_INVOICE_ENTITY.REMARK, PURCHASE_INVOICE_ENTITY.CREATE_TIME
            )
        );

        // 列表查询配置
        setListOption(createOp()
            .fieldEq(PURCHASE_INVOICE_ENTITY.ORDER_ID)
        );
    }

    @Operation(summary = "根据订单ID获取发票列表", description = "根据订单ID获取发票列表")
    @GetMapping("/listByOrderId")
    public Object listByOrderId(@Parameter(description = "订单ID") @RequestParam Long orderId) {
        List<PurchaseInvoiceEntity> list = service.listByOrderId(orderId);
        return ok(list);
    }

    @Operation(summary = "删除发票（需填写原因）", description = "删除发票信息，需填写删除原因")
    @PostMapping("/deleteWithReason")
    public Object deleteWithReason(@RequestBody JSONObject params) {
        Long id = params.getLong("id");
        String reason = params.getStr("reason");

        CoolPreconditions.check(id == null, "发票ID不能为空");
        CoolPreconditions.check(reason == null || reason.trim().isEmpty(), "删除原因不能为空");

        service.deleteWithReason(id, reason);
        return ok("删除成功");
    }

    @Operation(summary = "变更发票状态", description = "变更发票状态（0-待开票 1-已开票 2-已作废）")
    @PostMapping("/changeStatus")
    public Object changeStatus(@RequestBody JSONObject params) {
        Long id = params.getLong("id");
        Integer status = params.getInt("status");

        CoolPreconditions.check(id == null, "发票ID不能为空");
        CoolPreconditions.check(status == null, "状态不能为空");

        service.changeStatus(id, status);
        return ok("状态变更成功");
    }
}
