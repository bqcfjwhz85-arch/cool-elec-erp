package com.cool.modules.wechat.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.enums.Apis;
import com.cool.modules.wechat.entity.WeChatUser;
import com.cool.modules.wechat.service.WeChatUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 微信用户信息表
 */
@Tag(name = "微信用户信息表", description = "微信用户信息表")
@CoolRestController(api = {Apis.ADD, Apis.DELETE, Apis.UPDATE, Apis.PAGE, Apis.LIST, Apis.INFO})
public class AdminWeChatUserController extends BaseController<WeChatUserService, WeChatUser> {
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {

    }
}