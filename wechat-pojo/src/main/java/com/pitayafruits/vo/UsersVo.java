package com.pitayafruits.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.pitayafruits.utils.LocalDateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author cc
 * @since 2025-02-17
 */

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UsersVo {
    private String id;

    /**
     * 微信号
     */
    private String wechatNum;

    /**
     * 微信号二维码
     */
    private String wechatNumImg;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 性别，1:男 0:女 2:保密
     */
    private Integer sex;

    /**
     * 用户头像
     */
    private String face;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 生日
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(pattern = LocalDateUtils.DATE_PATTERN, timezone = LocalDateUtils.TIMEZONE_GMT8)
    private LocalDate birthday;

    /**
     * 国家
     */
    private String country;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 聊天背景
     */
    private String chatBg;

    /**
     * 朋友圈背景图
     */
    private String friendCircleBg;

    /**
     * 我的一句话签名
     */
    private String signature;

    /**
     * 创建时间
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = LocalDateUtils.DATETIME_PATTERN, timezone = LocalDateUtils.TIMEZONE_GMT8)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = LocalDateUtils.DATETIME_PATTERN, timezone = LocalDateUtils.TIMEZONE_GMT8)
    private LocalDateTime updatedTime;

    private String userToken;
}
