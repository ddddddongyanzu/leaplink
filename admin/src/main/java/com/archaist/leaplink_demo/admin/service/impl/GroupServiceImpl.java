package com.archaist.leaplink_demo.admin.service.impl;

import com.archaist.leaplink_demo.admin.dao.entity.GroupDO;
import com.archaist.leaplink_demo.admin.dao.mapper.GroupMapper;
import com.archaist.leaplink_demo.admin.service.GroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短链接分组接口实现层
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
}
