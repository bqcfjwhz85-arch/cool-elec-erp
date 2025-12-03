package com.cool.modules.config.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.config.entity.ContractTemplateEntity;
import com.cool.modules.config.service.ContractTemplateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 合同模板管理
 */
@Tag(name = "合同模板管理", description = "合同模板管理")
@CoolRestController(
    value = "/admin/config/contract",
    api = {"add", "delete", "update", "page", "list", "info"}
)
public class AdminContractTemplateController extends BaseController<ContractTemplateService, ContractTemplateEntity> {

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 可以在这里配置查询过滤等选项
    }
}
