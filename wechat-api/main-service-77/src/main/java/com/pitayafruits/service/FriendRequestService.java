package com.pitayafruits.service;

import com.pitayafruits.pojo.Users;
import com.pitayafruits.pojo.bo.ModifyUserBO;
import com.pitayafruits.pojo.bo.NewFriendRequestBo;
import com.pitayafruits.utils.PagedGridResult;

/**
 * <p>
 * 好友添加 服务类
 * </p>
 *
 * @author cc
 * @since 2025-02-17
 */
public interface FriendRequestService {


    /**
     * 新增添加好友的请求
     */
    void addNewRequest(NewFriendRequestBo newFriendRequestBo);

    /**
     * 查询新朋友的请求记录列表
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PagedGridResult queryNewFriendList(String userId,
                                       Integer page,
                                       Integer pageSize);

    /**
     * 通过好友请求
     * @param friendRequestId
     * @param friendRemark
     */
    public void passNewFriend(String friendRequestId, String friendRemark);

}
