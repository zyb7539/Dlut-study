package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * @description 统一认证接口
 * */
public interface AuthService {
    /**
     * @description 认证方法
     * @param authParamsDto 认证参数
     * */
    XcUserExt execute(AuthParamsDto authParamsDto);
}
