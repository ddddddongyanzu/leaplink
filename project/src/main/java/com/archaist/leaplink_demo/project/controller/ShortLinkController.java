package com.archaist.leaplink_demo.project.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.archaist.leaplink_demo.project.common.convention.result.Result;
import com.archaist.leaplink_demo.project.common.convention.result.Results;
import com.archaist.leaplink_demo.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.archaist.leaplink_demo.project.dto.req.ShortLinkCreateReqDTO;
import com.archaist.leaplink_demo.project.dto.req.ShortLinkPageReqDTO;
import com.archaist.leaplink_demo.project.dto.req.ShortLinkUpdateReqDTO;
import com.archaist.leaplink_demo.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.archaist.leaplink_demo.project.dto.resp.ShortLinkCreateRespDTO;
import com.archaist.leaplink_demo.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.archaist.leaplink_demo.project.dto.resp.ShortLinkPageRespDTO;
import com.archaist.leaplink_demo.project.handler.CustomBlockHandler;
import com.archaist.leaplink_demo.project.service.ShortLinkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
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
     * 短链接跳转原始链接
     */
    @GetMapping("/{short-uri}")
    public void restoreUrl(@PathVariable("short-uri") String shortUri, ServletRequest request, ServletResponse response) {
        shortLinkService.restoreUrl(shortUri, request, response);
    }

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/v1/create")
    @SentinelResource(
            value = "create_short-link",
            blockHandler = "createShortLinkBlockHandlerMethod",
            blockHandlerClass = CustomBlockHandler.class
    )
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return Results.success(shortLinkService.createShortLink(requestParam));
    }

    /**
     * 批量创建短链接
     */
    @PostMapping("/api/short-link/v1/create/batch")
    public Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam) {
        return Results.success(shortLinkService.batchCreateShortLink(requestParam));
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
