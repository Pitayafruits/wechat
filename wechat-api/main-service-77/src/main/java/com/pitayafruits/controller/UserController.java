package com.pitayafruits.controller;


import com.pitayafruits.api.feign.UserInfoMicroServiceFeign;
import com.pitayafruits.base.BaseInfoProperties;
import com.pitayafruits.grace.result.GraceJSONResult;
import com.pitayafruits.pojo.Users;
import com.pitayafruits.pojo.bo.ModifyUserBO;
import com.pitayafruits.service.IUsersService;
import com.pitayafruits.vo.UsersVo;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("userinfo")
public class UserController extends BaseInfoProperties {
    @Resource
    private IUsersService usersService;

    @PostMapping("modify")
    public GraceJSONResult modify(@RequestBody ModifyUserBO modifyUserBO) {

        usersService.modifyUserInfo(modifyUserBO);

        UsersVo usersVo = getUserInfo(modifyUserBO.getUserId(), true);

        return GraceJSONResult.ok(usersVo);

    }

    private UsersVo getUserInfo(String userId, boolean needToken) {
        Users latestUser = usersService.getById(userId);

        UsersVo usersVo = new UsersVo();

        BeanUtils.copyProperties(latestUser, usersVo);

        if (needToken) {
            String uToken = TOKEN_USER_PREFIX + SYMBOL_DOT + UUID.randomUUID();
            redis.set(REDIS_USER_TOKEN + ":" + userId, uToken);
            usersVo.setUserToken(uToken);
        }

        return usersVo;
    }


    @PostMapping("updateFace")
    public GraceJSONResult updateFace(@RequestParam("userId") String userId,
                                      @RequestParam("face") String face) {

        ModifyUserBO modifyUserBO = new ModifyUserBO();

        modifyUserBO.setUserId(userId);
        modifyUserBO.setFace(face);

        usersService.modifyUserInfo(modifyUserBO);

        UsersVo usersVo = getUserInfo(modifyUserBO.getUserId(), true);

        return GraceJSONResult.ok(usersVo);

    }


}
