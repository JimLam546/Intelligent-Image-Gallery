package com.jim.yun_picture.entity.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description UserAddRequest
 */

@Data
@ApiModel(value = "用户添加请求")
public class UserAddRequest implements Serializable {
    private static final long serialVersionUID = -5441877740951272660L;
    /**
     * 昵称
     */
    @ApiModelProperty(value = "昵称", required = true)
    private String username;

    /**
     * 登录账号
     */
    @ApiModelProperty(value = "登录账号", required = true)
    private String userAccount;

    /**
     * 登录头像
     */
    @ApiModelProperty(value = "登录头像")
    private String avatarUrl;

    /**
     * 性别：1男 0女
     */
    @ApiModelProperty(value = "性别：1男 0女")
    private Integer gender;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码", required = true)
    private String userPassword;

    /**
     * 电话
     */
    @ApiModelProperty(value = "电话")
    private String phone;

    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱")
    private String email;

    /**
     * 角色
     */
    @ApiModelProperty(value = "角色")
    private String userRole;

    /**
     * 个人简介
     */
    @ApiModelProperty(value = "个人简介")
    private String profile;

}