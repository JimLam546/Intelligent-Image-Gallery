package com.jim.yun_picture.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jim.yun_picture.entity.Picture;
import com.jim.yun_picture.entity.Space;
import com.jim.yun_picture.entity.request.SpaceAnalyzeRequest;
import com.jim.yun_picture.entity.request.SpaceCategoryAnalyzeRequest;
import com.jim.yun_picture.entity.request.SpaceSizeAnalyzeRequest;
import com.jim.yun_picture.entity.request.SpaceTagAnalyzeRequest;
import com.jim.yun_picture.entity.response.SpaceAnalyzeResponse;
import com.jim.yun_picture.entity.response.SpaceCategoryAnalyzeResponse;
import com.jim.yun_picture.entity.response.SpaceSizeAnalyzeResponse;
import com.jim.yun_picture.entity.response.SpaceTagAnalyzeResponse;
import com.jim.yun_picture.entity.vo.UserVO;
import com.jim.yun_picture.exception.BusinessException;
import com.jim.yun_picture.exception.ErrorCode;
import com.jim.yun_picture.exception.ThrowUtils;
import com.jim.yun_picture.service.PictureService;
import com.jim.yun_picture.service.SpaceAnalyzeService;
import com.jim.yun_picture.service.SpaceService;
import com.jim.yun_picture.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Jim_Lam
 * @description SpaceAnalyzeServiceImpl
 */

@Service
public class SpaceAnalyzeServiceImpl implements SpaceAnalyzeService {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Override
    public SpaceAnalyzeResponse getSpaceUsageAnalyze(SpaceAnalyzeRequest spaceAnalyzeRequest, UserVO userVO) {
        if (spaceAnalyzeRequest.isQueryPublic() || spaceAnalyzeRequest.isQueryAll()) {
            // 判断是否是管理员
            ThrowUtils.throwIf(!userService.isAdmin(userVO), ErrorCode.NO_AUTH_ERROR);
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("picSize");
            // 如果分析公共图库，那么就不存在spaceId
            queryWrapper.isNull(spaceAnalyzeRequest.isQueryPublic(), "spaceId");
            List<Object> pictureObjList = pictureService.getBaseMapper().selectObjs(queryWrapper);
            long usedSize = pictureObjList.stream().mapToLong(obj -> obj instanceof Long ? (Long) obj : 0).sum();
            long usedCount = pictureObjList.size();
            // 返回封装的结果
            SpaceAnalyzeResponse analyzeResponse = new SpaceAnalyzeResponse();
            analyzeResponse.setUsedSize(usedSize);
            analyzeResponse.setUsedCount(usedCount);
            // 公共图库没有上限
            analyzeResponse.setMaxCount(null);
            analyzeResponse.setMaxSize(null);
            analyzeResponse.setSpaceCountRatio(null);
            analyzeResponse.setUsedSizeRatio(null);
            return analyzeResponse;
        } else {
            // 空间分析
            Space space = checkAuth(spaceAnalyzeRequest, userVO);

            // 封装返回结果
            SpaceAnalyzeResponse analyzeResponse = new SpaceAnalyzeResponse();
            analyzeResponse.setMaxSize(space.getMaxSize());
            analyzeResponse.setMaxCount(space.getMaxCount());
            analyzeResponse.setUsedSize(space.getTotalSize());
            analyzeResponse.setUsedCount(space.getTotalCount());
            analyzeResponse.setUsedSizeRatio(NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue());
            analyzeResponse.setSpaceCountRatio(NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue());
            return analyzeResponse;
        }
    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, UserVO loginUser) {
        // 校验是否为管理员或本人空间
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 根据条件填充分析的查询条件
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);
        queryWrapper.select("category", "count(*) as count", "sum(picSize) as totalSize")
                .groupBy("category");
        return pictureService.getBaseMapper().selectMaps(queryWrapper).stream().map(result -> {
            String category = result.get("category") == null ? "未分类" : result.get("category").toString();
            Long count = ((Number) result.get("count")).longValue();
            Long totalSize = ((Number) result.get("totalSize")).longValue();
            return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
        }).collect(Collectors.toList());
    }

    public void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        if (spaceAnalyzeRequest.isQueryAll()) return;
        if (spaceAnalyzeRequest.isQueryPublic()) {
            queryWrapper.isNull("spaceId");
            return;
        }
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (ObjectUtil.isNotNull(spaceId)) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有指定查询范围!");
    }

    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, UserVO loginUser) {
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);
        queryWrapper.select("tags");
        // 获取所有的标签
        List<String> tags = pictureService.getBaseMapper().selectObjs(queryWrapper).stream()
                .filter(ObjectUtil::isNotNull)
                .map(Object::toString)
                .collect(Collectors.toList());
        // 统计所有标签出现的次数
        Map<String, Long> tagNum = tags.stream().flatMap(tag -> JSONUtil.toList(tag, String.class).stream()).collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));
        // 转换为响应对象，排序并返回
        return tagNum.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, UserVO loginUser) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);

        // 查询所有图片的大小
        queryWrapper.select("picSize");
        List<Long> pictureSizes = pictureService.getBaseMapper().selectObjs(queryWrapper).stream()
                .map(size -> ((Number) size).longValue())
                .collect(Collectors.toList());

        LinkedHashMap<String, Long> sizeMap = new LinkedHashMap<>();
        sizeMap.put("0-100KB", pictureSizes.stream().filter(size -> size <= 100 * 1024).count());
        sizeMap.put("100-500KB", pictureSizes.stream().filter(size -> size > 100 * 1024 && size <= 500 * 1024).count());
        sizeMap.put("500KB-1MB", pictureSizes.stream().filter(size -> size > 500 * 1024 && size <= 1024 * 1024).count());
        sizeMap.put(">1MB", pictureSizes.stream().filter(size -> size > 1024 * 1024).count());

        return sizeMap.entrySet().stream()
                .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, UserVO loginUser) {
        if (spaceAnalyzeRequest.isQueryPublic() || spaceAnalyzeRequest.isQueryAll()) {
            // 检查是否是管理员
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
        } else {
            checkAuth(spaceAnalyzeRequest, loginUser);
        }
    }

    private Space checkAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, UserVO userVO) {
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceId), ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(spaceId);
        // 判断空间是否存在
        ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.NOT_FOUND_ERROR);
        // 只有空间主人可以获取分析结果
        ThrowUtils.throwIf(ObjectUtil.notEqual(userVO.getId(), space.getUserId()), ErrorCode.NO_AUTH_ERROR);
        return space;
    }
}