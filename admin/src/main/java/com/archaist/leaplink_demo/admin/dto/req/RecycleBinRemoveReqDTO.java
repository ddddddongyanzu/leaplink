package com.archaist.leaplink_demo.admin.dto.req;

import lombok.Data;

/**
 * 回收站移除实体
 */
@Data
public class RecycleBinRemoveReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 全部短链接
     */
    private String fullShortUrl;
}
