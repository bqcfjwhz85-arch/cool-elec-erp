
package com.cool.modules.field.entity;

import com.cool.core.base.BaseEntity;
import com.cool.core.annotation.ColumnDefine;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(value = "custom_field", comment = "自定义字段")
public class CustomFieldEntity extends BaseEntity<CustomFieldEntity> {

    @ColumnDefine(comment = "菜单ID", notNull = true)
    private Long menuId;

    @ColumnDefine(comment = "字段名称", notNull = true)
    private String fieldName;

    @ColumnDefine(comment = "字段标签", notNull = true)
    private String fieldLabel;

    @ColumnDefine(comment = "字段类型", notNull = true)
    private String fieldType;

    @ColumnDefine(comment = "是否必填", defaultValue = "0")
    private Integer isRequired;

    @ColumnDefine(comment = "选项数据(JSON)", type = "text")
    private String options;

    @ColumnDefine(comment = "排序", defaultValue = "0")
    private Integer sort;

    @ColumnDefine(comment = "宽度(0-24)", defaultValue = "24")
    private Integer width;

    @ColumnDefine(comment = "其他属性(JSON)", type = "text")
    private String props;

}
