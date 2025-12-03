package com.cool.modules.customer.service.impl;

import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.customer.entity.CustomerInvoiceEntity;
import com.cool.modules.customer.mapper.CustomerInvoiceMapper;
import com.cool.modules.customer.service.CustomerInvoiceService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.cool.modules.customer.entity.table.CustomerInvoiceEntityTableDef.CUSTOMER_INVOICE_ENTITY;

/**
 * 客户开票信息服务实现类
 */
@Service
@RequiredArgsConstructor
public class CustomerInvoiceServiceImpl extends BaseServiceImpl<CustomerInvoiceMapper, CustomerInvoiceEntity> 
        implements CustomerInvoiceService {
    
    @Override
    public List<CustomerInvoiceEntity> listByCustomerId(Long customerId) {
        return list(QueryWrapper.create()
            .where(CUSTOMER_INVOICE_ENTITY.CUSTOMER_ID.eq(customerId))
            .orderBy(CUSTOMER_INVOICE_ENTITY.IS_DEFAULT.desc())
            .orderBy(CUSTOMER_INVOICE_ENTITY.CREATE_TIME.desc()));
    }
    
    @Override
    public CustomerInvoiceEntity getDefaultByCustomerId(Long customerId) {
        return getOne(QueryWrapper.create()
            .where(CUSTOMER_INVOICE_ENTITY.CUSTOMER_ID.eq(customerId))
            .and(CUSTOMER_INVOICE_ENTITY.IS_DEFAULT.eq(1))
            .limit(1));
    }
}

