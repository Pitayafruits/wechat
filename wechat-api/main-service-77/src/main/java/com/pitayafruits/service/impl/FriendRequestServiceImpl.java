package com.pitayafruits.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pitayafruits.base.BaseInfoProperties;
import com.pitayafruits.enums.FriendRequestVerifyStatus;
import com.pitayafruits.mapper.FriendRequestMapper;
import com.pitayafruits.mapper.UsersMapper;
import com.pitayafruits.pojo.FriendRequest;
import com.pitayafruits.pojo.bo.NewFriendRequestBo;
import com.pitayafruits.service.FriendRequestService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


/**
 * <p>
 * 好友添加 服务实现类
 * </p>
 *
 * @author cc
 * @since 2025-02-17
 */
@Service
public class FriendRequestServiceImpl extends BaseInfoProperties implements FriendRequestService {

    @Resource
    private FriendRequestMapper friendRequestMapper;

    @Override
    @Transactional
    public void addNewRequest(NewFriendRequestBo newFriendRequestBo) {
        // 先删除以前的记录
        QueryWrapper deleteWrapper = new QueryWrapper<FriendRequest>()
                .eq("my_id", newFriendRequestBo.getMyId())
                .eq("friend_id", newFriendRequestBo.getFriendId());
        friendRequestMapper.delete(deleteWrapper);

        // 再新增记录
        FriendRequest pendingFriendRequest = new FriendRequest();
        BeanUtils.copyProperties(newFriendRequestBo, pendingFriendRequest);
        pendingFriendRequest.setVerifyStatus(FriendRequestVerifyStatus.WAIT.type);
        pendingFriendRequest.setRequestTime(LocalDateTime.now());

        friendRequestMapper.insert(pendingFriendRequest);

    }
}
