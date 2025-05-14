package com.jim.yun_picture.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.jim.yun_picture.annotation.AuthCheck;
import com.jim.yun_picture.common.BaseResponse;
import com.jim.yun_picture.common.RedisKey;
import com.jim.yun_picture.common.ResultUtil;
import com.jim.yun_picture.constant.UserConstant;
import com.jim.yun_picture.entity.Picture;
import com.jim.yun_picture.entity.enums.PictureReviewStatusEnum;
import com.jim.yun_picture.entity.request.*;
import com.jim.yun_picture.entity.vo.PictureVO;
import com.jim.yun_picture.entity.vo.UserVO;
import com.jim.yun_picture.exception.ErrorCode;
import com.jim.yun_picture.exception.ThrowUtils;
import com.jim.yun_picture.service.PictureService;
import com.jim.yun_picture.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author Jim_Lam
 * @description PictureController
 */

@RestController
@RequestMapping("/picture")
@Api(tags = "图片管理模块")
@ApiSupport(author = "JimLam")
public class PictureController {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    /**
     * 上传图片
     */
    @PostMapping("/upload")
    @ApiOperation(value = "上传图片")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile file, PictureUploadRequest uploadRequest, HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(file, uploadRequest, loginUser);
        return ResultUtil.success(pictureVO);
    }

    @PostMapping("/upload/url")
    @ApiOperation(value = "上传图片(URL)")
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtil.success(pictureVO);
    }

    @PostMapping("/upload/batch")
    @ApiOperation(value = "批量上传内容图片")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest, HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        Integer count = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtil.success(count);
    }

    /**
     * 删除图片
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除图片")
    public BaseResponse<Boolean> deleteById(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        Picture oldPicture = pictureService.deletePicture(deleteRequest, loginUser);
        // 异步删除云文件
        pictureService.clearPictureFile(oldPicture);
        return ResultUtil.success(true);
    }


    /**
     * 分页获取图片列表（管理员）
     *
     * @param pictureQueryRequest
     * @param request
     * @return 非脱密图片信息
     */
    @PostMapping("/get/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "分页获取图片列表（管理员）")
    public BaseResponse<Page<Picture>> getPictureListByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        // 注解已验证是否登录
        Page<Picture> page = new Page<>(pictureQueryRequest.getCurrent(), pictureQueryRequest.getPageSize());
        Page<Picture> picturePage = pictureService.page(page, pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtil.success(picturePage);
    }

    /**
     * 获取图片列表（非管理员）
     *
     * @param pictureQueryRequest
     * @param request
     * @return 返回脱敏图片信息列
     */
    // @PostMapping("/getVO/page")
    // @ApiOperation(value = "获取图片列表")
    // public BaseResponse<Page<PictureVO>> getPictureVOListByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
    //     // 注解已验证是否登录
    //     userService.getLoginUser(request);
    //     ThrowUtils.throwIf(pictureQueryRequest.getPageSize() > 100, ErrorCode.PARAMS_ERROR, "一页最大数量不能超过10");
    //     Page<Picture> page = new Page<>(pictureQueryRequest.getCurrent(), pictureQueryRequest.getPageSize());
    //     QueryWrapper<Picture> queryWrapper = pictureService.getQueryWrapper(pictureQueryRequest);
    //     // 默认展示审核通过数据
    //     queryWrapper.lambda().eq(Picture::getReviewStatus, PictureReviewStatusEnum.PASS.getValue());
    //     Page<Picture> picturePage = pictureService.page(page, queryWrapper);
    //     List<PictureVO> pictureVOList = picturePage.getRecords().stream().map(Picture::objToVO).collect(Collectors.toList());
    //     Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
    //     // 根据创建用户id查询用户信息
    //     Set<Long> idList = pictureVOList.stream().map(PictureVO::getUserId).collect(Collectors.toSet());
    //     Map<Long, List<UserVO>> idListMap = userService.listByIds(idList).stream().map(User::objToVO).collect(Collectors.groupingBy(UserVO::getId));
    //     pictureVOList.forEach(pictureVO -> pictureVO.setUser(idListMap.get(pictureVO.getUserId()).get(0)));
    //     pictureVOPage.setRecords(pictureVOList);
    //     return ResultUtil.success(pictureVOPage);
    // }


    @PostMapping("/getVO/page/cache")
    @ApiOperation(value = "获取图片列表")
    public BaseResponse<Page<PictureVO>> getPictureVOListByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        // 注解已验证是否登录
        ThrowUtils.throwIf(pictureQueryRequest.getPageSize() > 30, ErrorCode.PARAMS_ERROR, "一页最大数量不能超过30");
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String md5Hex = DigestUtil.md5Hex(queryCondition);
        String key = RedisKey.PICTURE_PAGE_PREFIX + md5Hex;
        // todo 处理缓存穿透
        // userService.getLoginUser(request);
        return pictureService.getPictureVOListByPageWithCache(pictureQueryRequest, key);
    }

    /**
     * 获取图片信息
     *
     * @param pictureQueryRequest
     * @param request
     * @return 返回脱敏图片信息
     */
    @PostMapping("/get/vo")
    @ApiOperation(value = "获取图片信息")
    public BaseResponse<PictureVO> getPictureVOById(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureQueryRequest.getId() == null || pictureQueryRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 验证是否登录
        Picture picture = pictureService.getById(pictureQueryRequest.getId());
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        Long spaceId = picture.getSpaceId();
        if (ObjectUtil.isNotNull(spaceId)) {
            UserVO loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(picture, loginUser);
        }
        PictureVO pictureVO = Picture.objToVO(picture);
        return ResultUtil.success(pictureVO);
    }

    /**
     * 获取图片信息（管理员）
     *
     * @param pictureQueryRequest
     * @param request
     * @return 返回非脱敏图片信息
     */
    @PostMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "获取图片信息（管理员）")
    public BaseResponse<Picture> getPictureById(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureQueryRequest.getId() == null || pictureQueryRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(pictureQueryRequest.getId());
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        return ResultUtil.success(picture);
    }

    /**
     * 修改图片信息（管理员）
     *
     * @param pictureUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "修改图片信息（管理员）")
    public BaseResponse<Boolean> updateById(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUpdateRequest.getId() == null || pictureUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        UserVO loginUser = userService.getLoginUser(request);
        Picture picture = pictureService.getById(pictureUpdateRequest.getId());
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        CopyOptions copyOptions = CopyOptions.create().setIgnoreProperties("tags");
        BeanUtil.copyProperties(pictureUpdateRequest, picture, copyOptions);
        String jsonStr = JSONUtil.toJsonStr(pictureUpdateRequest.getTags());
        picture.setTags(jsonStr);
        // 合法性检验
        pictureService.validPicture(picture);
        pictureService.fillReviewParam(picture, loginUser);
        boolean res = pictureService.updateById(picture);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "更新失败");
        return ResultUtil.success(true);
    }


    /**
     * 编辑图片信息
     *
     * @param pictureEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    @ApiOperation(value = "编辑图片信息")
    public BaseResponse<Boolean> editPictureById(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(pictureEditRequest.getId() == null || pictureEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = new Picture();
        CopyOptions copyOptions = CopyOptions.create().setIgnoreProperties("tags");
        BeanUtil.copyProperties(pictureEditRequest, picture, copyOptions);
        // 数据校验
        pictureService.validPicture(picture);
        picture.setEditTime(new Date());
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 填充审核参数
        pictureService.fillReviewParam(picture, loginUser);
        Picture oldPicture = pictureService.getById(pictureEditRequest.getId());
        ThrowUtils.throwIf(ObjectUtil.isNull(oldPicture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 判断是否为管理员或所属用户
        pictureService.checkPictureAuth(picture, loginUser);
        // 操作数据库
        boolean res = pictureService.updateById(picture);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "更新失败");
        return ResultUtil.success(true);
    }

    /**
     * 图片审核（管理员）
     *
     * @param pictureReviewRequest
     * @param request
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "图片审核（管理员）")
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        Long pictureId = pictureReviewRequest.getId();
        ThrowUtils.throwIf(ObjectUtil.isNull(pictureId), ErrorCode.NOT_FOUND_ERROR);
        // 检测审核参数
        PictureReviewStatusEnum enumByValue = PictureReviewStatusEnum.getEnumByValue(pictureReviewRequest.getReviewStatus());
        ThrowUtils.throwIf(ObjectUtil.isNull(enumByValue), ErrorCode.PARAMS_ERROR, "审核操作不存在");
        ThrowUtils.throwIf(ObjectUtil.equal(enumByValue, PictureReviewStatusEnum.REVIEWING), ErrorCode.PARAMS_ERROR, "请勿重复审核");
        // 更新审核状态
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, picture);
        picture.setReviewTime(new Date());
        picture.setReviewerId(loginUser.getId());
        boolean res = pictureService.updateById(picture);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "审核失败");
        return ResultUtil.success(true);
    }

}