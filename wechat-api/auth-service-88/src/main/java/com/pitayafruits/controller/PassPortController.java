package com.pitayafruits.controller;


import com.pitayafruits.base.BaseInfoProperties;
import com.pitayafruits.grace.result.GraceJSONResult;
import com.pitayafruits.pojo.bo.RegistLoginBo;
import com.pitayafruits.utils.IPUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("passport")
public class PassPortController extends BaseInfoProperties {

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
    public GraceJSONResult regist(@RequestBody  RegistLoginBo registLoginBo) {
        String mobile = registLoginBo.getMobile();
        String smsCode = registLoginBo.getSmsCode();

        // 校验验证码


        return GraceJSONResult.ok();
    }


}
