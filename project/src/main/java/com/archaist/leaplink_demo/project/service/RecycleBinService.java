package com.archaist.leaplink_demo.project.service;

import com.archaist.leaplink_demo.project.dao.entity.ShortLinkDO;
import com.archaist.leaplink_demo.project.dto.req.RecycleBinSaveReqDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 回收站管理接口层
 */
public interface RecycleBinService extends IService<ShortLinkDO> {

    /**
     * 保存回收站
     *
     * @param requestParam 请求参数
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);
}
