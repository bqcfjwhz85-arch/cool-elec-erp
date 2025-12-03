package com.cool.modules.customer.mapper;

import com.cool.modules.customer.entity.CustomerEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户Mapper
 */
@Mapper
public interface CustomerMapper extends BaseMapper<CustomerEntity> {
}

