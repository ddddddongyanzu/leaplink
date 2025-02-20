package com.archaist.leaplink_demo.admin.service.impl;

import com.archaist.leaplink_demo.admin.common.convention.exception.ClientException;
import com.archaist.leaplink_demo.admin.common.enums.UserErrorCodeEnum;
import com.archaist.leaplink_demo.admin.dao.entity.UserDO;
import com.archaist.leaplink_demo.admin.dao.mapper.UserMapper;
import com.archaist.leaplink_demo.admin.dto.resp.UserRespDTO;
import com.archaist.leaplink_demo.admin.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 用户接口实现层
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO, result);
        return result;
    }
}
