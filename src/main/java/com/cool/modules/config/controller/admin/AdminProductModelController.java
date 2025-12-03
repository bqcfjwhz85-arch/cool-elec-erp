package com.cool.modules.config.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.config.entity.ProductModelEntity;
import com.cool.modules.config.service.ProductModelService;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import static com.cool.modules.config.entity.table.ProductModelEntityTableDef.PRODUCT_MODEL_ENTITY;

/**
 * 商品型号管理Controller
 */
@Tag(name = "商品型号管理", description = "商品型号管理")
@CoolRestController(
    value = "/admin/config/productModel",
    api = {"add", "delete", "update", "page", "list", "info"}
)
public class AdminProductModelController extends BaseController<ProductModelService, ProductModelEntity> {
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        setPageOption(createOp()
            .fieldEq(PRODUCT_MODEL_ENTITY.BRAND_ID, PRODUCT_MODEL_ENTITY.STATUS, 
                    PRODUCT_MODEL_ENTITY.CATEGORY)
            .keyWordLikeFields(PRODUCT_MODEL_ENTITY.MODEL_CODE, PRODUCT_MODEL_ENTITY.MODEL_NAME, 
                              PRODUCT_MODEL_ENTITY.BRAND_NAME, PRODUCT_MODEL_ENTITY.SPECIFICATION)
            .select(
                PRODUCT_MODEL_ENTITY.ID, PRODUCT_MODEL_ENTITY.MODEL_CODE, 
                PRODUCT_MODEL_ENTITY.MODEL_NAME, PRODUCT_MODEL_ENTITY.BRAND_ID, 
                PRODUCT_MODEL_ENTITY.BRAND_NAME, PRODUCT_MODEL_ENTITY.CATEGORY, 
                PRODUCT_MODEL_ENTITY.SPECIFICATION, PRODUCT_MODEL_ENTITY.UNIT,
                PRODUCT_MODEL_ENTITY.POWER, PRODUCT_MODEL_ENTITY.VOLTAGE,
                PRODUCT_MODEL_ENTITY.STATUS, PRODUCT_MODEL_ENTITY.SORT_ORDER, 
                PRODUCT_MODEL_ENTITY.CREATE_TIME, PRODUCT_MODEL_ENTITY.REMARK
            )
        );
        
        setListOption(createOp()
            .fieldEq(PRODUCT_MODEL_ENTITY.BRAND_ID, PRODUCT_MODEL_ENTITY.STATUS, 
                    PRODUCT_MODEL_ENTITY.CATEGORY)
            .select(
                PRODUCT_MODEL_ENTITY.ID, PRODUCT_MODEL_ENTITY.MODEL_CODE, 
                PRODUCT_MODEL_ENTITY.MODEL_NAME, PRODUCT_MODEL_ENTITY.BRAND_ID, 
                PRODUCT_MODEL_ENTITY.BRAND_NAME, PRODUCT_MODEL_ENTITY.STATUS
            )
        );
    }
}




