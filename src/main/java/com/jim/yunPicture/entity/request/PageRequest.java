package com.jim.yunPicture.entity.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description PageRequest
 */

@Data
@ApiModel(value = "分页请求")
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 7632360047937344414L;

    /**
     * 当前页
     */
    @ApiModelProperty(value = "当前页", dataType = "Integer")
    private int current = 1;

    /**
     * 页大小
     */
    @ApiModelProperty(value = "页大小", dataType = "Integer")
    private int pageSize = 10;

}