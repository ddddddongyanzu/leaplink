package com.archaist.leaplink_demo.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.archaist.leaplink_demo.admin.common.biz.user.UserContext;
import com.archaist.leaplink_demo.admin.common.convention.exception.ServiceException;
import com.archaist.leaplink_demo.admin.common.convention.result.Result;
import com.archaist.leaplink_demo.admin.dao.entity.GroupDO;
import com.archaist.leaplink_demo.admin.dao.mapper.GroupMapper;
import com.archaist.leaplink_demo.admin.remote.dto.ShortLinkRemoteService;
import com.archaist.leaplink_demo.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.archaist.leaplink_demo.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.archaist.leaplink_demo.admin.service.RecycleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * URL 回收站接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleServiceImpl implements RecycleService {

    private final GroupMapper groupMapper;

    /**
     * 后续重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    @Override
    public Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOList = groupMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(groupDOList)) {
            throw new ServiceException("用户无分组信息");
        }
        requestParam.setGidList(groupDOList.stream().map(GroupDO::getGid).toList());
        return shortLinkRemoteService.pageRecycleBinShortLink(requestParam);
    }
}
