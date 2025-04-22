package com.jim.yunPicture.entity.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description UserDeleteRequest
 */

@Data
public class UserDeleteRequest implements Serializable {
    private static final long serialVersionUID = 8884144632688490530L;

    /**
     * 用户id
     */
    private Long id;
}