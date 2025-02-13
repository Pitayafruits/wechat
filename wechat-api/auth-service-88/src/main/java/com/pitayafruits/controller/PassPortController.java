package com.pitayafruits.controller;


import com.pitayafruits.base.BaseInfoProperties;
import com.pitayafruits.grace.result.GraceJSONResult;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("passport")
public class PassPortController extends BaseInfoProperties {

    @GetMapping("generateSMSCode")
    public GraceJSONResult generateSMSCode(String mobile,
                                           HttpServletRequest request) {
        if (StringUtils.isBlank(mobile)) {
            return GraceJSONResult.error();
        }
        String code = (int) (Math.random() * 9000 + 1000) + "";
        redis.set(MOBILE_SMSCODE + ":" + mobile, code, 30 * 60);
        return GraceJSONResult.ok();
    }

}
