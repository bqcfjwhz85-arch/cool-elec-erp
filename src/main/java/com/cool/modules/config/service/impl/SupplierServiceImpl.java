package com.cool.modules.config.service.impl;

import static com.cool.modules.config.entity.table.ProductEntityTableDef.PRODUCT_ENTITY;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.exception.CoolException;
import com.cool.modules.config.entity.SupplierEntity;
import com.cool.modules.config.entity.SupplierGoodsEntity;
import com.cool.modules.config.entity.ProductEntity;
import com.cool.modules.config.mapper.SupplierGoodsMapper;
import com.cool.modules.config.mapper.SupplierMapper;
import com.cool.modules.config.mapper.ProductMapper;
import com.cool.modules.config.service.SupplierService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 供应商信息服务实现类
 */
@Service
public class SupplierServiceImpl extends BaseServiceImpl<SupplierMapper, SupplierEntity> implements SupplierService {

    @Resource
    private SupplierGoodsMapper supplierGoodsMapper;
    
    @Resource
    private ProductMapper productMapper;

    @Override
    public Long add(SupplierEntity entity) {
        // 校验统一社会信用代码唯一性
        if (StrUtil.isNotBlank(entity.getSocialCreditCode())) {
            long count = this.count(QueryWrapper.create()
                    .eq(SupplierEntity::getSocialCreditCode, entity.getSocialCreditCode()));
            if (count > 0) {
                throw new CoolException("统一社会信用代码已存在");
            }
        }

        // 自动生成供应商编码 SUP+年月+6位
        String prefix = "SUP" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        
        // 查询当月最大编码
        String maxCode = getMapper().selectOneByQueryAs(
                QueryWrapper.create()
                        .select(SupplierEntity::getSupplierCode)
                        .like(SupplierEntity::getSupplierCode, prefix + "%")
                        .orderBy(SupplierEntity::getSupplierCode, false)
                        .limit(1),
                String.class
        );

        long seq = 1;
        if (StrUtil.isNotBlank(maxCode)) {
            String seqStr = maxCode.substring(prefix.length());
            if (StrUtil.isNumeric(seqStr)) {
                seq = Long.parseLong(seqStr) + 1;
            }
        }
        entity.setSupplierCode(prefix + String.format("%06d", seq));

        return super.add(entity);
    }

    @Override
    public boolean update(SupplierEntity entity) {
        // 校验统一社会信用代码唯一性
        if (StrUtil.isNotBlank(entity.getSocialCreditCode())) {
            long count = this.count(QueryWrapper.create()
                    .eq(SupplierEntity::getSocialCreditCode, entity.getSocialCreditCode())
                    .ne(SupplierEntity::getId, entity.getId()));
            if (count > 0) {
                throw new CoolException("统一社会信用代码已存在");
            }
        }
        return super.update(entity);
    }

    @Resource
    private com.cool.modules.config.service.PriceConfigService priceConfigService;

    @Override
    public JSONObject getGoods(Long supplierId, Integer page, Integer size, String keyword) {
        // 查询关联的商品ID列表
        List<SupplierGoodsEntity> relations = supplierGoodsMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq(SupplierGoodsEntity::getSupplierId, supplierId)
        );
        
        if (relations.isEmpty()) {
            JSONObject result = new JSONObject();
            result.set("list", new ArrayList<>());
            result.set("pagination", new JSONObject()
                    .set("total", 0)
                    .set("page", page != null ? page : 1)
                    .set("size", size != null ? size : 10));
            return result;
        }
        
        List<Long> goodsIds = relations.stream()
                .map(SupplierGoodsEntity::getGoodsId)
                .toList();
        
        // 构造查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .in(ProductEntity::getId, goodsIds);
        
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.and(
                PRODUCT_ENTITY.PRODUCT_SKU.like(keyword)
                    .or(PRODUCT_ENTITY.PRODUCT_NAME.like(keyword))
                    .or(PRODUCT_ENTITY.SPECIFICATION.like(keyword))
            );
        }
        
        // 分页查询商品
        Page<ProductEntity> pageResult = productMapper.paginate(
                Page.of(page != null ? page : 1, size != null ? size : 10),
                queryWrapper
        );

        List<ProductEntity> list = pageResult.getRecords();
        if (list != null && !list.isEmpty()) {
            // 提取SKU列表
            List<String> skus = list.stream()
                    .map(ProductEntity::getProductSku)
                    .filter(StrUtil::isNotBlank)
                    .toList();

            if (!skus.isEmpty()) {
                // 查询国网价 (priceType=1)
                List<com.cool.modules.config.entity.PriceConfigEntity> prices = priceConfigService.list(QueryWrapper.create()
                        .in(com.cool.modules.config.entity.PriceConfigEntity::getProductSku, skus)
                        .eq(com.cool.modules.config.entity.PriceConfigEntity::getPriceType, 1));

                // 映射价格
                java.util.Map<String, java.math.BigDecimal> priceMap = prices.stream()
                        .collect(java.util.stream.Collectors.toMap(
                                com.cool.modules.config.entity.PriceConfigEntity::getProductSku,
                                p -> p.getPrice() != null ? p.getPrice() : p.getStateGridPrice(),
                                (v1, v2) -> v1 // 如果有重复，取第一个
                        ));

                for (ProductEntity product : list) {
                    product.setPrice(priceMap.get(product.getProductSku()));
                }
            }
        }
        
        JSONObject result = new JSONObject();
        result.set("list", list);
        result.set("pagination", new JSONObject()
                .set("total", pageResult.getTotalRow())
                .set("page", pageResult.getPageNumber())
                .set("size", pageResult.getPageSize()));
        
        return result;
    }

    @Override
    public void addGoods(Long supplierId, List<Long> goodsIds) {
        List<Long> newGoodsIds = goodsIds != null ? goodsIds : new ArrayList<>();
        
        // 1. 查询当前已关联的商品ID列表
        List<SupplierGoodsEntity> currentRelations = supplierGoodsMapper.selectListByQuery(
                QueryWrapper.create().eq(SupplierGoodsEntity::getSupplierId, supplierId)
        );
        List<Long> oldGoodsIds = currentRelations.stream()
                .map(SupplierGoodsEntity::getGoodsId)
                .toList();
        
        // 2. 计算需要删除的商品 (在旧列表中但不在新列表中)
        List<Long> toDelete = oldGoodsIds.stream()
                .filter(id -> !newGoodsIds.contains(id))
                .toList();
        
        // 3. 计算需要添加的商品 (在新列表中但不在旧列表中)
        List<Long> toAdd = newGoodsIds.stream()
                .filter(id -> !oldGoodsIds.contains(id))
                .toList();
        
        // 4. 处理删除
        if (!toDelete.isEmpty()) {
            // 删除关联记录
            supplierGoodsMapper.deleteByQuery(
                    QueryWrapper.create()
                            .eq(SupplierGoodsEntity::getSupplierId, supplierId)
                            .in(SupplierGoodsEntity::getGoodsId, toDelete)
            );
            
            // 更新商品的supplierId (如果当前是该供应商)
            for (Long goodsId : toDelete) {
                ProductEntity product = productMapper.selectOneById(goodsId);
                if (product != null && supplierId.equals(product.getSupplierId())) {
                    product.setSupplierId(null);
                    productMapper.update(product);
                }
            }
        }
        
        // 5. 处理添加
        if (!toAdd.isEmpty()) {
            // 批量插入新关联
            List<SupplierGoodsEntity> entities = new ArrayList<>();
            for (Long goodsId : toAdd) {
                SupplierGoodsEntity entity = new SupplierGoodsEntity();
                entity.setSupplierId(supplierId);
                entity.setGoodsId(goodsId);
                entities.add(entity);
            }
            supplierGoodsMapper.insertBatch(entities);
            
            // 更新商品的supplierId
            for (Long goodsId : toAdd) {
                ProductEntity product = productMapper.selectOneById(goodsId);
                if (product != null) {
                    product.setSupplierId(supplierId);
                    productMapper.update(product);
                }
            }
        }
    }

    @Override
    public void removeGoods(Long supplierId, Long goodsId) {
        supplierGoodsMapper.deleteByQuery(
                QueryWrapper.create()
                        .eq(SupplierGoodsEntity::getSupplierId, supplierId)
                        .eq(SupplierGoodsEntity::getGoodsId, goodsId)
        );
        
        // 检查该商品是否还有其他供应商关联
        long count = supplierGoodsMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(SupplierGoodsEntity::getGoodsId, goodsId)
        );
        
        // 如果没有其他供应商关联,清空商品的supplierId字段
        if (count == 0) {
            ProductEntity product = productMapper.selectOneById(goodsId);
            if (product != null && supplierId.equals(product.getSupplierId())) {
                product.setSupplierId(null);
                productMapper.update(product);
            }
        }
    }
}
