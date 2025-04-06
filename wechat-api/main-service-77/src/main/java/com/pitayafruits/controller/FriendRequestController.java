package com.pitayafruits.controller;


import com.pitayafruits.base.BaseInfoProperties;
import com.pitayafruits.grace.result.GraceJSONResult;
import com.pitayafruits.pojo.bo.NewFriendRequestBo;
import com.pitayafruits.service.FriendRequestService;
import com.pitayafruits.utils.PagedGridResult;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping("queryNew")
    public GraceJSONResult queryNew(HttpServletRequest request,
                                    @RequestParam(defaultValue = "1", name = "page") Integer page,
                                    @RequestParam(defaultValue = "10", name = "pageSize") Integer pageSize) {

        String userId = request.getHeader(HEADER_USER_ID);

        PagedGridResult result = friendRequestService.queryNewFriendList(userId,
                page,
                pageSize);
        return GraceJSONResult.ok(result);
    }

    @PostMapping("pass")
    public GraceJSONResult pass(String friendRequestId, String friendRemark) {
        friendRequestService.passNewFriend(friendRequestId, friendRemark);
        return GraceJSONResult.ok();
    }


}
