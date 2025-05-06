package com.jim.yunPicture.entity.request;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.jim.yunPicture.entity.Space;
import com.jim.yunPicture.entity.vo.SpaceVO;
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


}