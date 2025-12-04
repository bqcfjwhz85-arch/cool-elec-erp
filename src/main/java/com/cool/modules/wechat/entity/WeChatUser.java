package com.cool.modules.wechat.entity;

import com.cool.core.annotation.ColumnDefine;
import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Getter
@Setter
@Table(value = "wechat_user", comment = "微信用户信息表")
public class WeChatUser extends BaseEntity<WeChatUser> {

    // 微信核心标识字段
    @ColumnDefine(comment = "微信用户唯一标识(openid)", length = 64, notNull = true)
    private String openid;

    @ColumnDefine(comment = "微信开放平台统一ID(unionid)", length = 64)
    private String unionid;

    // 用户基础信息
    @ColumnDefine(comment = "用户昵称", length = 100)
    private String nickname;

    @ColumnDefine(comment = "用户头像URL", length = 500)
    private String avatarUrl;

    // 手机号信息
    @ColumnDefine(comment = "用户手机号", length = 20)
    private String phone;

    // 微信会话信息
    @ColumnDefine(comment = "微信session_key", length = 100)
    @JsonIgnore  // 敏感信息，不序列化到前端
    private String sessionKey;

    // 时间信息
    @ColumnDefine(comment = "最后登录时间")
    private LocalDateTime lastLoginTime;

    // 状态字段
    @ColumnDefine(comment = "用户状态(0=正常,1=禁用)", defaultValue = "0")
    private Integer status;
}