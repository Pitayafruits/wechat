package com.pitayafruits.service;

import com.pitayafruits.pojo.Users;
import com.pitayafruits.pojo.bo.ModifyUserBO;
import org.apache.catalina.User;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author cc
 * @since 2025-02-17
 */
public interface IUsersService {


    void modifyUserInfo(ModifyUserBO modifyUserBO);

    Users getById(String userId);

}
