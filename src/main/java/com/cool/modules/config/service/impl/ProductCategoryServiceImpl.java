package com.cool.modules.config.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.base.ModifyEnum;
import com.cool.core.exception.CoolPreconditions;
import com.cool.core.util.CodeGenerator;
import com.cool.modules.config.entity.ProductCategoryEntity;
import com.cool.modules.config.mapper.ProductCategoryMapper;
import com.cool.modules.config.service.PointConfigService;
import com.cool.modules.config.service.ProductCategoryService;
import com.cool.modules.config.service.ProductModelService;
import com.cool.modules.product.service.ProductService;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.cool.modules.config.entity.table.PointConfigEntityTableDef.POINT_CONFIG_ENTITY;
import static com.cool.modules.config.entity.table.ProductCategoryEntityTableDef.PRODUCT_CATEGORY_ENTITY;
import static com.cool.modules.config.entity.table.ProductEntityTableDef.PRODUCT_ENTITY;
import static com.cool.modules.config.entity.table.ProductModelEntityTableDef.PRODUCT_MODEL_ENTITY;

/**
 * 商品类别服务实现类
 */
@Service
public class ProductCategoryServiceImpl extends BaseServiceImpl<ProductCategoryMapper, ProductCategoryEntity> 
        implements ProductCategoryService {

    private final ProductModelService productModelService;
    private final ProductService productService;
    private final PointConfigService pointConfigService;

    // 使用构造器注入，@Lazy 打破循环依赖
    public ProductCategoryServiceImpl(@Lazy ProductModelService productModelService,
                                     @Lazy ProductService productService,
                                     @Lazy PointConfigService pointConfigService) {
        this.productModelService = productModelService;
        this.productService = productService;
        this.pointConfigService = pointConfigService;
    }

    @Override
    public void modifyBefore(JSONObject requestParams, ProductCategoryEntity entity, ModifyEnum type) {
        // 删除前检查是否被引用
        if (type == ModifyEnum.DELETE) {
            Long categoryId = entity.getId();
            
            // 1. 检查是否有子类别
            long childCount = count(QueryWrapper.create()
                .where(PRODUCT_CATEGORY_ENTITY.PARENT_ID.eq(categoryId)));
            CoolPreconditions.check(childCount > 0, 
                "该类别下存在 " + childCount + " 个子类别，无法删除。请先删除子类别");
            
            // 2. 检查型号表是否引用
            long modelCount = productModelService.count(QueryWrapper.create()
                .where(PRODUCT_MODEL_ENTITY.CATEGORY_ID.eq(categoryId)));
            CoolPreconditions.check(modelCount > 0, 
                "该类别已被 " + modelCount + " 个型号引用，无法删除。请先删除相关型号或修改型号的类别");
            
            // 3. 检查商品表是否引用
            long productCount = productService.count(QueryWrapper.create()
                .where(PRODUCT_ENTITY.CATEGORY_ID.eq(categoryId)));
            CoolPreconditions.check(productCount > 0, 
                "该类别已被 " + productCount + " 个商品引用，无法删除。请先删除相关商品或修改商品的类别");
            
            // 4. 检查点位配置表是否引用
            long pointConfigCount = pointConfigService.count(QueryWrapper.create()
                .where(POINT_CONFIG_ENTITY.CATEGORY_ID.eq(categoryId)));
            CoolPreconditions.check(pointConfigCount > 0, 
                "该类别已被 " + pointConfigCount + " 个点位配置引用，无法删除。请先删除相关点位配置或修改点位配置的类别");
        }
        
        // 新增时自动生成类别编码（如果前端未提供）
        if (type == ModifyEnum.ADD && StrUtil.isBlank(entity.getCategoryCode())) {
            String autoCode = CodeGenerator.generateCode("CAT", todayPrefix -> {
                // 查询当天该前缀的最大编码
                ProductCategoryEntity maxCategory = getOne(QueryWrapper.create()
                    .where(PRODUCT_CATEGORY_ENTITY.CATEGORY_CODE.like(todayPrefix + "%"))
                    .orderBy(PRODUCT_CATEGORY_ENTITY.CATEGORY_CODE, false)
                    .limit(1));
                return maxCategory != null ? maxCategory.getCategoryCode() : null;
            });
            entity.setCategoryCode(autoCode);
        }
        
        // 新增或修改时填充父级名称和层级
        if (type == ModifyEnum.ADD || type == ModifyEnum.UPDATE) {
            if (entity.getParentId() != null && entity.getParentId() > 0) {
                ProductCategoryEntity parent = getById(entity.getParentId());
                if (parent != null) {
                    entity.setParentName(parent.getCategoryName());
                    entity.setLevel(parent.getLevel() + 1);
                    
                    // 构建类别路径
                    if (parent.getCategoryPath() != null) {
                        entity.setCategoryPath(parent.getCategoryPath() + "/" + entity.getId());
                    } else {
                        entity.setCategoryPath(parent.getId() + "/" + entity.getId());
                    }
                }
            } else {
                // 顶级类别
                entity.setParentId(0L);
                entity.setParentName(null);
                entity.setLevel(1);
                entity.setCategoryPath(null);
            }
        }
    }

    @Override
    public ProductCategoryEntity getByCode(String categoryCode) {
        return getOne(QueryWrapper.create()
            .where(PRODUCT_CATEGORY_ENTITY.CATEGORY_CODE.eq(categoryCode)));
    }

    @Override
    public ProductCategoryEntity getByName(String categoryName) {
        return getOne(QueryWrapper.create()
            .where(PRODUCT_CATEGORY_ENTITY.CATEGORY_NAME.eq(categoryName)));
    }

    @Override
    public List<ProductCategoryEntity> listByParentId(Long parentId) {
        return list(QueryWrapper.create()
            .where(PRODUCT_CATEGORY_ENTITY.PARENT_ID.eq(parentId))
            .and(PRODUCT_CATEGORY_ENTITY.STATUS.eq(1))
            .orderBy(PRODUCT_CATEGORY_ENTITY.SORT_ORDER, true)
            .orderBy(PRODUCT_CATEGORY_ENTITY.CATEGORY_NAME, true));
    }

    @Override
    public List<ProductCategoryEntity> getTreeList() {
        // 查询所有启用的类别
        List<ProductCategoryEntity> allCategories = list(QueryWrapper.create()
            .where(PRODUCT_CATEGORY_ENTITY.STATUS.eq(1))
            .orderBy(PRODUCT_CATEGORY_ENTITY.SORT_ORDER, true)
            .orderBy(PRODUCT_CATEGORY_ENTITY.CATEGORY_NAME, true));
        
        // 构建树形结构（这里返回扁平列表，前端可以根据parentId构建树）
        return allCategories;
    }
}

