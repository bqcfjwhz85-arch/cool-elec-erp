package com.cool.modules.config.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.base.ModifyEnum;
import com.cool.core.exception.CoolPreconditions;
import com.cool.core.util.CodeGenerator;
import com.cool.modules.config.entity.PlatformEntity;
import com.cool.modules.config.mapper.PlatformMapper;
import com.cool.modules.config.service.PlatformService;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.stereotype.Service;

import static com.cool.modules.config.entity.table.PlatformEntityTableDef.PLATFORM_ENTITY;

/**
 * 平台服务实现类
 */
@Service
public class PlatformServiceImpl extends BaseServiceImpl<PlatformMapper, PlatformEntity> 
        implements PlatformService {
    
    @Override
    public PlatformEntity getByCode(String code) {
        return getOne(QueryWrapper.create()
            .where(PLATFORM_ENTITY.PLATFORM_CODE.eq(code))
            .and(PLATFORM_ENTITY.STATUS.eq(1)));
    }
    
    @Override
    public void modifyBefore(JSONObject requestParams, PlatformEntity entity, ModifyEnum type) {
        // 新增/修改前校验
        if (type == ModifyEnum.ADD || type == ModifyEnum.UPDATE) {
            // 设置默认状态：如果没有传status，默认为启用
            if (entity.getStatus() == null) {
                entity.setStatus(1);
            }
            
            // 新增时自动生成平台编码（如果前端未提供）
            if (type == ModifyEnum.ADD && StrUtil.isBlank(entity.getPlatformCode())) {
                String autoCode = CodeGenerator.generateCode("PLT", todayPrefix -> {
                    // 查询当天该前缀的最大编码
                    PlatformEntity maxPlatform = getOne(QueryWrapper.create()
                        .where(PLATFORM_ENTITY.PLATFORM_CODE.like(todayPrefix + "%"))
                        .orderBy(PLATFORM_ENTITY.PLATFORM_CODE, false)
                        .limit(1));
                    return maxPlatform != null ? maxPlatform.getPlatformCode() : null;
                });
                entity.setPlatformCode(autoCode);
            }
            
            // 校验编码唯一性
            if (StrUtil.isNotBlank(entity.getPlatformCode())) {
                QueryWrapper qw = QueryWrapper.create()
                    .where(PLATFORM_ENTITY.PLATFORM_CODE.eq(entity.getPlatformCode()));
                
                if (type == ModifyEnum.UPDATE) {
                    qw.and(PLATFORM_ENTITY.ID.ne(entity.getId()));
                }
                
                PlatformEntity exists = getOne(qw);
                // 如果存在重复编码，抛出异常
                CoolPreconditions.check(exists != null, "平台编码已存在");
            }
        }
    }
}

