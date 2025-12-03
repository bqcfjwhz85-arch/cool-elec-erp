package com.cool.modules.config.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.config.entity.PointConfigEntity;
import com.cool.modules.config.service.PointConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import static com.cool.modules.config.entity.table.PointConfigEntityTableDef.POINT_CONFIG_ENTITY;

/**
 * 点位配置管理Controller
 */
@Tag(name = "点位配置管理", description = "点位配置管理")
@CoolRestController(
    value = "/admin/config/point",
    api = {"add", "delete", "update", "page", "list", "info"}
)
public class AdminPointConfigController extends BaseController<PointConfigService, PointConfigEntity> {
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        setPageOption(createOp()
            .fieldEq(POINT_CONFIG_ENTITY.CONFIG_TYPE, POINT_CONFIG_ENTITY.PLATFORM_ID, 
                    POINT_CONFIG_ENTITY.PROVIDER_ID, POINT_CONFIG_ENTITY.STATUS)
            .keyWordLikeFields(POINT_CONFIG_ENTITY.PLATFORM_NAME, POINT_CONFIG_ENTITY.PROVIDER_NAME, 
                              POINT_CONFIG_ENTITY.BRAND, POINT_CONFIG_ENTITY.MODEL)
            .select(
                POINT_CONFIG_ENTITY.ID, POINT_CONFIG_ENTITY.CONFIG_TYPE, POINT_CONFIG_ENTITY.PLATFORM_ID, 
                POINT_CONFIG_ENTITY.PLATFORM_NAME, POINT_CONFIG_ENTITY.REGION_CODE, POINT_CONFIG_ENTITY.REGION_NAME, 
                POINT_CONFIG_ENTITY.BRAND, POINT_CONFIG_ENTITY.MODEL, POINT_CONFIG_ENTITY.PROVIDER_ID, 
                POINT_CONFIG_ENTITY.PROVIDER_NAME, POINT_CONFIG_ENTITY.CATEGORY_NAME, POINT_CONFIG_ENTITY.POINTS, 
                POINT_CONFIG_ENTITY.PRIORITY, POINT_CONFIG_ENTITY.STATUS, POINT_CONFIG_ENTITY.CREATE_TIME, 
                POINT_CONFIG_ENTITY.REMARK, POINT_CONFIG_ENTITY.CUSTOM_DATA
            )
        );
        
        setListOption(createOp()
            .fieldEq(POINT_CONFIG_ENTITY.STATUS, POINT_CONFIG_ENTITY.CONFIG_TYPE)
        );
    }
}

