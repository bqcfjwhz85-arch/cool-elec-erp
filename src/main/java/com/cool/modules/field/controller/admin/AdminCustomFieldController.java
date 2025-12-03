
package com.cool.modules.field.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.field.entity.CustomFieldEntity;
import com.cool.modules.field.service.CustomFieldService;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 自定义字段
 */
@Tag(name = "自定义字段", description = "自定义字段")
@CoolRestController(api = {"add", "delete", "update", "page", "list", "info"})
public class AdminCustomFieldController extends BaseController <CustomFieldService, CustomFieldEntity> {
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 获取请求参数中的 menuId
        Long menuId = requestParams.getLong("menuId");
        if (menuId != null) {
            // 为 list 和 page 查询添加 menuId 过滤条件
            QueryWrapper queryWrapper = QueryWrapper.create().where("menu_id = " + menuId);
            setListOption(createOp().queryWrapper(queryWrapper));
            setPageOption(createOp().queryWrapper(queryWrapper));
        }
    }
}
