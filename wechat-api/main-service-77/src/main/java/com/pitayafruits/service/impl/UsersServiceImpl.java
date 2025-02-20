package com.pitayafruits.service.impl;

import com.pitayafruits.base.BaseInfoProperties;
import com.pitayafruits.exceptions.GraceException;
import com.pitayafruits.grace.result.ResponseStatusEnum;
import com.pitayafruits.mapper.UsersMapper;
import com.pitayafruits.pojo.Users;
import com.pitayafruits.pojo.bo.ModifyUserBO;
import com.pitayafruits.service.IUsersService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author cc
 * @since 2025-02-17
 */
@Service
public class UsersServiceImpl extends BaseInfoProperties implements IUsersService {

    @Resource
    private UsersMapper usersMapper;


    @Override
    @Transactional
    public void modifyUserInfo(ModifyUserBO modifyUserBO) {

        String wechatNum = modifyUserBO.getWechatNum();

        Users pendingUser = new Users();

        String userId = modifyUserBO.getUserId();


        if (StringUtils.isBlank(userId)) {
            GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_ERROR);
        }

        if (StringUtils.isNotBlank(wechatNum)) {
            String isExist = redis.get(REDIS_USER_ALREADY_UPDATE_WECHAT_NUM + ":" + userId);
            if (StringUtils.isNotBlank(isExist)) {
                GraceException.display(ResponseStatusEnum.WECHAT_NUM_ALREADY_MODIFIED_ERROR);
            }
        }

        pendingUser.setId(userId);
        pendingUser.setUpdatedTime(LocalDateTime.now());

        BeanUtils.copyProperties(modifyUserBO, pendingUser);

        usersMapper.updateById(pendingUser);

        if (StringUtils.isNotBlank(wechatNum)) {
            redis.setByDays(REDIS_USER_ALREADY_UPDATE_WECHAT_NUM + ":" + userId,
                    userId, 365);
        }


    }

    @Override
    public Users getById(String userId) {
        return usersMapper.selectById(userId);
    }

}
