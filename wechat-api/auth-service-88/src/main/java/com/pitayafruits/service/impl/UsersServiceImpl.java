package com.pitayafruits.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pitayafruits.base.BaseInfoProperties;
import com.pitayafruits.enums.Sex;
import com.pitayafruits.mapper.UsersMapper;
import com.pitayafruits.pojo.Users;
import com.pitayafruits.service.IUsersService;
import com.pitayafruits.utils.DesensitizationUtil;
import com.pitayafruits.utils.LocalDateUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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
    public Users queryMobileIfExist(String mobile) {
        return usersMapper.selectOne(
                new QueryWrapper<Users>()
                        .eq("mobile", mobile));
    }

    @Override
    @Transactional
    public Users createUser(String mobile, String nickname) {
        Users user = new Users();

        user.setMobile(mobile);

        String uuid = UUID.randomUUID().toString();
        String[] uuidStr = uuid.split("-");
        String wechatNum = "wx" + uuidStr[0] + uuidStr[1];
        user.setWechatNum(wechatNum);

        if (StringUtils.isNotBlank(nickname)) {
            user.setNickname(nickname);
        } else {
            user.setNickname("用户" + DesensitizationUtil.commonDisplay(mobile));
        }
        user.setRealName("");
        user.setSex(Sex.secret.type);
        user.setFace("");
        user.setFriendCircleBg("");
        user.setEmail("");
        user.setWechatNumImg("");

        user.setBirthday(LocalDateUtils.parseLocalDate("1980-01-01", LocalDateUtils.DATE_PATTERN));

        user.setCountry("中国");
        user.setProvince("");
        user.setCity("");
        user.setDistrict("");

        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());

        usersMapper.insert(user);

        return user;
    }
}
