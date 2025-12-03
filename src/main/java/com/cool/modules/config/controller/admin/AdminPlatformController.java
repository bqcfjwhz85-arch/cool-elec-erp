package com.cool.modules.config.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.config.entity.PlatformEntity;
import com.cool.modules.config.service.PlatformService;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 平台管理Controller
 */
@Tag(name = "平台管理", description = "平台管理")
@CoolRestController(
    value = "/admin/config/platform",
    api = {"add", "delete", "update", "page", "list", "info"}
)
public class AdminPlatformController extends BaseController<PlatformService, PlatformEntity> {
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 分页和列表查询配置
        // 使用默认配置，支持所有字段的查询和返回
    }
}

