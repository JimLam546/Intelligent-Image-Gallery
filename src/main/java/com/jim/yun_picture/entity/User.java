package com.jim.yun_picture.entity;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.jim.yun_picture.entity.vo.UserVO;
import lombok.Data;

/**
 * 
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 生成长整型的id
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 密码
     */
    private String userPassword;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 空间ID
     */
    private Long spaceId;

    /**
     * 是否有效(是否被封号)
     */
    private Integer isValid;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 角色
     */
    private String userRole;

    /**
     * 个人简介
     */
    private String profile;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public static UserVO objToVO(User user) {
        return BeanUtil.copyProperties(user, UserVO.class);
    }
}