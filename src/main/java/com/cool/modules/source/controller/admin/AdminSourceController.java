package com.cool.modules.source.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.request.R;
import com.cool.modules.source.entity.SourceEntity;
import com.cool.modules.source.service.SourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static com.cool.modules.source.entity.table.SourceEntityTableDef.SOURCE_ENTITY;
import static com.cool.modules.source.entity.table.SourceItemEntityTableDef.SOURCE_ITEM_ENTITY;
import static com.mybatisflex.core.query.QueryMethods.groupConcat;
import static com.mybatisflex.core.query.QueryMethods.select;
import com.mybatisflex.core.query.SelectQueryColumn;

/**
 * 订单来源报备Controller
 */
@Tag(name = "订单来源管理", description = "订单来源管理（服务商报备）")
@CoolRestController(
    value = "/admin/source/order",
    api = {"add", "delete", "update", "page", "list", "info"}
)
public class AdminSourceController extends BaseController<SourceService, SourceEntity> {
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 分页查询配置
        setPageOption(createOp()
            .fieldEq(SOURCE_ENTITY.STATUS, SOURCE_ENTITY.ORDER_MODE, 
                    SOURCE_ENTITY.PROVIDER_ID, SOURCE_ENTITY.PLATFORM_ID, 
                    SOURCE_ENTITY.IS_REGIONAL, SOURCE_ENTITY.VALIDATE_STATUS)
            .keyWordLikeFields(SOURCE_ENTITY.SOURCE_NO, SOURCE_ENTITY.PROVIDER_NAME, 
                              SOURCE_ENTITY.CUSTOMER_NAME, SOURCE_ENTITY.PRODUCT_NAME,
                              SOURCE_ENTITY.PR_NO, SOURCE_ENTITY.ERP_NO, SOURCE_ENTITY.SALES_ORDER_NO)
            .select(
                SOURCE_ENTITY.ID, SOURCE_ENTITY.SOURCE_NO, SOURCE_ENTITY.PROVIDER_NAME,
                SOURCE_ENTITY.PR_NO, SOURCE_ENTITY.ERP_NO, SOURCE_ENTITY.ORDER_TIME,
                SOURCE_ENTITY.PRODUCT_NAME, SOURCE_ENTITY.PRODUCT_SKU,
                SOURCE_ENTITY.QUANTITY, SOURCE_ENTITY.NET_PRICE, SOURCE_ENTITY.TOTAL_AMOUNT,
                SOURCE_ENTITY.PLATFORM_NAME, SOURCE_ENTITY.ORDER_MODE, SOURCE_ENTITY.CUSTOMER_NAME,
                SOURCE_ENTITY.IS_REGIONAL, SOURCE_ENTITY.REGION_CODE, SOURCE_ENTITY.STATUS,
                SOURCE_ENTITY.VALIDATE_STATUS, SOURCE_ENTITY.REVIEWER_NAME, SOURCE_ENTITY.REVIEW_TIME,
                SOURCE_ENTITY.CREATE_TIME, SOURCE_ENTITY.REMARK, SOURCE_ENTITY.SALES_ORDER_NO,
                // 子查询获取报备商品名称
                new SelectQueryColumn(
                    select(groupConcat(SOURCE_ITEM_ENTITY.PRODUCT_NAME))
                        .from(SOURCE_ITEM_ENTITY)
                        .where(SOURCE_ITEM_ENTITY.SOURCE_ID.eq(SOURCE_ENTITY.ID))
                ).as("productNames")
            )
        );
        
        // 列表查询配置
        setListOption(createOp()
            .fieldEq(SOURCE_ENTITY.STATUS, SOURCE_ENTITY.PROVIDER_ID)
        );
    }
    
    @Operation(summary = "OCR识别订单凭证", description = "上传订单凭证图片进行OCR识别")
    @PostMapping("/recognizeVoucher")
    public R recognizeVoucher(@RequestBody JSONObject params) {
        String imageUrl = params.getStr("imageUrl");
        JSONObject result = service.recognizeVoucher(imageUrl);
        return R.ok(result);
    }
    
    @Operation(summary = "校验报备信息", description = "校验客户存在性、商品存在性、价格计算正确性")
    @PostMapping("/validate")
    public R validate(@RequestBody SourceEntity entity) {
        JSONObject result = service.validateSource(entity);
        return R.ok(result);
    }
    
    @Operation(summary = "审核报备单", description = "审核服务商报备单，支持通过或驳回")
    @PostMapping("/review")
    public R review(@RequestBody JSONObject params) {
        Long id = params.getLong("id");
        Boolean passed = params.getBool("passed");
        String reason = params.getStr("reason");
        
        boolean success = service.reviewSource(id, passed, reason);
        return success ? R.ok("审核成功") : R.error("审核失败");
    }
    
    @Operation(summary = "生成销售订单", description = "从报备单生成销售订单")
    @PostMapping("/generateSalesOrder")
    public R generateSalesOrder(@RequestBody JSONObject params) {
        Long sourceId = params.getLong("sourceId");
        Long salesOrderId = service.generateSalesOrder(sourceId, params);
        
        JSONObject result = new JSONObject();
        result.set("salesOrderId", salesOrderId);
        return R.ok(result);
    }

    @Operation(summary = "批量生成销售订单", description = "批量从报备单生成销售订单")
    @PostMapping("/generateBatchSalesOrder")
    public R generateBatchSalesOrder(@RequestBody JSONObject params) {
        List<Long> ids = params.getBeanList("ids", Long.class);
        Long salesOrderId = service.generateBatchSalesOrder(ids, params);
        
        JSONObject result = new JSONObject();
        result.set("salesOrderId", salesOrderId);
        return R.ok(result);
    }
}

