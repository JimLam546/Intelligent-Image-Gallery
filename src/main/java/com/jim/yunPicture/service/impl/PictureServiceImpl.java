package com.jim.yunPicture.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jim.yunPicture.common.BaseResponse;
import com.jim.yunPicture.common.ResultUtil;
import com.jim.yunPicture.config.COSClientConfig;
import com.jim.yunPicture.entity.Picture;
import com.jim.yunPicture.entity.Space;
import com.jim.yunPicture.entity.User;
import com.jim.yunPicture.entity.dto.PictureUploadResult;
import com.jim.yunPicture.entity.enums.PictureReviewStatusEnum;
import com.jim.yunPicture.entity.request.DeleteRequest;
import com.jim.yunPicture.entity.request.PictureQueryRequest;
import com.jim.yunPicture.entity.request.PictureUploadByBatchRequest;
import com.jim.yunPicture.entity.request.PictureUploadRequest;
import com.jim.yunPicture.entity.vo.PictureVO;
import com.jim.yunPicture.entity.vo.UserVO;
import com.jim.yunPicture.exception.BusinessException;
import com.jim.yunPicture.exception.ErrorCode;
import com.jim.yunPicture.exception.ThrowUtils;
import com.jim.yunPicture.manage.COSManager;
import com.jim.yunPicture.manage.upload.FilePictureUpload;
import com.jim.yunPicture.manage.upload.PictureUploadTemplate;
import com.jim.yunPicture.manage.upload.UrlPictureUpload;
import com.jim.yunPicture.service.PictureService;
import com.jim.yunPicture.mapper.PictureMapper;
import com.jim.yunPicture.service.SpaceService;
import com.jim.yunPicture.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Jim_Lam
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-04-17 22:06:10
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UserService userService;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    private static final Cache<String, String> localCache = Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10000L)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    @Resource
    private SpaceService spaceService;

    @Resource
    private COSManager cosManager;

    @Resource
    private COSClientConfig cosClientConfig;

    @Resource
    private TransactionTemplate transactionTemplate;

    private static final ConcurrentHashMap<String, Object> lockMap = new ConcurrentHashMap<>();

    /**
     * 上传图片
     *
     * @return
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest uploadRequest, UserVO loginUser) {
        ThrowUtils.throwIf(inputSource == null, ErrorCode.PARAMS_ERROR);
        // 校验空间是否存在
        Long spaceId = uploadRequest.getSpaceId();
        if (ObjectUtil.isNotNull(spaceId)) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 检验额度
            ThrowUtils.throwIf(space.getTotalCount() >= space.getMaxCount(),
                    ErrorCode.OPERATION_ERROR, "空间文件数量已达上限");
            ThrowUtils.throwIf(space.getTotalSize() >= space.getMaxSize(),
                    ErrorCode.OPERATION_ERROR, "空间内存大小已达上限");
        }
        // 用于判断是新增还是修改
        Long pictureId = uploadRequest.getId();
        if (pictureId != null && pictureId > 0) {
            // 判断是否存在该图片Id的记录
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(ObjectUtil.isNull(oldPicture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 仅本人和管理员可以修改
            ThrowUtils.throwIf(ObjectUtil.notEqual(loginUser.getId(), oldPicture.getUserId())
                            && !userService.isAdmin(loginUser),
                    ErrorCode.OPERATION_ERROR, "无权限");
            // 判断更新是否换一个空间存放
            if (ObjectUtil.isNull(spaceId)) {
                // 判断原来记录是否有空间Id
                if (ObjectUtil.isNotNull(oldPicture.getSpaceId())) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                ThrowUtils.throwIf(ObjectUtil.notEqual(spaceId, oldPicture.getSpaceId()), ErrorCode.OPERATION_ERROR, "空间Id不匹配");
            }
        }
        // 默认为图片文件上传
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            // 设置为URL图片上传
            pictureUploadTemplate = urlPictureUpload;
        }
        String uploadPathPrefix = null;
        if (ObjectUtil.isNull(spaceId)) {
            uploadPathPrefix = String.format("/public/%s", loginUser.getId());
        } else {
            uploadPathPrefix = String.format("/private/%s", spaceId);
        }
        PictureUploadResult pictureUploadResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        Picture picture = PictureUploadResult.objToDO(pictureUploadResult, loginUser);
        // 填充审核状态
        fillReviewParam(picture, loginUser);
        // 如果ID存在，则表示更新记录
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        picture.setSpaceId(spaceId);
        if (ObjectUtil.isNotNull(spaceId)) {
            Space oldSpace = spaceService.getById(spaceId);
            // todo 判断空间大小是否足够放下
            Space space = new Space();
            space.setId(spaceId);
            space.setTotalCount(oldSpace.getTotalCount() + 1);
            space.setTotalSize(oldSpace.getTotalSize() + picture.getPicSize());
            spaceService.updateById(space);
        }

        boolean res = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "上传失败");
        // 上传图片，添加数据库记录
        return Picture.objToVO(picture);
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        ThrowUtils.throwIf(ObjectUtil.isNull(id), ErrorCode.PARAMS_ERROR, "图片ID不能为空");
        ThrowUtils.throwIf(url.length() > 512, ErrorCode.PARAMS_ERROR, "图片地址不能太长");
        ThrowUtils.throwIf(introduction.length() > 512, ErrorCode.PARAMS_ERROR, "图片介绍不能太长");
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        String tagsJSON = null;
        if (CollUtil.isNotEmpty(pictureQueryRequest.getTags())) {
            tagsJSON = JSONUtil.toJsonStr(pictureQueryRequest.getTags());
        }
        queryWrapper.lambda().and(ObjectUtil.isNotNull(pictureQueryRequest.getSearchText()),
                        pictureLambdaQueryWrapper ->
                                // 模糊查询图片名称和图片简介
                                pictureLambdaQueryWrapper
                                        .like(Picture::getName, pictureQueryRequest.getSearchText())
                                        .or()
                                        .like(Picture::getIntroduction, pictureQueryRequest.getSearchText()))
                .like(ObjectUtil.isNotNull(pictureQueryRequest.getCategory()), Picture::getCategory, pictureQueryRequest.getCategory())
                .in(CharSequenceUtil.isNotBlank(tagsJSON), Picture::getTags, pictureQueryRequest.getTags())
                .eq(ObjectUtil.isNotNull(pictureQueryRequest.getPicWidth()), Picture::getPicWidth, pictureQueryRequest.getPicWidth())
                .eq(ObjectUtil.isNotNull(pictureQueryRequest.getPicHeight()), Picture::getPicHeight, pictureQueryRequest.getPicHeight())
                .eq(ObjectUtil.isNotNull(pictureQueryRequest.getPicScale()), Picture::getPicScale, pictureQueryRequest.getPicScale())
                .eq(ObjectUtil.isNotNull(pictureQueryRequest.getPicFormat()), Picture::getPicFormat, pictureQueryRequest.getPicFormat())
                .eq(ObjectUtil.isNotNull(pictureQueryRequest.getUserId()), Picture::getUserId, pictureQueryRequest.getUserId())
                .eq(ObjectUtil.isNotNull(pictureQueryRequest.getPicSize()), Picture::getPicSize, pictureQueryRequest.getPicSize());
        return queryWrapper;
    }


    public void fillReviewParam(Picture picture, UserVO loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动审核
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewMessage("管理员自动通过审核");
            picture.setReviewTime(new Date());
            picture.setReviewerId(loginUser.getId());
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, UserVO loginUser) {
        String searchText = pictureUploadByBatchRequest.getSearchText();
        ThrowUtils.throwIf(CharSequenceUtil.isBlank(searchText), ErrorCode.PARAMS_ERROR, "搜索文本不能为空");
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count <= 0, ErrorCode.PARAMS_ERROR, "图片数量不能小于等于0");
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "图片数量不能大于30");
        // 抓取的网址
        String fetchUrl = String.format("https://www.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document = null;
        try {
            document = Jsoup.connect(fetchUrl).get();

        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        Element imgpt = document.getElementsByClass("dgControl").first();
        ThrowUtils.throwIf(ObjectUtil.isNull(imgpt), ErrorCode.OPERATION_ERROR, "获取页面失败");
        Elements elements = imgpt.select("img.mimg");
        int uploadCount = 0;
        for (Element element : elements) {
            String src = element.attr("src");
            if (CharSequenceUtil.isBlank(src)) {
                log.info("当前图片链接为空，已跳过:{}", src);
                continue;
            }
            src = src.contains("?") ? src.substring(0, src.indexOf("?")) : src;
            try {
                PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                pictureUploadRequest.setFileUrl(src);
                this.uploadPicture(src, pictureUploadRequest, loginUser);
                log.info("图片上传成功，图片路径为：{}", src);
                uploadCount++;
                if (uploadCount >= count) {
                    break;
                }
            } catch (Exception e) {
                log.error("图片上传失败", e);
            }
        }
        return uploadCount;
    }

    @Override
    public BaseResponse<Page<PictureVO>> getPictureVOListByPageWithCache(PictureQueryRequest pictureQueryRequest, String key) {
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        // 本地缓存
        String localCacheValue = localCache.getIfPresent(key);
        Page<PictureVO> localCachedPage = null;
        if (ObjectUtil.isNotNull(localCacheValue)) {
            localCachedPage = JSONUtil.toBean(localCacheValue, new TypeReference<Page<PictureVO>>() {
            }.getType(), true);
            return ResultUtil.success(localCachedPage);
        }
        // redis 缓存
        String cachedValue = redisTemplate.opsForValue().get(key);
        Page<PictureVO> cachedPage = null;
        if (ObjectUtil.isNotNull(cachedValue)) {
            cachedPage = JSONUtil.toBean(cachedValue, new TypeReference<Page<PictureVO>>() {
            }.getType(), true);
            // redis 如果存在那么就写入本地缓存
            localCache.put(key, cachedValue);
            return ResultUtil.success(cachedPage);
        }
        // 防止缓存击穿
        lockMap.putIfAbsent(key, new Object());
        synchronized (lockMap.get(key)) {
            try {
                String cachedPageStr = redisTemplate.opsForValue().get(key);
                String localCachedPageStr = localCache.getIfPresent(key);
                // 如果两者中其中一个为空，就进行查询
                if (ObjectUtil.isNotNull(cachedPageStr) && ObjectUtil.isNotNull(localCachedPageStr)) {
                    return ResultUtil.success(localCachedPage);
                }
                Page<Picture> page = new Page<>(current, pageSize);
                QueryWrapper<Picture> queryWrapper = this.getQueryWrapper(pictureQueryRequest);
                // 默认展示审核通过数据
                queryWrapper.lambda().eq(Picture::getReviewStatus, PictureReviewStatusEnum.PASS.getValue())
                        .isNull(Picture::getSpaceId);
                Page<Picture> picturePage = this.page(page, queryWrapper);
                List<PictureVO> pictureVOList = picturePage.getRecords().stream().map(picture -> {
                    // 返回压缩后的图片Url
                    PictureVO pictureVO = Picture.objToVO(picture);
                    if (ObjectUtil.isNotNull(picture.getThumbnailUrl())) {
                        pictureVO.setUrl(picture.getThumbnailUrl());
                    }
                    return pictureVO;
                }).collect(Collectors.toList());
                Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
                // 根据创建用户id查询用户信息
                Set<Long> idList = pictureVOList.stream().map(PictureVO::getUserId).collect(Collectors.toSet());
                Map<Long, List<UserVO>> idListMap = userService.listByIds(idList).stream().map(User::objToVO).collect(Collectors.groupingBy(UserVO::getId));
                pictureVOList.forEach(pictureVO -> pictureVO.setUser(idListMap.get(pictureVO.getUserId()).get(0)));
                pictureVOPage.setRecords(pictureVOList);
                // 设置随机过期时间
                int randomSeconds = 300 + RandomUtil.randomInt(0, 300);
                String pageJsonStr = JSONUtil.toJsonStr(pictureVOPage);
                // 将查询结果写入 redis 和 caffeine
                redisTemplate.opsForValue().set(key, pageJsonStr, randomSeconds, TimeUnit.SECONDS);
                localCache.put(key, pageJsonStr);
                return ResultUtil.success(pictureVOPage);
            } finally {
                lockMap.remove(key);
            }
        }
    }

    @Override
    public Picture deletePicture(DeleteRequest deleteRequest, UserVO loginUser) {
        // 检验参数
        Long pictureId = deleteRequest.getId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        // 检验图片是否存在
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "该图片不存在");
        // 验证是否是管理员和图片用户
        this.checkPictureAuth(oldPicture, loginUser);
        transactionTemplate.execute(status -> {
            boolean res = this.removeById(pictureId);
            ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "删除失败");
            // 增加空间额度
            if (ObjectUtil.isNotNull(oldPicture.getSpaceId())) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, oldPicture.getSpaceId())
                        .setSql("totalSize = totalSize - " + oldPicture.getPicSize())
                        .setSql("totalCount = totalCount - 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "空间额度更新失败");
            }
            return true;
        });
        return oldPicture;
    }

    @Override
    public void checkPictureAuth(Picture picture, UserVO loginUser) {
        Long spaceId = picture.getSpaceId();
        if (ObjectUtil.isNull(spaceId)) {
            // 公共空间，只有本人和管理员能够删除
            ThrowUtils.throwIf(!userService.isAdmin(loginUser)
                    || ObjectUtil.equal(loginUser.getId(), picture.getUserId()), ErrorCode.NO_AUTH_ERROR, "无权限");
        } else {
            // 私有空间，只有本人能够删除
            ThrowUtils.throwIf(!ObjectUtil.equal(loginUser.getId(), picture.getUserId()), ErrorCode.NO_AUTH_ERROR, "无权限");
        }
    }

    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        Long count = this.lambdaQuery()
                .eq(Picture::getUrl, oldPicture.getUrl())
                .count();
        if (count > 1) return;
        // 删除原图
        cosManager.deleteObject(getPictureUrlKey(oldPicture.getUrl()));
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (CharSequenceUtil.isNotBlank(thumbnailUrl)) {
            // 删除缩略图
            cosManager.deleteObject(getPictureUrlKey(thumbnailUrl));
        }
        String compressedUrl = oldPicture.getCompressedUrl();
        if (CharSequenceUtil.isNotBlank(compressedUrl)) {
            // 删除压缩图
            cosManager.deleteObject(getPictureUrlKey(compressedUrl));
        }
    }

    private String getPictureUrlKey(String url) {
        int index = url.indexOf(cosClientConfig.getHost()) + 1 + cosClientConfig.getHost().length();
        return url.substring(index);
    }
}




