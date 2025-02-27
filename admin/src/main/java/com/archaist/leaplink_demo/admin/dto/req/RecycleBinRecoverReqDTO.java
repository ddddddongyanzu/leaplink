package com.archaist.leaplink_demo.admin.dto.req;

import lombok.Data;

/**
 * 回收站恢复实体
 */
@Data
public class RecycleBinRecoverReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 全部短链接
     */
    private String fullShortUrl;
}
