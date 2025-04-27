package com.jim.yunPicture.entity.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description PictureUploadByBatchRequest
 */

@Data
public class PictureUploadByBatchRequest implements Serializable {
    private static final long serialVersionUID = 4215422567233396986L;

    /**
     * 搜索内容
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer count = 10;
}