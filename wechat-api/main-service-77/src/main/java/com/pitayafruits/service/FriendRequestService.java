package com.pitayafruits.service;

import com.pitayafruits.pojo.Users;
import com.pitayafruits.pojo.bo.ModifyUserBO;
import com.pitayafruits.pojo.bo.NewFriendRequestBo;

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

}
