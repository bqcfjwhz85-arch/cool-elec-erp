package com.cool.modules.config.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.config.entity.ProviderEntity;
import com.cool.modules.config.service.ProviderService;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import static com.cool.modules.config.entity.table.ProviderEntityTableDef.PROVIDER_ENTITY;

/**
 * 服务商管理Controller
 */
@Tag(name = "服务商管理", description = "服务商管理")
@CoolRestController(
    value = "/admin/config/provider",
    api = {"add", "delete", "update", "page", "list", "info"}
)
public class AdminProviderController extends BaseController<ProviderService, ProviderEntity> {
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        setPageOption(createOp()
            .fieldEq(PROVIDER_ENTITY.IS_REGIONAL, PROVIDER_ENTITY.STATUS)
            .keyWordLikeFields(
                PROVIDER_ENTITY.PROVIDER_CODE, PROVIDER_ENTITY.PROVIDER_NAME, 
                PROVIDER_ENTITY.SHORT_NAME, PROVIDER_ENTITY.SOCIAL_CREDIT_CODE,
                PROVIDER_ENTITY.CONTACT_PERSON, PROVIDER_ENTITY.CONTACT_PHONE
            )
            .select(
                // 基础信息
                PROVIDER_ENTITY.ID, PROVIDER_ENTITY.PROVIDER_CODE, PROVIDER_ENTITY.PROVIDER_NAME, 
                PROVIDER_ENTITY.SHORT_NAME, PROVIDER_ENTITY.SOCIAL_CREDIT_CODE,
                PROVIDER_ENTITY.LEGAL_REPRESENTATIVE, PROVIDER_ENTITY.REGISTERED_ADDRESS,
                // 联系信息
                PROVIDER_ENTITY.CONTACT_PERSON, PROVIDER_ENTITY.CONTACT_PHONE, 
                PROVIDER_ENTITY.CONTACT_EMAIL, PROVIDER_ENTITY.CONTACT_ADDRESS,
                // 银行信息
                PROVIDER_ENTITY.BANK_NAME, PROVIDER_ENTITY.BANK_ACCOUNT,
                // 业务配置
                PROVIDER_ENTITY.IS_REGIONAL, PROVIDER_ENTITY.DEFAULT_POINT,
                PROVIDER_ENTITY.REGION_SCOPE, PROVIDER_ENTITY.ALLOWED_REGIONS, 
                PROVIDER_ENTITY.ALLOWED_PRODUCTS,
                // 系统字段
                PROVIDER_ENTITY.USER_ID, PROVIDER_ENTITY.STATUS, 
                PROVIDER_ENTITY.CREATE_TIME, PROVIDER_ENTITY.UPDATE_TIME, PROVIDER_ENTITY.REMARK,
                PROVIDER_ENTITY.CUSTOM_DATA
            )
        );
        
        setListOption(createOp()
            .fieldEq(PROVIDER_ENTITY.STATUS)
            .select(
                PROVIDER_ENTITY.ID, PROVIDER_ENTITY.PROVIDER_CODE, 
                PROVIDER_ENTITY.PROVIDER_NAME, PROVIDER_ENTITY.SHORT_NAME,
                PROVIDER_ENTITY.IS_REGIONAL, PROVIDER_ENTITY.STATUS,
                PROVIDER_ENTITY.CONTACT_PERSON, PROVIDER_ENTITY.CONTACT_PHONE,
                PROVIDER_ENTITY.CONTACT_ADDRESS,
                PROVIDER_ENTITY.DELIVERY_PROVINCE, PROVIDER_ENTITY.DELIVERY_CITY, PROVIDER_ENTITY.DELIVERY_DISTRICT,
                PROVIDER_ENTITY.BANK_NAME, PROVIDER_ENTITY.BANK_ACCOUNT,
                PROVIDER_ENTITY.REGION_SCOPE, PROVIDER_ENTITY.ALLOWED_REGIONS
            )
        );
    }
}

