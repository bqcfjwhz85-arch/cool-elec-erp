package com.cool.modules.config.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.request.R;
import com.cool.modules.config.entity.PriceConfigEntity;
import com.cool.modules.config.service.PriceConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

import static com.cool.core.request.R.ok;
import static com.cool.modules.config.entity.table.PriceConfigEntityTableDef.PRICE_CONFIG_ENTITY;

/**
 * 价格配置管理Controller
 */
@Tag(name = "价格配置管理", description = "价格配置管理")
@CoolRestController(
    value = "/admin/config/price",
    api = {"add", "delete", "update", "page", "list", "info"}
)
public class AdminPriceConfigController extends BaseController<PriceConfigService, PriceConfigEntity> {
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        setPageOption(createOp()
            .fieldEq(PRICE_CONFIG_ENTITY.PRODUCT_SKU, PRICE_CONFIG_ENTITY.PRICE_TYPE, 
                    PRICE_CONFIG_ENTITY.REGION_CODE, PRICE_CONFIG_ENTITY.PROVIDER_ID, 
                    PRICE_CONFIG_ENTITY.PLATFORM_ID, PRICE_CONFIG_ENTITY.PROVINCE,
                    PRICE_CONFIG_ENTITY.IS_DEFAULT, PRICE_CONFIG_ENTITY.STATUS)
            .keyWordLikeFields(PRICE_CONFIG_ENTITY.PRODUCT_SKU, PRICE_CONFIG_ENTITY.PRODUCT_NAME)
            .select(
                PRICE_CONFIG_ENTITY.ID, PRICE_CONFIG_ENTITY.PRODUCT_SKU, PRICE_CONFIG_ENTITY.PRODUCT_NAME, 
                PRICE_CONFIG_ENTITY.PRICE_TYPE, PRICE_CONFIG_ENTITY.PRICE,
                PRICE_CONFIG_ENTITY.STATE_GRID_PRICE, PRICE_CONFIG_ENTITY.REGIONAL_PRICE,
                PRICE_CONFIG_ENTITY.PROVIDER_PRICE, PRICE_CONFIG_ENTITY.SETTLEMENT_PRICE,
                PRICE_CONFIG_ENTITY.REGION_CODE, PRICE_CONFIG_ENTITY.REGION_NAME,
                PRICE_CONFIG_ENTITY.PLATFORM_ID, PRICE_CONFIG_ENTITY.PLATFORM_NAME,
                PRICE_CONFIG_ENTITY.PROVINCE, PRICE_CONFIG_ENTITY.IS_DEFAULT,
                PRICE_CONFIG_ENTITY.PROVIDER_ID, PRICE_CONFIG_ENTITY.PROVIDER_NAME,
                PRICE_CONFIG_ENTITY.EFFECTIVE_TIME, PRICE_CONFIG_ENTITY.EXPIRY_TIME, 
                PRICE_CONFIG_ENTITY.STATUS, PRICE_CONFIG_ENTITY.CREATE_TIME, PRICE_CONFIG_ENTITY.UPDATE_TIME,
                PRICE_CONFIG_ENTITY.REMARK
            )
        );
        
        setListOption(createOp()
            .fieldEq(PRICE_CONFIG_ENTITY.PRODUCT_SKU, PRICE_CONFIG_ENTITY.STATUS, 
                    PRICE_CONFIG_ENTITY.PRICE_TYPE, PRICE_CONFIG_ENTITY.REGION_CODE,
                    PRICE_CONFIG_ENTITY.PROVIDER_ID, PRICE_CONFIG_ENTITY.PLATFORM_ID,
                    PRICE_CONFIG_ENTITY.PROVINCE, PRICE_CONFIG_ENTITY.IS_DEFAULT)
        );
    }
    
    @Operation(summary = "批量调整价格", description = "按商品类别和区域批量调整价格")
    @PostMapping("/batchAdjust")
    public R<String> batchAdjust(@RequestBody JSONObject params) {
        String category = params.getStr("category");
        String regionCode = params.getStr("regionCode");
        BigDecimal adjustRate = params.getBigDecimal("adjustRate");
        
        service.batchAdjustPrice(category, regionCode, adjustRate);
        return ok("批量调整成功");
    }
}

