package com.jim.yun_picture.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Jim_Lam
 * @description UserVO
 */

@Data
public class UserVO implements Serializable {
    private static final long serialVersionUID = 6277514395052472116L;

    private Long id;

    /**
     * 昵称
     */
    private String username;

    /**
     * 登录账号
     */
    private String userAccount;

    /**
     * 登录头像
     */
    private String avatarUrl;

    /**
     * 性别：1男 0女
     */
    private Integer gender;

    /**
     * 空间ID
     */
    private Long spaceId;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 角色
     */
    private String userRole;

    /**
     * 个人简介
     */
    private String profile;
}