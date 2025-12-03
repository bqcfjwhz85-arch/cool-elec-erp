package com.cool.modules.customer.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.base.ModifyEnum;
import com.cool.core.exception.CoolPreconditions;
import com.cool.core.util.CodeGenerator;
import com.cool.modules.customer.entity.CustomerEntity;
import com.cool.modules.customer.mapper.CustomerMapper;
import com.cool.modules.customer.service.CustomerService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.cool.modules.customer.entity.table.CustomerEntityTableDef.CUSTOMER_ENTITY;

/**
 * 客户服务实现类
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl extends BaseServiceImpl<CustomerMapper, CustomerEntity> 
        implements CustomerService {
    
    @Override
    public CustomerEntity getByName(String name) {
        return getOne(QueryWrapper.create()
            .where(CUSTOMER_ENTITY.CUSTOMER_NAME.eq(name))
            .and(CUSTOMER_ENTITY.STATUS.eq(1)));
    }
    
    @Override
    public CustomerEntity getByCode(String code) {
        return getOne(QueryWrapper.create()
            .where(CUSTOMER_ENTITY.CUSTOMER_CODE.eq(code))
            .and(CUSTOMER_ENTITY.STATUS.eq(1)));
    }
    
    @Override
    public Map<String, Object> getCustomerStatistics(Long customerId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // TODO: 实现客户统计信息查询
        // 需要等销售订单模块实现后，查询该客户的订单数量、交易金额等
        statistics.put("customerId", customerId);
        statistics.put("orderCount", 0);
        statistics.put("totalAmount", 0);
        
        return statistics;
    }
    
    @Override
    public void modifyBefore(JSONObject requestParams, CustomerEntity entity, ModifyEnum type) {
        // 新增/修改前校验
        if (type == ModifyEnum.ADD || type == ModifyEnum.UPDATE) {
            // 设置默认状态：如果没有传status，默认为启用
            if (entity.getStatus() == null) {
                entity.setStatus(1);
            }
            
            // 新增时自动生成客户编码（如果前端未提供）
            if (type == ModifyEnum.ADD && StrUtil.isBlank(entity.getCustomerCode())) {
                String autoCode = CodeGenerator.generateCode("CUS", todayPrefix -> {
                    // 查询当天该前缀的最大编码
                    CustomerEntity maxCustomer = getOne(QueryWrapper.create()
                        .where(CUSTOMER_ENTITY.CUSTOMER_CODE.like(todayPrefix + "%"))
                        .orderBy(CUSTOMER_ENTITY.CUSTOMER_CODE, false)
                        .limit(1));
                    return maxCustomer != null ? maxCustomer.getCustomerCode() : null;
                });
                entity.setCustomerCode(autoCode);
            }
            
            // 校验编码唯一性
            if (StrUtil.isNotBlank(entity.getCustomerCode())) {
                QueryWrapper qw = QueryWrapper.create()
                    .where(CUSTOMER_ENTITY.CUSTOMER_CODE.eq(entity.getCustomerCode()));
                
                if (type == ModifyEnum.UPDATE) {
                    qw.and(CUSTOMER_ENTITY.ID.ne(entity.getId()));
                }
                
                CustomerEntity exists = getOne(qw);
                // 如果存在重复编码，抛出异常
                CoolPreconditions.check(exists != null, "客户编码已存在");
            }
        }
    }
}

