package com.cool.modules.product.service;

import com.cool.core.base.BaseService;
import com.cool.modules.config.entity.ProductEntity;

/**
 * 商品服务接口
 */
public interface ProductService extends BaseService<ProductEntity> {
    
    /**
     * 根据SKU获取商品
     * 
     * @param sku 商品SKU
     * @return 商品信息
     */
    ProductEntity getBySku(String sku);
    
    /**
     * 检查商品是否存在
     * 
     * @param sku 商品SKU
     * @return true-存在 false-不存在
     */
    boolean existsBySku(String sku);

    /**
     * 导入商品
     * @param file Excel文件
     * @return 结果
     */
    Object importProducts(org.springframework.web.multipart.MultipartFile file) throws java.io.IOException;

    /**
     * 导出商品
     * @param requestParams 请求参数
     * @param response 响应对象
     */
    void exportProducts(cn.hutool.json.JSONObject requestParams, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException;
    
    /**
     * 导入商品(JSON)
     * @param list 商品列表
     * @return 结果
     */
    Object importProducts(java.util.List<java.util.Map<String, Object>> list);
}

