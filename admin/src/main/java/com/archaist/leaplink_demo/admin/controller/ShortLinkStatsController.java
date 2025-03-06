package com.archaist.leaplink_demo.admin.controller;

import com.archaist.leaplink_demo.admin.common.convention.result.Result;
import com.archaist.leaplink_demo.admin.remote.dto.ShortLinkRemoteService;
import com.archaist.leaplink_demo.admin.remote.dto.req.ShortLinkGroupStatsReqDTO;
import com.archaist.leaplink_demo.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.archaist.leaplink_demo.admin.remote.dto.req.ShortLinkStatsReqDTO;
import com.archaist.leaplink_demo.admin.remote.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.archaist.leaplink_demo.admin.remote.dto.resp.ShortLinkStatsRespDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    /**
     * 后续重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam) {
        return shortLinkRemoteService.oneShortLinkStats(requestParam);
    }

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/group")
    public Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        return shortLinkRemoteService.groupShortLinkStats(requestParam);
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/access-record")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return shortLinkRemoteService.shortLinkStatsAccessRecord(requestParam);
    }
}
