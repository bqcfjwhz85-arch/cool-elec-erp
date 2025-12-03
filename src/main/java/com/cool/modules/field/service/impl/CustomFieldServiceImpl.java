
package com.cool.modules.field.service.impl;

import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.field.entity.CustomFieldEntity;
import com.cool.modules.field.mapper.CustomFieldMapper;
import com.cool.modules.field.service.CustomFieldService;
import org.springframework.stereotype.Service;

/**
 * 自定义字段
 */
@Service
public class CustomFieldServiceImpl extends BaseServiceImpl<CustomFieldMapper, CustomFieldEntity> implements CustomFieldService {
}
