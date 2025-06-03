package com.jim.yun_picture.controller;

import com.jim.yun_picture.common.BaseResponse;
import com.jim.yun_picture.common.ResultUtil;
import com.jim.yun_picture.entity.request.SpaceAnalyzeRequest;
import com.jim.yun_picture.entity.request.SpaceCategoryAnalyzeRequest;
import com.jim.yun_picture.entity.request.SpaceSizeAnalyzeRequest;
import com.jim.yun_picture.entity.request.SpaceTagAnalyzeRequest;
import com.jim.yun_picture.entity.response.SpaceAnalyzeResponse;
import com.jim.yun_picture.entity.response.SpaceCategoryAnalyzeResponse;
import com.jim.yun_picture.entity.response.SpaceSizeAnalyzeResponse;
import com.jim.yun_picture.entity.response.SpaceTagAnalyzeResponse;
import com.jim.yun_picture.entity.vo.UserVO;
import com.jim.yun_picture.service.SpaceAnalyzeService;
import com.jim.yun_picture.service.UserService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Jim_Lam
 * @description SpaceAnalyzeController
 */

@RestController
@RequestMapping("/spaceAnalyze")
@Api(tags = "空间分析模块")
public class SpaceAnalyzeController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;

    @PostMapping("/usage")
    public BaseResponse<SpaceAnalyzeResponse> getSpaceUsageAnalyze(@RequestBody SpaceAnalyzeRequest spaceAnalyzeRequest,
                                                                   HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        SpaceAnalyzeResponse spaceUsageAnalyze = spaceAnalyzeService.getSpaceUsageAnalyze(spaceAnalyzeRequest, loginUser);
        return ResultUtil.success(spaceUsageAnalyze);
    }

    /**
     * 统计分类空间使用情况
     * @param spaceCategoryAnalyzeRequest
     * @param request
     * @return
     */
    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(
            @RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
                                                                       HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        List<SpaceCategoryAnalyzeResponse> spaceCategoryAnalyzeResponse = spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser);
        return ResultUtil.success(spaceCategoryAnalyzeResponse);
    }

    /**
     * 统计空间的标签使用情况
     * @param request
     * @param spaceTagAnalyzeRequest
     * @return
     */
    @PostMapping("/tag")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(HttpServletRequest request,
                                                                          @RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest) {
        UserVO loginUser = userService.getLoginUser(request);
        List<SpaceTagAnalyzeResponse> spaceTagAnalyzeResponse = spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
        return ResultUtil.success(spaceTagAnalyzeResponse);
    }

    @PostMapping("/size")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(@RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest,
                                                                            HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        List<SpaceSizeAnalyzeResponse> spaceSizeAnalyzeResponse = spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
        return ResultUtil.success(spaceSizeAnalyzeResponse);
    }
}