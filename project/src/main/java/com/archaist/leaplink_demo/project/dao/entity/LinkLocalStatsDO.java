package com.archaist.leaplink_demo.project.dao.entity;

import com.archaist.leaplink_demo.project.common.database.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 地区统计访问实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_local_stats")
public class LinkLocalStatsDO extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 日期
     */
    private Date date;

    /**
     * 访问量
     */
    private Integer cnt;

    /**
     * 省份名称
     */
    private String province;

    /**
     * 市名称
     */
    private String city;

    /**
     * 城市编码
     */
    private String adcode;

    /**
     * 国家标识
     */
    private String country;
}
