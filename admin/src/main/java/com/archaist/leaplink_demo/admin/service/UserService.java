package com.archaist.leaplink_demo.admin.service;

import com.archaist.leaplink_demo.admin.dao.entity.UserDO;
import com.archaist.leaplink_demo.admin.dto.resp.UserRespDTO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户返回实体
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户是否存在
     * @param username 用户名
     * @return 用户名存在返回 True， 不存在返回 False
     */
    Boolean hasUsername(String username);
}
