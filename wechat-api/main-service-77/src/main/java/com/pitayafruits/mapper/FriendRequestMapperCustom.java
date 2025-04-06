package com.pitayafruits.mapper;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pitayafruits.pojo.vo.NewFriendsVO;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * <p>
 * 好友请求记录表 Mapper 接口
 * </p>
 *
 * @author 风间影月
 * @since 2024-03-27
 */
public interface FriendRequestMapperCustom {

    public Page<NewFriendsVO> queryNewFriendList(@Param("page") Page<NewFriendsVO> page,
                                                 @Param("paramMap") Map<String, Object> map);

}
