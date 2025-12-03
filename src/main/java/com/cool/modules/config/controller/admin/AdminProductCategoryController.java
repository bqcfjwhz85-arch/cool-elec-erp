package com.cool.modules.config.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.config.entity.ProductCategoryEntity;
import com.cool.modules.config.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import static com.cool.modules.config.entity.table.ProductCategoryEntityTableDef.PRODUCT_CATEGORY_ENTITY;

/**
 * 商品类别管理Controller
 */
@Tag(name = "商品类别管理", description = "商品类别管理")
@CoolRestController(
    value = "/admin/config/productCategory",
    api = {"add", "delete", "update", "page", "list", "info"}
)
public class AdminProductCategoryController extends BaseController<ProductCategoryService, ProductCategoryEntity> {
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        setPageOption(createOp()
            .fieldEq(PRODUCT_CATEGORY_ENTITY.PARENT_ID, PRODUCT_CATEGORY_ENTITY.LEVEL, 
                    PRODUCT_CATEGORY_ENTITY.STATUS)
            .keyWordLikeFields(PRODUCT_CATEGORY_ENTITY.CATEGORY_CODE, 
                              PRODUCT_CATEGORY_ENTITY.CATEGORY_NAME,
                              PRODUCT_CATEGORY_ENTITY.PARENT_NAME)
            .select(
                PRODUCT_CATEGORY_ENTITY.ID, PRODUCT_CATEGORY_ENTITY.CATEGORY_CODE, 
                PRODUCT_CATEGORY_ENTITY.CATEGORY_NAME, PRODUCT_CATEGORY_ENTITY.PARENT_ID,
                PRODUCT_CATEGORY_ENTITY.PARENT_NAME, PRODUCT_CATEGORY_ENTITY.LEVEL,
                PRODUCT_CATEGORY_ENTITY.CATEGORY_ICON, PRODUCT_CATEGORY_ENTITY.STATUS, 
                PRODUCT_CATEGORY_ENTITY.SORT_ORDER, PRODUCT_CATEGORY_ENTITY.CREATE_TIME,
                PRODUCT_CATEGORY_ENTITY.REMARK
            )
        );
        
        setListOption(createOp()
            .fieldEq(PRODUCT_CATEGORY_ENTITY.PARENT_ID, PRODUCT_CATEGORY_ENTITY.LEVEL, 
                    PRODUCT_CATEGORY_ENTITY.STATUS)
            .select(
                PRODUCT_CATEGORY_ENTITY.ID, PRODUCT_CATEGORY_ENTITY.CATEGORY_CODE, 
                PRODUCT_CATEGORY_ENTITY.CATEGORY_NAME, PRODUCT_CATEGORY_ENTITY.PARENT_ID,
                PRODUCT_CATEGORY_ENTITY.LEVEL, PRODUCT_CATEGORY_ENTITY.STATUS
            )
        );
    }
}




