package com.cool.modules.config.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.config.entity.BrandEntity;
import com.cool.modules.config.service.BrandService;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import static com.cool.modules.config.entity.table.BrandEntityTableDef.BRAND_ENTITY;

/**
 * 品牌管理Controller
 */
@Tag(name = "品牌管理", description = "品牌管理")
@CoolRestController(
    value = "/admin/config/brand",
    api = {"add", "delete", "update", "page", "list", "info"}
)
public class AdminBrandController extends BaseController<BrandService, BrandEntity> {
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        setPageOption(createOp()
            .fieldEq(BRAND_ENTITY.STATUS)
            .keyWordLikeFields(BRAND_ENTITY.BRAND_CODE, BRAND_ENTITY.BRAND_NAME, 
                              BRAND_ENTITY.BRAND_NAME_EN, BRAND_ENTITY.MANUFACTURER)
            .select(
                BRAND_ENTITY.ID, BRAND_ENTITY.BRAND_CODE, BRAND_ENTITY.BRAND_NAME, 
                BRAND_ENTITY.BRAND_NAME_EN, BRAND_ENTITY.BRAND_LOGO, 
                BRAND_ENTITY.MANUFACTURER, BRAND_ENTITY.COUNTRY, BRAND_ENTITY.CATEGORY,
                BRAND_ENTITY.STATUS, BRAND_ENTITY.SORT_ORDER, 
                BRAND_ENTITY.CREATE_TIME, BRAND_ENTITY.REMARK
            )
        );
        
        setListOption(createOp()
            .fieldEq(BRAND_ENTITY.STATUS)
            .select(
                BRAND_ENTITY.ID, BRAND_ENTITY.BRAND_CODE, BRAND_ENTITY.BRAND_NAME, 
                BRAND_ENTITY.BRAND_NAME_EN, BRAND_ENTITY.STATUS
            )
        );
    }
}




