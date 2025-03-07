package com.archaist.leaplink_demo.project.service.impl;

import com.archaist.leaplink_demo.project.dao.entity.LinkStatsTodayDO;
import com.archaist.leaplink_demo.project.dao.mapper.LinkStatsTodayMapper;
import com.archaist.leaplink_demo.project.service.LinkStatsTodayService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 短链接今日统计接口实现层
 */
@Service
public class LinkStatsTodayServiceImpl extends ServiceImpl<LinkStatsTodayMapper, LinkStatsTodayDO> implements LinkStatsTodayService {
}