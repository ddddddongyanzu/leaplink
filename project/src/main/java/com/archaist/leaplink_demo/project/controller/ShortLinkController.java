package com.archaist.leaplink_demo.project.controller;

import com.archaist.leaplink_demo.project.common.convention.result.Result;
import com.archaist.leaplink_demo.project.common.convention.result.Results;
import com.archaist.leaplink_demo.project.dto.req.ShortLinkCreateReqDTO;
import com.archaist.leaplink_demo.project.dto.req.ShortLinkPageReqDTO;
import com.archaist.leaplink_demo.project.dto.req.ShortLinkUpdateReqDTO;
import com.archaist.leaplink_demo.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.archaist.leaplink_demo.project.dto.resp.ShortLinkCreateRespDTO;
import com.archaist.leaplink_demo.project.dto.resp.ShortLinkPageRespDTO;
import com.archaist.leaplink_demo.project.service.ShortLinkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return Results.success(shortLinkService.createShortLink(requestParam));
    }

    /**
     * 修改短链接
     */
    @PostMapping("/api/short-link/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        shortLinkService.updateShortLink(requestParam);
        return Results.success();
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return Results.success(shortLinkService.pageShortLink(requestParam));
    }

    /**
     * 查询短链接分组内数量
     */
    @GetMapping("/api/short-link/v1/count")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam) {
        return Results.success(shortLinkService.listGroupShortLinkCount(requestParam));
    }
}
