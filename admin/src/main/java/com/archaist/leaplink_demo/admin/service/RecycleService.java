package com.archaist.leaplink_demo.admin.service;

import com.archaist.leaplink_demo.admin.common.convention.result.Result;
import com.archaist.leaplink_demo.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.archaist.leaplink_demo.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * URL 回收站接口层
 */
public interface RecycleService {

    /**
     * 分页查询回收站短链接
     * @param requestParam 请求参数
     * @return 返回参数包装
     */
    Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam);
}
