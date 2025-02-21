package com.archaist.leaplink_demo.admin.dto.req;

import lombok.Data;

@Data
public class UserLoginReqDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 怒那
     */
    private String password;
}
