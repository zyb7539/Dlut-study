package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class UserServiceImpl implements UserDetailsService {
    @Autowired
    XcUserMapper xcUserMapper;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    XcMenuMapper xcMenuMapper;

    // s即为输入的username
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        // 将传入的json转为AuthParamsDto
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            throw new RuntimeException("请求认证的参数不符合要求");
        }
        // 认证类型
        String authType = authParamsDto.getAuthType();
        // 根据认证类型，取出指定的bean
        // beanName
        String beanName = authType +"_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        XcUserExt execute = authService.execute(authParamsDto);
        // 如果查到用户得到密码，最终封装成一个UserDetails
        UserDetails userDetails = getUserPrincipal(execute);
        return userDetails;
    }
    public UserDetails getUserPrincipal(XcUserExt xcUserExt){
        String password = xcUserExt.getPassword();
        String[] authorities = {"test"};
        // authorities  根据用户id权限
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(xcUserExt.getId());
        if(xcMenus.size() > 0){
            List<String> permissons = new ArrayList<>();
            for (XcMenu xcMenu : xcMenus) {
                permissons.add(xcMenu.getCode());
            }
            //得到权限标识符
            authorities = permissons.toArray(new String[0]);
        }

        // 将用户信息转为json
        xcUserExt.setPassword(null);
        String userJson = JSON.toJSONString(xcUserExt);
        UserDetails userDetails = User.withUsername(userJson).password(password).authorities(authorities).build();

        return userDetails;
    }


}
