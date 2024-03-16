package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;

/**
 * @description 微信扫码接入
 * */
public interface WxAuthService {

    /**
     * 微信申请令牌
     * @param code 授权码
     * */
    public XcUser wxAuth(String code);
}
