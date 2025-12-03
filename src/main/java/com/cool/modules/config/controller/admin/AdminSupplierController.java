package com.cool.modules.config.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.config.entity.SupplierEntity;
import com.cool.modules.config.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 供应商管理控制器
 */
@Tag(name = "供应商管理", description = "供应商管理")
@CoolRestController(api = {"add", "delete", "update", "page", "list", "info"})
public class AdminSupplierController extends BaseController<SupplierService, SupplierEntity> {
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {

    }
    
    @Operation(summary = "获取供应商关联的商品")
    @PostMapping("/getGoods")
    public JSONObject getGoods(@RequestBody JSONObject params) {
        Long supplierId = params.getLong("supplierId");
        Integer page = params.getInt("page", 1);
        Integer size = params.getInt("size", 10);
        String keyword = params.getStr("keyword");
        return service.getGoods(supplierId, page, size, keyword);
    }
    
    @Operation(summary = "批量关联商品到供应商")
    @PostMapping("/addGoods")
    public void addGoods(@RequestBody JSONObject params) {
        Long supplierId = params.getLong("supplierId");
        List<Long> goodsIds = params.getBeanList("goodsIds", Long.class);
        service.addGoods(supplierId, goodsIds);
    }
    
    @Operation(summary = "移除供应商的商品关联")
    @PostMapping("/removeGoods")
    public void removeGoods(@RequestBody JSONObject params) {
        Long supplierId = params.getLong("supplierId");
        Long goodsId = params.getLong("goodsId");
        service.removeGoods(supplierId, goodsId);
    }
}
