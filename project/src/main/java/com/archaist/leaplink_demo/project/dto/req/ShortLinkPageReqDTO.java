package com.archaist.leaplink_demo.project.dto.req;

import com.archaist.leaplink_demo.project.dao.entity.ShortLinkDO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * 短链接分页请求参数
 */
@Data
public class ShortLinkPageReqDTO extends Page<ShortLinkDO> {

    /**
     * 分组标识
     */
    private String gid;
}
