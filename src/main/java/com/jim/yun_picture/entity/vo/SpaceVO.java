package com.jim.yun_picture.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Jim_Lam
 * @description UserVO
 */

@Data
public class SpaceVO implements Serializable {

    private static final long serialVersionUID = 8218313928319228958L;

    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    /**
     * 当前空间下图片的总大小
     */
    private Long totalSize;

    /**
     * 当前空间下的图片数量
     */
    private Long totalCount;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 权限列表
     */
    private List<String> permissionList;
}