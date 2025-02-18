package com.pitayafruits.controller;


import com.pitayafruits.base.BaseInfoProperties;
import com.pitayafruits.grace.result.GraceJSONResult;
import com.pitayafruits.grace.result.ResponseStatusEnum;
import com.pitayafruits.pojo.Users;
import com.pitayafruits.pojo.bo.RegistLoginBo;
import com.pitayafruits.service.IUsersService;
import com.pitayafruits.utils.IPUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("passport")
public class PassPortController extends BaseInfoProperties {

    @Resource
    private IUsersService usersService;

    @PostMapping("getSMSCode")
    public GraceJSONResult getSMSCode(String mobile,
                                      HttpServletRequest request) {
        if (StringUtils.isBlank(mobile)) {
            return GraceJSONResult.error();
        }

        String requestIp = IPUtil.getRequestIp(request);
        redis.setnx60s(MOBILE_SMSCODE + ":" +requestIp, mobile);

        String code = (int) (Math.random() * 9000 + 1000) + "";
        redis.set(MOBILE_SMSCODE + ":" + mobile, code, 30 * 60);

        return GraceJSONResult.ok();
    }


    @PostMapping("regist")
    public GraceJSONResult regist(@RequestBody  @Valid RegistLoginBo registLoginBo) {
        String mobile = registLoginBo.getMobile();
        String smsCode = registLoginBo.getSmsCode();
        String nickname = registLoginBo.getNickname();

        // 校验验证码
        String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(smsCode)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }
        // 根据手机号查询用户是否存在
        Users user = usersService.queryMobileIfExist(mobile);
        if (user == null) {
            // 如果不存在，则创建用户
            user = usersService.createUser(mobile, nickname);
        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_ALREADY_EXIST_ERROR);
        }
        // 用户注册成功后，删除短信验证码
        redis.del(MOBILE_SMSCODE + ":" + mobile);
        // 返回数据
        return GraceJSONResult.ok(user);
    }


    @PostMapping("login")
    public GraceJSONResult login(@RequestBody  @Valid RegistLoginBo registLoginBo) {
        String mobile = registLoginBo.getMobile();
        String smsCode = registLoginBo.getSmsCode();

        // 校验验证码
        String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(smsCode)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }
        // 根据手机号查询用户是否存在
        Users user = usersService.queryMobileIfExist(mobile);
        if (user == null) {
            // 如果不存在，则创建用户
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        // 用户注册成功后，删除短信验证码
        redis.del(MOBILE_SMSCODE + ":" + mobile);

        // 返回数据
        return GraceJSONResult.ok(user);
    }


}
