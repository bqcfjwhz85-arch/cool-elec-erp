package com.cool.modules.config.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.base.ModifyEnum;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.config.entity.ProviderEntity;
import com.cool.modules.config.mapper.ProviderMapper;
import com.cool.modules.config.service.ProviderService;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.cool.modules.config.entity.table.ProviderEntityTableDef.PROVIDER_ENTITY;

/**
 * 服务商服务实现类
 */
@Service
public class ProviderServiceImpl extends BaseServiceImpl<ProviderMapper, ProviderEntity> 
        implements ProviderService {
    
    @Override
    public ProviderEntity getByCode(String code) {
        return getOne(QueryWrapper.create()
            .where(PROVIDER_ENTITY.PROVIDER_CODE.eq(code))
            .and(PROVIDER_ENTITY.STATUS.eq(1)));
    }
    
    @Override
    public boolean isRegionalProvider(Long providerId) {
        if (providerId == null) {
            return false;
        }
        ProviderEntity provider = getById(providerId);
        return provider != null && Integer.valueOf(1).equals(provider.getIsRegional());
    }
    
    @Override
    public void modifyBefore(JSONObject requestParams, ProviderEntity entity, ModifyEnum type) {
        // 新增时自动生成服务商编码
        if (type == ModifyEnum.ADD) {
            // 生成服务商编码：FWS + 年月日 + 序号
            String dateStr = DateUtil.format(LocalDateTime.now(), "yyyyMMdd");
            String providerCode = "FWS" + dateStr + String.format("%04d", 
                count(QueryWrapper.create()
                    .where(PROVIDER_ENTITY.PROVIDER_CODE.like("FWS" + dateStr + "%"))) + 1);
            entity.setProviderCode(providerCode);
        }
        
        // 新增/修改前校验
        if (type == ModifyEnum.ADD || type == ModifyEnum.UPDATE) {
            // 1. 必填字段校验
            CoolPreconditions.check(StrUtil.isBlank(entity.getProviderName()), 
                "服务商名称不能为空");
            
            // 2. 设置默认状态：如果没有传status，默认为启用
            if (entity.getStatus() == null) {
                entity.setStatus(1);
            }
            
            // 3. 校验服务商编码唯一性（更新时才需要校验）
            if (type == ModifyEnum.UPDATE) {
                QueryWrapper qw = QueryWrapper.create()
                    .where(PROVIDER_ENTITY.PROVIDER_CODE.eq(entity.getProviderCode()))
                    .and(PROVIDER_ENTITY.ID.ne(entity.getId()));
                
                ProviderEntity exists = getOne(qw);
                CoolPreconditions.check(exists != null, "服务商编码已存在");
            }
        }
    }
}
