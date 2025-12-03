package com.cool.modules.purchase.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.purchase.entity.PurchasePaymentEntity;
import com.cool.modules.purchase.service.PurchasePaymentService;
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
import static com.cool.modules.purchase.entity.table.PurchasePaymentEntityTableDef.PURCHASE_PAYMENT_ENTITY;

/**
 * 采购付款管理Controller
 */
@Tag(name = "采购付款管理", description = "采购付款管理")
@CoolRestController(
    value = "/admin/purchase/payment",
    api = {"add", "delete", "update", "page", "list", "info"}
)
@RequiredArgsConstructor
public class AdminPurchasePaymentController extends BaseController<PurchasePaymentService, PurchasePaymentEntity> {

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 分页查询配置
        setPageOption(createOp()
            .fieldEq(PURCHASE_PAYMENT_ENTITY.ORDER_ID, PURCHASE_PAYMENT_ENTITY.STATUS)
            .select(
                PURCHASE_PAYMENT_ENTITY.ID, PURCHASE_PAYMENT_ENTITY.ORDER_ID,
                PURCHASE_PAYMENT_ENTITY.PAYMENT_NO, PURCHASE_PAYMENT_ENTITY.PAYMENT_DATE,
                PURCHASE_PAYMENT_ENTITY.AMOUNT, PURCHASE_PAYMENT_ENTITY.PAYMENT_METHOD,
                PURCHASE_PAYMENT_ENTITY.PAYMENT_ACCOUNT, PURCHASE_PAYMENT_ENTITY.TRANSACTION_NO,
                PURCHASE_PAYMENT_ENTITY.VOUCHER_URL, PURCHASE_PAYMENT_ENTITY.STATUS,
                PURCHASE_PAYMENT_ENTITY.REMARK, PURCHASE_PAYMENT_ENTITY.CREATE_TIME
            )
        );

        // 列表查询配置
        setListOption(createOp()
            .fieldEq(PURCHASE_PAYMENT_ENTITY.ORDER_ID)
        );
    }

    @Operation(summary = "根据订单ID获取付款列表", description = "根据订单ID获取付款列表")
    @GetMapping("/listByOrderId")
    public Object listByOrderId(@Parameter(description = "订单ID") @RequestParam Long orderId) {
        List<PurchasePaymentEntity> list = service.listByOrderId(orderId);
        return ok(list);
    }

    @Operation(summary = "删除付款（需填写原因）", description = "删除付款信息，需填写删除原因")
    @PostMapping("/deleteWithReason")
    public Object deleteWithReason(@RequestBody JSONObject params) {
        Long id = params.getLong("id");
        String reason = params.getStr("reason");

        CoolPreconditions.check(id == null, "付款ID不能为空");
        CoolPreconditions.check(reason == null || reason.trim().isEmpty(), "删除原因不能为空");

        service.deleteWithReason(id, reason);
        return ok("删除成功");
    }
}
