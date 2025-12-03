package com.cool.modules.product.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.config.entity.ProductEntity;
import com.cool.modules.product.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import static com.cool.modules.config.entity.table.ProductEntityTableDef.PRODUCT_ENTITY;

import cn.hutool.core.util.StrUtil;
import com.cool.core.request.CrudOption;
import com.cool.core.request.PageResult;
import com.cool.core.request.R;
import com.cool.modules.config.entity.PriceConfigEntity;
import com.cool.modules.config.service.PriceConfigService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 商品管理Controller
 */
@Tag(name = "商品管理", description = "商品管理")
@CoolRestController(
    value = "/admin/product/info",
    api = {"add", "delete", "update", "page", "list", "info"}
)
public class AdminProductController extends BaseController<ProductService, ProductEntity> {

    @Resource
    private PriceConfigService priceConfigService;
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        setPageOption(createOp()
            .fieldEq(PRODUCT_ENTITY.STATUS, PRODUCT_ENTITY.BRAND_ID, 
                    PRODUCT_ENTITY.MODEL_ID, PRODUCT_ENTITY.CATEGORY_ID,
                    PRODUCT_ENTITY.SUPPLIER_ID)
            .keyWordLikeFields(PRODUCT_ENTITY.PRODUCT_SKU, PRODUCT_ENTITY.PRODUCT_NAME, 
                    PRODUCT_ENTITY.BRAND, PRODUCT_ENTITY.MODEL)
            .select(
                PRODUCT_ENTITY.ID, PRODUCT_ENTITY.PRODUCT_SKU, PRODUCT_ENTITY.PRODUCT_NAME,
                // 新字段（推荐使用）
                PRODUCT_ENTITY.BRAND_ID, PRODUCT_ENTITY.MODEL_ID, PRODUCT_ENTITY.CATEGORY_ID,
                PRODUCT_ENTITY.SUPPLIER_ID,
                // 旧字段（向下兼容）
                PRODUCT_ENTITY.BRAND, PRODUCT_ENTITY.MODEL, PRODUCT_ENTITY.CATEGORY,
                // 其他字段
                PRODUCT_ENTITY.SPECIFICATION, PRODUCT_ENTITY.UNIT, PRODUCT_ENTITY.STATUS,
                PRODUCT_ENTITY.CREATE_TIME, PRODUCT_ENTITY.REMARK
            )
            .queryWrapper(QueryWrapper.create().orderBy(PRODUCT_ENTITY.CREATE_TIME.desc()))
        );
        
        setListOption(createOp()
            .fieldEq(PRODUCT_ENTITY.STATUS, PRODUCT_ENTITY.BRAND_ID, 
                    PRODUCT_ENTITY.MODEL_ID, PRODUCT_ENTITY.CATEGORY_ID,
                    PRODUCT_ENTITY.SUPPLIER_ID)
        );
    }

    @Override
    @Operation(summary = "分页", description = "分页查询多个信息")
    @PostMapping("/page")
    protected R<PageResult<ProductEntity>> page(@RequestAttribute() JSONObject requestParams,
                                                @RequestAttribute(COOL_PAGE_OP) CrudOption<ProductEntity> option) {
        R<PageResult<ProductEntity>> result = super.page(requestParams, option);
        List<ProductEntity> list = result.getData().getList();

        if (list != null && !list.isEmpty()) {
            List<String> skus = list.stream()
                    .map(ProductEntity::getProductSku)
                    .filter(StrUtil::isNotBlank)
                    .toList();

            if (!skus.isEmpty()) {
                // 查询国网价 (priceType=1)
                List<PriceConfigEntity> prices = priceConfigService.list(QueryWrapper.create()
                        .in(PriceConfigEntity::getProductSku, skus)
                        .eq(PriceConfigEntity::getPriceType, 1));

                // 映射价格
                Map<String, java.math.BigDecimal> priceMap = prices.stream()
                        .collect(Collectors.toMap(
                                PriceConfigEntity::getProductSku,
                                p -> p.getPrice() != null ? p.getPrice() : p.getStateGridPrice(),
                                (v1, v2) -> v1
                        ));

                for (ProductEntity product : list) {
                    product.setPrice(priceMap.get(product.getProductSku()));
                }
            }
        }
        return result;
    }

    @Operation(summary = "导入", description = "导入商品")
    @PostMapping("/import")
    public R importProducts(@RequestParam("file") MultipartFile file) throws IOException {
        return R.ok(service.importProducts(file));
    }

    @Operation(summary = "导入JSON", description = "导入商品(JSON)")
    @PostMapping("/importJson")
    public R importProductsJson(@RequestBody List<Map<String, Object>> list) {
        return R.ok(service.importProducts(list));
    }

    @Operation(summary = "导出", description = "导出商品")
    @PostMapping("/export")
    public void exportProducts(@RequestBody JSONObject requestParams, HttpServletResponse response) throws IOException {
        service.exportProducts(requestParams, response);
    }
}

