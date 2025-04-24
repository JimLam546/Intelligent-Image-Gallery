package com.jim.yunPicture.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.jim.yunPicture.annotation.AuthCheck;
import com.jim.yunPicture.common.BaseResponse;
import com.jim.yunPicture.common.ResultUtil;
import com.jim.yunPicture.constant.UserConstant;
import com.jim.yunPicture.entity.Picture;
import com.jim.yunPicture.entity.enums.PictureReviewStatusEnum;
import com.jim.yunPicture.entity.enums.UserRoleEnum;
import com.jim.yunPicture.entity.request.*;
import com.jim.yunPicture.entity.vo.PictureVO;
import com.jim.yunPicture.entity.vo.UserVO;
import com.jim.yunPicture.exception.ErrorCode;
import com.jim.yunPicture.exception.ThrowUtils;
import com.jim.yunPicture.service.PictureService;
import com.jim.yunPicture.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        ThrowUtils.throwIf(deleteRequest.getId() == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        UserVO loginUser = userService.getLoginUser(request);
        Picture picture = pictureService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 验证是否是管理员和图片用户
        ThrowUtils.throwIf(!userService.isAdmin(loginUser) &&
                        !Objects.equals(picture.getUserId(), loginUser.getId())
                , ErrorCode.NO_AUTH_ERROR, "无操作权限");
        boolean res = pictureService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "删除失败");
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
    @PostMapping("/getVO/page")
    @ApiOperation(value = "获取图片列表")
    public BaseResponse<Page<PictureVO>> getPictureVOListByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        // 注解已验证是否登录
        userService.getLoginUser(request);
        ThrowUtils.throwIf(pictureQueryRequest.getPageSize() > 100, ErrorCode.PARAMS_ERROR, "一页最大数量不能超过10");
        Page<Picture> page = new Page<>(pictureQueryRequest.getCurrent(), pictureQueryRequest.getPageSize());
        QueryWrapper<Picture> queryWrapper = pictureService.getQueryWrapper(pictureQueryRequest);
        // 默认展示审核通过数据
        queryWrapper.lambda().eq(Picture::getReviewStatus, PictureReviewStatusEnum.PASS.getValue());
        Page<Picture> picturePage = pictureService.page(page, queryWrapper);
        List<PictureVO> pictureVOList = picturePage.getRecords().stream().map(Picture::objToVO).collect(Collectors.toList());
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        // todo 根据创建用户id查询用户信息
        pictureVOPage.setRecords(pictureVOList);
        return ResultUtil.success(pictureVOPage);
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
        userService.getLoginUser(request);
        Picture picture = pictureService.getById(pictureQueryRequest.getId());
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
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
        ThrowUtils.throwIf(pictureEditRequest.getId() == null || pictureEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        UserVO loginUser = userService.getLoginUser(request);
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
        ThrowUtils.throwIf(
                ObjectUtil.notEqual(UserRoleEnum.ADMIN.getValue(), loginUser.getUserRole())
                        && ObjectUtil.notEqual(loginUser.getId(), oldPicture.getUserId()),
                ErrorCode.OPERATION_ERROR, "无权限");
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