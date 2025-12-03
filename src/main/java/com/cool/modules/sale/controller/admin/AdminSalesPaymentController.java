package com.cool.modules.sale.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.sale.entity.SalesPaymentEntity;
import com.cool.modules.sale.service.SalesPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static com.cool.core.request.R.ok;
import static com.cool.modules.sale.entity.table.SalesPaymentEntityTableDef.SALES_PAYMENT_ENTITY;

/**
 * 回款信息管理Controller
 */
@Tag(name = "回款信息管理", description = "销售订单回款信息管理")
@CoolRestController(
    value = "/admin/sale/payment",
    api = {"add", "delete", "update", "page", "list", "info"}
)
@RequiredArgsConstructor
public class AdminSalesPaymentController extends BaseController<SalesPaymentService, SalesPaymentEntity> {
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 分页查询配置
        setPageOption(createOp()
            .fieldEq(SALES_PAYMENT_ENTITY.ORDER_ID, SALES_PAYMENT_ENTITY.PAYMENT_METHOD, 
                    SALES_PAYMENT_ENTITY.STATUS)
            .keyWordLikeFields(SALES_PAYMENT_ENTITY.PAYMENT_NO, SALES_PAYMENT_ENTITY.PAYMENT_ACCOUNT, 
                              SALES_PAYMENT_ENTITY.TRANSACTION_NO)
            .select(
                SALES_PAYMENT_ENTITY.ID, SALES_PAYMENT_ENTITY.ORDER_ID, 
                SALES_PAYMENT_ENTITY.PAYMENT_NO, SALES_PAYMENT_ENTITY.PAYMENT_DATE, 
                SALES_PAYMENT_ENTITY.AMOUNT,
                SALES_PAYMENT_ENTITY.PAYMENT_METHOD, SALES_PAYMENT_ENTITY.PAYMENT_ACCOUNT, 
                SALES_PAYMENT_ENTITY.TRANSACTION_NO,
                SALES_PAYMENT_ENTITY.VOUCHER_URL, SALES_PAYMENT_ENTITY.STATUS, 
                SALES_PAYMENT_ENTITY.CONFIRM_BY, SALES_PAYMENT_ENTITY.CONFIRM_TIME,
                SALES_PAYMENT_ENTITY.REMARK, SALES_PAYMENT_ENTITY.CREATE_TIME, 
                SALES_PAYMENT_ENTITY.UPDATE_TIME
            )
        );
        
        // 列表查询配置
        setListOption(createOp()
            .fieldEq(SALES_PAYMENT_ENTITY.ORDER_ID, SALES_PAYMENT_ENTITY.STATUS)
        );
    }
    
    @Operation(summary = "根据订单ID获取回款信息", description = "根据订单ID获取回款信息列表")
    @GetMapping("/listByOrderId")
    public Object listByOrderId(@RequestParam Long orderId) {
        List<SalesPaymentEntity> list = service.listByOrderId(orderId);
        return ok(list);
    }
    
    @Operation(summary = "删除回款信息", description = "删除回款信息（需要填写原因）")
    @PostMapping("/deleteWithReason")
    public Object deleteWithReason(@RequestBody JSONObject params) {
        Long id = params.getLong("id");
        String reason = params.getStr("reason");
        
        CoolPreconditions.check(id == null, "ID不能为空");
        
        service.deleteWithReason(id, reason);
        return ok("删除成功");
    }

    @Operation(summary = "批量回款", description = "批量回款")
    @PostMapping("/batch")
    public Object batchPayment(@RequestBody JSONObject params) {
        List<Long> orderIds = params.getJSONArray("orderIds").toList(Long.class);
        Integer paymentMethod = params.getInt("paymentMethod");
        String paymentDate = params.getStr("paymentDate");
        String paymentAccount = params.getStr("paymentAccount");    // 新增：回款账户
        String transactionNo = params.getStr("transactionNo");      // 新增：交易流水号
        String voucherUrl = params.getStr("voucherUrl");            // 新增：回款凭证URL
        String remark = params.getStr("remark");
        Boolean confirm = params.getBool("confirm");                // 新增：是否批量确认

        service.batchPayment(orderIds, paymentMethod, paymentDate, 
                           paymentAccount, transactionNo, voucherUrl, remark, confirm);
        return ok("批量回款成功");
    }
}

