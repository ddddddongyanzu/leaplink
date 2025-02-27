package com.archaist.leaplink_demo.project.service;

import com.archaist.leaplink_demo.project.dao.entity.ShortLinkDO;
import com.archaist.leaplink_demo.project.dto.req.RecycleBinRecoverReqDTO;
import com.archaist.leaplink_demo.project.dto.req.RecycleBinSaveReqDTO;
import com.archaist.leaplink_demo.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.archaist.leaplink_demo.project.dto.resp.ShortLinkPageRespDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

    /**
     * 分页查询短链接
     * @param requestParam 分页查询短链接请求参数
     * @return 短链接分页返回结果
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam);

    /**
     * 恢复短链接
     * @param requestParam 请求参数
     */
    void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam);
}
