package com.cool.modules.sale.service.impl;

import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.sale.entity.SalesDeliveryDetailEntity;
import com.cool.modules.sale.mapper.SalesDeliveryDetailMapper;
import com.cool.modules.sale.service.SalesDeliveryDetailService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.cool.modules.sale.entity.table.SalesDeliveryDetailEntityTableDef.SALES_DELIVERY_DETAIL_ENTITY;

/**
 * 销售订单发货明细服务实现类
 */
@Slf4j
@Service
public class SalesDeliveryDetailServiceImpl extends BaseServiceImpl<SalesDeliveryDetailMapper, SalesDeliveryDetailEntity>
        implements SalesDeliveryDetailService {
    
    @Override
    public List<SalesDeliveryDetailEntity> listByDeliveryId(Long deliveryId) {
        return list(QueryWrapper.create()
            .where(SALES_DELIVERY_DETAIL_ENTITY.DELIVERY_ID.eq(deliveryId)));
    }
    
    @Override
    public void deleteByDeliveryId(Long deliveryId) {
        remove(QueryWrapper.create()
            .where(SALES_DELIVERY_DETAIL_ENTITY.DELIVERY_ID.eq(deliveryId)));
    }
}
