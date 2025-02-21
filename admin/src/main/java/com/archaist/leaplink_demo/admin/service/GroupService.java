package com.archaist.leaplink_demo.admin.service;

import com.archaist.leaplink_demo.admin.dao.entity.GroupDO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增短链接分组
     * @param groupName 短链接分组名
     */
    void saveGroup(String groupName);
}
