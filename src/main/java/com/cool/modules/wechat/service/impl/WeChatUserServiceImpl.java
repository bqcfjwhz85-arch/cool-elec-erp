package com.cool.modules.wechat.service.impl;

import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.wechat.entity.WeChatUser;
import com.cool.modules.wechat.mapper.WeChatUserMapper;
import com.cool.modules.wechat.service.WeChatUserService;
import org.springframework.stereotype.Service;

/**
 * 微信用户信息表
 */
@Service
public class WeChatUserServiceImpl extends BaseServiceImpl<WeChatUserMapper, WeChatUser> implements WeChatUserService {
}