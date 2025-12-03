package com.cool.modules.sale.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.sale.entity.SalesInvoiceEntity;
import com.cool.modules.sale.service.SalesInvoiceService;
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
import static com.cool.modules.sale.entity.table.SalesInvoiceEntityTableDef.SALES_INVOICE_ENTITY;

/**
 * 销售发票信息管理Controller
 */
@Tag(name = "销售发票管理", description = "销售发票信息管理")
@CoolRestController(
    value = "/admin/sale/invoice",
    api = {"add", "delete", "update", "page", "list", "info"}
)
@RequiredArgsConstructor
public class AdminSalesInvoiceController extends BaseController<SalesInvoiceService, SalesInvoiceEntity> {
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 分页查询配置
        setPageOption(createOp()
            .fieldEq(SALES_INVOICE_ENTITY.ORDER_ID, SALES_INVOICE_ENTITY.INVOICE_TYPE, 
                    SALES_INVOICE_ENTITY.STATUS)
            .keyWordLikeFields(SALES_INVOICE_ENTITY.INVOICE_NO, SALES_INVOICE_ENTITY.BUYER_NAME, 
                              SALES_INVOICE_ENTITY.BUYER_TAX_NO)
            .select(
                SALES_INVOICE_ENTITY.ID, SALES_INVOICE_ENTITY.ORDER_ID, 
                SALES_INVOICE_ENTITY.INVOICE_NO, SALES_INVOICE_ENTITY.INVOICE_TYPE, 
                SALES_INVOICE_ENTITY.INVOICE_DATE,
                SALES_INVOICE_ENTITY.AMOUNT, SALES_INVOICE_ENTITY.TAX_AMOUNT, 
                SALES_INVOICE_ENTITY.TOTAL_AMOUNT,
                SALES_INVOICE_ENTITY.BUYER_NAME, SALES_INVOICE_ENTITY.BUYER_TAX_NO, 
                SALES_INVOICE_ENTITY.SCAN_FILE_URL,
                SALES_INVOICE_ENTITY.STATUS, SALES_INVOICE_ENTITY.REMARK, 
                SALES_INVOICE_ENTITY.CREATE_TIME, SALES_INVOICE_ENTITY.UPDATE_TIME
            )
        );
        
        // 列表查询配置
        setListOption(createOp()
            .fieldEq(SALES_INVOICE_ENTITY.ORDER_ID, SALES_INVOICE_ENTITY.STATUS)
        );
    }
    
    @Operation(summary = "根据订单ID获取发票信息", description = "根据订单ID获取发票信息列表")
    @GetMapping("/listByOrderId")
    public Object listByOrderId(@RequestParam Long orderId) {
        List<SalesInvoiceEntity> list = service.listByOrderId(orderId);
        return ok(list);
    }
    
    @Operation(summary = "删除发票信息", description = "删除发票信息（需要填写原因）")
    @PostMapping("/deleteWithReason")
    public Object deleteWithReason(@RequestBody JSONObject params) {
        Long id = params.getLong("id");
        String reason = params.getStr("reason");
        
        CoolPreconditions.check(id == null, "ID不能为空");
        
        service.deleteWithReason(id, reason);
        return ok("删除成功");
    }
}

