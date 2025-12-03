package com.cool.modules.sale.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.exception.CoolPreconditions;
import com.cool.core.request.R;
import com.cool.modules.sale.entity.SalesDeliveryEntity;
import com.cool.modules.sale.service.SalesDeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.cool.core.request.R.ok;
import static com.cool.modules.sale.entity.table.SalesDeliveryEntityTableDef.SALES_DELIVERY_ENTITY;

/**
 * 发货信息管理Controller
 */
@Tag(name = "发货信息管理", description = "销售订单发货信息管理")
@CoolRestController(
    value = "/admin/sale/delivery",
    api = {"add", "delete", "update", "page", "list", "info"}
)
@RequiredArgsConstructor
public class AdminSalesDeliveryController extends BaseController<SalesDeliveryService, SalesDeliveryEntity> {
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 分页查询配置
        setPageOption(createOp()
            .fieldEq(SALES_DELIVERY_ENTITY.ORDER_ID, SALES_DELIVERY_ENTITY.STATUS)
            .keyWordLikeFields(SALES_DELIVERY_ENTITY.DELIVERY_NO, SALES_DELIVERY_ENTITY.TRACKING_NO, 
                              SALES_DELIVERY_ENTITY.LOGISTICS_COMPANY)
            .select(
                SALES_DELIVERY_ENTITY.ID, SALES_DELIVERY_ENTITY.ORDER_ID, 
                SALES_DELIVERY_ENTITY.DELIVERY_NO, SALES_DELIVERY_ENTITY.LOGISTICS_COMPANY, 
                SALES_DELIVERY_ENTITY.TRACKING_NO,
                SALES_DELIVERY_ENTITY.QUANTITY, SALES_DELIVERY_ENTITY.DELIVERY_TIME, 
                SALES_DELIVERY_ENTITY.DELIVERY_BY, SALES_DELIVERY_ENTITY.DELIVERY_ADDRESS,
                SALES_DELIVERY_ENTITY.DELIVERY_CONTACT, SALES_DELIVERY_ENTITY.DELIVERY_PHONE, 
                SALES_DELIVERY_ENTITY.STATUS, SALES_DELIVERY_ENTITY.REMARK,
                SALES_DELIVERY_ENTITY.CREATE_TIME, SALES_DELIVERY_ENTITY.UPDATE_TIME
            )
        );
        
        // 列表查询配置
        setListOption(createOp()
            .fieldEq(SALES_DELIVERY_ENTITY.ORDER_ID, SALES_DELIVERY_ENTITY.STATUS)
        );
    }
    
    @Override
    @PostMapping("/add")
    public R add(@RequestBody JSONObject params) {
        // 提取并移除details参数
        Object details = params.remove("details");
        
        // 调用service的自定义保存方法
        Object result = service.addWithDetails(params.toBean(SalesDeliveryEntity.class), details);
        return ok(result);
    }
    
    @Override
    @PostMapping("/update")
    public R update(@RequestBody SalesDeliveryEntity entity, @RequestAttribute JSONObject requestParams) {
        // 提取details参数
        Object details = requestParams.get("details");
        
        // 调用service的自定义更新方法
        Object result = service.updateWithDetails(entity, details);
        return ok(result);
    }
    
    @Operation(summary = "获取发货详情(含明细)", description = "获取发货信息及商品明细")
    @GetMapping("/infoWithDetails")
    public R infoWithDetails(@RequestParam Long id) {
        Map<String, Object> result = service.getInfoWithDetails(id);
        return ok(result);
    }
    
    @Operation(summary = "根据订单ID获取发货信息", description = "根据订单ID获取发货信息列表")
    @GetMapping("/listByOrderId")
    public R listByOrderId(@RequestParam Long orderId) {
        List<SalesDeliveryEntity> list = service.listByOrderId(orderId);
        return ok(list);
    }
    
    @Operation(summary = "删除发货信息", description = "删除发货信息（需要填写原因）")
    @PostMapping("/deleteWithReason")
    public Object deleteWithReason(@RequestBody JSONObject params) {
        Long id = params.getLong("id");
        String reason = params.getStr("reason");
        
        CoolPreconditions.check(id == null, "ID不能为空");
        
        service.deleteWithReason(id, reason);
        return ok("删除成功");
    }
}


