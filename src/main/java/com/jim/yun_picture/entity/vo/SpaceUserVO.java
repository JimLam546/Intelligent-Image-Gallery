package com.jim.yun_picture.entity.vo;

import cn.hutool.core.bean.BeanUtil;
import com.jim.yun_picture.entity.SpaceUser;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Jim_Lam
 * @description SpaceUserVO
 */

@Data
public class SpaceUserVO implements Serializable {
    private static final long serialVersionUID = 5754032844823903343L;

    /**
     * id
     */
    private Long id;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户角色信息
     */
    private UserVO user;

    /**
     * 空间信息
     */
    private SpaceVO space;

    /**
     * VO类转实体类
     *
     * @param spaceUserVO
     * @return
     */
    public static SpaceUser voToObj(SpaceUserVO spaceUserVO) {
        if (spaceUserVO == null) {
            return null;
        }
        return BeanUtil.copyProperties(spaceUserVO, SpaceUser.class);
    }

    /**
     * 实体类转VO类
     *
     * @param spaceUser
     * @return
     */
    public static SpaceUserVO objToVO(SpaceUser spaceUser) {
        if (spaceUser == null) {
            return null;
        }
        return BeanUtil.copyProperties(spaceUser, SpaceUserVO.class);
    }
}