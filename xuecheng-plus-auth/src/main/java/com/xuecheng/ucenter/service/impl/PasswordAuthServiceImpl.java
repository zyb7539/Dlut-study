package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.feignclient.checkcodeclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.AuthCache;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
/**
 * @description 账号密码校验
 * */
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {
    // TODO 内网穿透是什么
    @Autowired
    XcUserMapper xcUserMapper;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    CheckCodeClient checkCodeClient;
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        // 校验验证码
        String checkcode = authParamsDto.getCheckcode();
        String checkcodekey = authParamsDto.getCheckcodekey();
        if(StringUtils.isEmpty(checkcodekey) || StringUtils.isEmpty(checkcode)){
            throw new RuntimeException("请输入验证码");
        }
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if(verify == null || !verify){
            throw new RuntimeException("验证码输入错误");
        }


        // 校验密码
        //查询用户不存在，返回null即可，spring security框架抛出异常表示不存在
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, authParamsDto.getUsername()));
        if(xcUser == null){
            throw  new RuntimeException("账户不存在");
        }
        // 正确的密码
        String password = xcUser.getPassword();
        // 用户输入的密码
        String passwordForm = authParamsDto.getPassword();
        boolean matches = passwordEncoder.matches(passwordForm, password);
        if(!matches){
            throw new RuntimeException("账号或密码错误");
        }
        XcUserExt xcUserExt= new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);


        return xcUserExt;
    }
}
