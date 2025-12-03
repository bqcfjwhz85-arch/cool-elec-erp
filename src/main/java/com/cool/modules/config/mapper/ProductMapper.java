package com.cool.modules.config.mapper;

import com.cool.modules.config.entity.ProductEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品信息 Mapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<ProductEntity> {
}

