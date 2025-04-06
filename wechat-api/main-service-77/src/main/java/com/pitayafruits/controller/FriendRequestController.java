package com.pitayafruits.controller;


import com.pitayafruits.base.BaseInfoProperties;
import com.pitayafruits.grace.result.GraceJSONResult;
import com.pitayafruits.pojo.bo.ModifyUserBO;
import com.pitayafruits.pojo.bo.NewFriendRequestBo;
import com.pitayafruits.service.FriendRequestService;
import com.pitayafruits.service.IUsersService;
import com.pitayafruits.vo.UsersVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("friendRequest")
@Slf4j
public class FriendRequestController extends BaseInfoProperties {
    @Resource
    private FriendRequestService friendRequestService;

    @PostMapping("add")
    public GraceJSONResult modify(@RequestBody NewFriendRequestBo newFriendRequestBo) {
        friendRequestService.addNewRequest(newFriendRequestBo);
        return GraceJSONResult.ok();
    }


}
