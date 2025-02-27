package com.archaist.leaplink_demo.project.controller;

import com.archaist.leaplink_demo.project.common.convention.result.Result;
import com.archaist.leaplink_demo.project.common.convention.result.Results;
import com.archaist.leaplink_demo.project.dto.req.RecycleBinSaveReqDTO;
import com.archaist.leaplink_demo.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站管理控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    /**
     * 保存回收站
     */
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        recycleBinService.saveRecycleBin(requestParam);
        return Results.success();
    }

}
