package com.cool.modules.source.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.request.R;
import com.cool.modules.source.entity.PlatformOrderEntity;
import com.cool.modules.source.service.PlatformOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import static com.cool.modules.source.entity.table.PlatformOrderEntityTableDef.PLATFORM_ORDER_ENTITY;

/**
 * 平台订单Controller
 */
@Tag(name = "平台订单管理", description = "平台订单管理")
@CoolRestController(
    value = "/admin/source/platformOrder",
    api = {"add", "delete", "update", "page", "list", "info"}
)
public class AdminPlatformOrderController extends BaseController<PlatformOrderService, PlatformOrderEntity> {
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 分页查询配置
        setPageOption(createOp()
            .fieldEq(PLATFORM_ORDER_ENTITY.STATUS, PLATFORM_ORDER_ENTITY.PLATFORM_ID, 
                    PLATFORM_ORDER_ENTITY.PROVIDER_ID)
            .keyWordLikeFields(PLATFORM_ORDER_ENTITY.PLATFORM_ORDER_NO, 
                              PLATFORM_ORDER_ENTITY.PLATFORM_NO,
                              PLATFORM_ORDER_ENTITY.PLATFORM_NAME, 
                              PLATFORM_ORDER_ENTITY.PROVIDER_NAME,
                              PLATFORM_ORDER_ENTITY.CONSIGNEE)
            .select(
                PLATFORM_ORDER_ENTITY.ID, PLATFORM_ORDER_ENTITY.PLATFORM_ORDER_NO,
                PLATFORM_ORDER_ENTITY.PLATFORM_NO, PLATFORM_ORDER_ENTITY.PLATFORM_NAME,
                PLATFORM_ORDER_ENTITY.PROVIDER_ID, PLATFORM_ORDER_ENTITY.PROVIDER_NAME, 
                PLATFORM_ORDER_ENTITY.PROVIDER_REGION, PLATFORM_ORDER_ENTITY.CONSIGNEE,
                PLATFORM_ORDER_ENTITY.CONTACT_PHONE, PLATFORM_ORDER_ENTITY.TOTAL_AMOUNT,
                PLATFORM_ORDER_ENTITY.PLATFORM_POINTS, PLATFORM_ORDER_ENTITY.INVOICE_TYPE,
                PLATFORM_ORDER_ENTITY.STATUS, PLATFORM_ORDER_ENTITY.SALES_ORDER_NO, 
                PLATFORM_ORDER_ENTITY.CREATE_TIME, PLATFORM_ORDER_ENTITY.REMARK
            )
        );
        
        // 列表查询配置
        setListOption(createOp()
            .fieldEq(PLATFORM_ORDER_ENTITY.STATUS, PLATFORM_ORDER_ENTITY.PLATFORM_ID)
        );
    }
    
    @Operation(summary = "生成销售订单", description = "从平台订单生成销售订单")
    @PostMapping("/generateSalesOrder")
    public R generateSalesOrder(@RequestBody JSONObject params) {
        Long platformOrderId = params.getLong("platformOrderId");
        Long salesOrderId = service.generateSalesOrder(platformOrderId, params);
        
        JSONObject result = new JSONObject();
        result.set("salesOrderId", salesOrderId);
        return R.ok(result);
    }
    
    @Operation(summary = "批量导入平台订单", description = "通过Excel批量导入平台订单")
    @PostMapping("/batchImport")
    public R batchImport(@RequestParam("file") MultipartFile file) {
        // TODO: 解析Excel文件并导入
        JSONObject result = service.batchImport(new JSONObject());
        return R.ok(result);
    }
}

