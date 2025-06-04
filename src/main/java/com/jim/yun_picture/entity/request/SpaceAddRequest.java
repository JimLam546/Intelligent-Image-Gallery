package com.jim.yun_picture.entity.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description SpaceAddRequest
 */

@Data
public class SpaceAddRequest implements Serializable {
    private static final long serialVersionUID = -161318173949996245L;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间类型：0-个人空间 1-团队空间
     */
    private int spaceType;
}