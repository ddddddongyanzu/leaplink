package com.archaist.leaplink_demo.project.service;

import com.archaist.leaplink_demo.project.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.archaist.leaplink_demo.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.archaist.leaplink_demo.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.archaist.leaplink_demo.project.dto.req.ShortLinkStatsReqDTO;
import com.archaist.leaplink_demo.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.archaist.leaplink_demo.project.dto.resp.ShortLinkStatsRespDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

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

    /**
     * 获取分组短链接监控数据
     *
     * @param requestParam 获取分组短链接监控数据入参
     * @return 分组短链接监控数据
     */
    ShortLinkStatsRespDTO groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam);

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     * @param requestParam 获取短链接监控访问记录数据入参
     * @return 访问记录监控数据
     */
    IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam);

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     * @param requestParam 获取分组短链接监控访问记录数据入参
     * @return 分组访问记录监控数据
     */
    IPage<ShortLinkStatsAccessRecordRespDTO> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam);
}