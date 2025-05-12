package com.jim.yunPicture.entity.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description DeleteRequest
 */

@Data
@ApiModel(value = "删除请求")
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = 1475158394333248543L;

    @ApiModelProperty(value = "ID", required = true)
    private Long id;

    @ApiModelProperty(value = "空间ID")
    private Long spaceId;
}