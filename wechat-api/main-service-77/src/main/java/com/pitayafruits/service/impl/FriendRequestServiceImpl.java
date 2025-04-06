package com.pitayafruits.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pitayafruits.base.BaseInfoProperties;
import com.pitayafruits.enums.FriendRequestVerifyStatus;
import com.pitayafruits.mapper.FriendRequestMapper;
import com.pitayafruits.mapper.FriendRequestMapperCustom;
import com.pitayafruits.mapper.UsersMapper;
import com.pitayafruits.pojo.FriendRequest;
import com.pitayafruits.pojo.bo.NewFriendRequestBo;
import com.pitayafruits.pojo.vo.NewFriendsVO;
import com.pitayafruits.service.FriendRequestService;
import com.pitayafruits.utils.PagedGridResult;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


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

    @Resource
    private FriendRequestMapperCustom friendRequestMapperCustom;

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


    @Override
    public PagedGridResult queryNewFriendList(String userId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("mySelfId", userId);

        Page<NewFriendsVO> pageInfo = new Page<>(page, pageSize);
        friendRequestMapperCustom.queryNewFriendList(pageInfo, map);

        return setterPagedGridPlus(pageInfo);
    }
}
