package com.pitayafruits.controller;


import com.pitayafruits.base.BaseInfoProperties;
import com.pitayafruits.grace.result.GraceJSONResult;
import com.pitayafruits.grace.result.ResponseStatusEnum;
import com.pitayafruits.pojo.Users;
import com.pitayafruits.pojo.bo.ModifyUserBO;
import com.pitayafruits.service.IUsersService;
import com.pitayafruits.pojo.vo.UsersVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
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


    @PostMapping("queryFriend")
    public GraceJSONResult queryFriend(String queryString,
                                       HttpServletRequest request) {

        if (StringUtils.isBlank(queryString)) {
            return GraceJSONResult.error();
        }

        Users friend = usersService.getByWechatNumOrMobile(queryString);

        if (friend == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FRIEND_NOT_EXIST_ERROR);
        }

        // 判断 不能添加自己为好友
        String myId = request.getHeader(HEADER_USER_ID);
        if (myId.equals(friend.getId())) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.CAN_NOT_ADD_SELF_FRIEND_ERROR);
        }

        return GraceJSONResult.ok(friend);
    }


}
