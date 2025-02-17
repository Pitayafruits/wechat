package com.pitayafruits.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pitayafruits.pojo.Users;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author cc
 * @since 2025-02-17
 */
public interface IUsersService {

    /**
     * 根据手机号查询用户是否存在
     * @param mobile
     * @return
     */
    public Users queryMobileIfExist(String mobile);

    /**
     * 创建用户信息
     * @param mobile
     * @return
     */
    public Users createUser(String mobile);

}
