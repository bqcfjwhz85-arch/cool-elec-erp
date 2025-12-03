package com.cool.modules.config.mapper;

import com.cool.modules.config.entity.SupplierEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商信息 Mapper 接口
 */
@Mapper
public interface SupplierMapper extends BaseMapper<SupplierEntity> {
}
