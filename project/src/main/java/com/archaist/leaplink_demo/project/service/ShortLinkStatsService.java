package com.archaist.leaplink_demo.project.service;

import com.archaist.leaplink_demo.project.dto.req.ShortLinkStatsReqDTO;
import com.archaist.leaplink_demo.project.dto.resp.ShortLinkStatsRespDTO;

/**
 * 短链接监控接口层
 */
public interface ShortLinkStatsService {

    /**
     * 获取单个短链接监控数据
     *
     * @param requestParam 获取短链接监控数据入参
     * @return 短链接监控数据
     */
    ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam);
}