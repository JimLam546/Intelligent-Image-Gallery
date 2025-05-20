package com.jim.yun_picture.entity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.jim.yun_picture.entity.vo.PictureVO;
import lombok.Data;

/**
 * 图片
 * @TableName picture
 */
@TableName(value ="picture")
@Data
public class Picture {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 图片 url
     */
    private String url;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（JSON 数组）
     */
    private String tags;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

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
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 被压缩的图片 url
     */
    private String compressedUrl;

    /**
     * 缩略图片 url
     */
    private String thumbnailUrl;

    /**
     * 存储空间id
     */
    private Long spaceId;


    /**
     * 对象转换为 VO
     * @param picture
     * @return
     */
    public static PictureVO objToVO(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVO pictureVO = BeanUtil.copyProperties(picture, PictureVO.class);
        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVO;
    }

    public static Picture VOToObj(PictureVO pictureVO) {
        if (pictureVO == null) {
            return null;
        }
        Picture picture = BeanUtil.copyProperties(pictureVO, Picture.class);
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return picture;
    }
}