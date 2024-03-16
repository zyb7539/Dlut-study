package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.xuecheng.ucenter.mapper.XcRoleMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AuthService;
import com.xuecheng.ucenter.service.WxAuthService;
import org.bouncycastle.math.ec.ScaleXNegateYPointMap;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * @description 微信扫码认证
 * */
@Service("wx_authservice")
public class WxAuthServiceImpl implements AuthService, WxAuthService {
    @Autowired
    XcUserMapper xcUserMapper;
    @Autowired
    XcUserRoleMapper xcUserRoleMapper;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    WxAuthServiceImpl proxy;

    @Value("${weixin.appid}")
    String appid;
    @Value("${weixin.secret}")
    String secret;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        // 得到账号
        String username = authParamsDto.getUsername();

        // 查询数据库
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if(xcUser == null){
            throw new RuntimeException("用户不存在");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        return xcUserExt;
    }

    @Override
    public XcUser wxAuth(String code) {
        // 申请令牌
        Map<String, String> accessToken = getAccess_token(code);
        // 携带令牌访问信息
        Map<String, String> userinfo = getUserinfo(accessToken.get("access_token"), accessToken.get("openid"));
        // 存数据库
        XcUser xcUser = proxy.addWxUser(userinfo);
        return xcUser;
    }
    /**
     * 接口地址
     * https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
     * 申请访问令牌,响应示例
     {
     "access_token":"ACCESS_TOKEN",
     "expires_in":7200,
     "refresh_token":"REFRESH_TOKEN",
     "openid":"OPENID",
     "scope":"SCOPE",
     "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     }
     */
    private Map<String,String> getAccess_token(String code){
        String url_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        // 最终的请求路径
        String url = String.format(url_template, appid, secret, code);

        // 远程调用url
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        String body = exchange.getBody();
        //将body转为map
        Map<String,String> map = JSON.parseObject(body, Map.class);

        return map;
    }

    /**获取用户信息，示例如下：
     {
     "openid":"OPENID",
     "nickname":"NICKNAME",
     "sex":1,
     "province":"PROVINCE",
     "city":"CITY",
     "country":"COUNTRY",
     "headimgurl": "https://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
     "privilege":[
     "PRIVILEGE1",
     "PRIVILEGE2"
     ],
     "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
     }
     */
    private Map<String,String> getUserinfo(String access_token,String openid){
        java.lang.String url_template = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        String url = java.lang.String.format(url_template, access_token, openid);

        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        // 存到数据库
        String body = new String(exchange.getBody().getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);
        Map<String,String> map = JSON.parseObject(body, Map.class);
        return map;
    }
    @Transactional
    public XcUser addWxUser(Map<String,String> userInfo){
        String unionid = userInfo.get("unionid");
        //查询用户信息
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getWxUnionid, unionid));
        if(xcUser != null){
            // TODO 修改
            return xcUser;
        }
         //向数据库新增记录
        xcUser = new XcUser();
        String uuid = UUID.randomUUID().toString();
        xcUser.setId(uuid);
        xcUser.setUsername(unionid);
        xcUser.setPassword(unionid);
        xcUser.setWxUnionid(unionid);

        xcUser.setNickname(userInfo.get("nickname"));
        xcUser.setName(userInfo.get("nickname"));

        xcUser.setUtype("101001");  //学生类型
        xcUser.setStatus("1");  //用户状态
        xcUser.setCreateTime(LocalDateTime.now());

        xcUserMapper.insert(xcUser);
         //向用户角色表插入数据

        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setUserId(uuid);
        xcUserRole.setRoleId("17"); //学生类型
        xcUserRole.setCreateTime(LocalDateTime.now());

        xcUserRoleMapper.insert(xcUserRole);

        return xcUser;
    }

}
