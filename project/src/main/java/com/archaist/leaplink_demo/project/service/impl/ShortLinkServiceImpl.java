package com.archaist.leaplink_demo.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.archaist.leaplink_demo.project.common.constant.RedisKeyConstant;
import com.archaist.leaplink_demo.project.common.constant.ShortLinkConstant;
import com.archaist.leaplink_demo.project.common.convention.exception.ClientException;
import com.archaist.leaplink_demo.project.common.convention.exception.ServiceException;
import com.archaist.leaplink_demo.project.common.enums.ValidateTypeEnum;
import com.archaist.leaplink_demo.project.dao.entity.*;
import com.archaist.leaplink_demo.project.dao.mapper.*;
import com.archaist.leaplink_demo.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.archaist.leaplink_demo.project.dto.req.ShortLinkCreateReqDTO;
import com.archaist.leaplink_demo.project.dto.req.ShortLinkPageReqDTO;
import com.archaist.leaplink_demo.project.dto.req.ShortLinkUpdateReqDTO;
import com.archaist.leaplink_demo.project.dto.resp.*;
import com.archaist.leaplink_demo.project.service.ShortLinkService;
import com.archaist.leaplink_demo.project.toolkit.HashUtil;
import com.archaist.leaplink_demo.project.toolkit.LinkUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocalStatsMapper linkLocalStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;

    @Value("${short-link.stats.local.amap-key}")
    private String staticLocalAmapKey;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.create(createShortLinkDefaultDomain)
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(createShortLinkDefaultDomain)
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .totalPv(0)
                .totalUv(0)
                .totalUip(0)
                .fullShortUrl(fullShortUrl)
                .favicon(getFavicon(requestParam.getOriginUrl()))
                .build();
        ShortLinkGotoDO linkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParam.getGid())
                .build();
        try {
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(linkGotoDO);
        } catch (DuplicateKeyException ex) {
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl);
            ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
            if (hasShortLinkDO != null) {
                log.warn("短链接:{}重复入库", fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
        }
        stringRedisTemplate.opsForValue().set(
                String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestParam.getOriginUrl(),
                LinkUtil.getLinkCacheValidTime(requestParam.getValidDate()), TimeUnit.MILLISECONDS
        );
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    @Override
    public ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        List<String> originUrls = requestParam.getOriginUrls();
        List<String> describes = requestParam.getDescribes();
        List<ShortLinkBaseInfoRespDTO> result = new ArrayList<>();
        for (int i = 0; i < originUrls.size(); i++) {
            ShortLinkCreateReqDTO shortLinkCreateReqDTO = BeanUtil.toBean(requestParam, ShortLinkCreateReqDTO.class);
            shortLinkCreateReqDTO.setOriginUrl(originUrls.get(i));
            shortLinkCreateReqDTO.setDescribe(describes.get(i));
            try {
                ShortLinkCreateRespDTO shortLink = createShortLink(shortLinkCreateReqDTO);
                ShortLinkBaseInfoRespDTO linkBaseInfoRespDTO = ShortLinkBaseInfoRespDTO.builder()
                        .fullShortUrl(shortLink.getFullShortUrl())
                        .originUrl(shortLink.getOriginUrl())
                        .describe(describes.get(i))
                        .build();
                result.add(linkBaseInfoRespDTO);
            } catch (Throwable ex) {
                log.error("批量创建短链接失败，原始参数：{}", originUrls.get(i));
            }
        }
        return ShortLinkBatchCreateRespDTO.builder()
                .total(result.size())
                .baseLinkInfos(result)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {

        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDO == null) {
            throw new ClientException("短链接记录不存在");
        }
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(hasShortLinkDO.getDomain())
                .shortUri(hasShortLinkDO.getShortUri())
                .clickNum(hasShortLinkDO.getClickNum())
                .favicon(hasShortLinkDO.getFavicon())
                .createdType(hasShortLinkDO.getCreatedType())
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .describe(requestParam.getDescribe())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .build();
        if (Objects.equals(hasShortLinkDO.getGid(), requestParam.getGid())) {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), ValidateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            baseMapper.update(shortLinkDO, updateWrapper);
        } else {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, hasShortLinkDO.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            baseMapper.delete(updateWrapper);
            baseMapper.insert(shortLinkDO);
        }
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        IPage<ShortLinkDO> resultPage = baseMapper.pageLink(requestParam);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkDOList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOList, ShortLinkGroupCountQueryRespDTO.class);
    }

    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();
        String serverPort = Optional.of(request.getServerPort())
                .filter(each -> !Objects.equals(each, 80))
                .map(String::valueOf)
                .map(each -> ":" + each)
                .orElse("");
        String fullShortUrl = serverName + serverPort + "/" + shortUri;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            shortLinkStats(fullShortUrl, null, request, response);
            ((HttpServletResponse)response).sendRedirect(originalLink);
            return;
        }
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            ((HttpServletResponse)response).sendRedirect("/page/notfound");
            return;
        }
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            ((HttpServletResponse)response).sendRedirect("/page/notfound");
            return;
        }
        RLock lock = redissonClient.getLock(String.format(RedisKeyConstant.LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            originalLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalLink)) {
                shortLinkStats(fullShortUrl, null, request, response);
                ((HttpServletResponse)response).sendRedirect(originalLink);
                return;
            }
            LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
            if (shortLinkGotoDO == null) {
                stringRedisTemplate.opsForValue().set(String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                ((HttpServletResponse)response).sendRedirect("/page/notfound");
                return;
            }
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if (shortLinkDO == null || shortLinkDO.getValidDate().before(new Date())) {
                stringRedisTemplate.opsForValue().set(String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                ((HttpServletResponse)response).sendRedirect("/page/notfound");
                return;
            }
            stringRedisTemplate.opsForValue().set(
                    String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDO.getOriginUrl(),
                    LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()), TimeUnit.MILLISECONDS
            );
            shortLinkStats(fullShortUrl, shortLinkDO.getGid(), request, response);
            ((HttpServletResponse)response).sendRedirect(shortLinkDO.getOriginUrl());
        } finally {
            lock.unlock();
        }
    }

    private void shortLinkStats(String fullShortUrl, String gid, ServletRequest request, ServletResponse response) {
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        try {
            AtomicReference<String> uv = new AtomicReference<>();
            Runnable addResponseCookieTask = () -> {
                uv.set(UUID.fastUUID().toString());
                Cookie uvCookie = new Cookie("uv", uv.get());
                uvCookie.setMaxAge(60 * 60 * 24 *30);
                uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
                ((HttpServletResponse)response).addCookie(uvCookie);
                uvFirstFlag.set(Boolean.TRUE);
                stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, uv.get());
            };
            if (ArrayUtil.isNotEmpty(cookies)) {
                Arrays.stream(cookies)
                        .filter(each -> Objects.equals(each.getName(), "uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .ifPresentOrElse(each -> {
                            uv.set(each);
                            Long uvAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, each);
                            uvFirstFlag.set(uvAdded != null && uvAdded > 0);
                        }, addResponseCookieTask);
            } else {
                addResponseCookieTask.run();
            }
            String remoteAddr = LinkUtil.getActualIp((HttpServletRequest) request);
            Long uipAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uip:" + fullShortUrl, remoteAddr);
            boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;
            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
                gid = shortLinkGotoDO.getGid();
            }
            int hour = DateUtil.hour(new Date(), true);
            Week week = DateUtil.dayOfWeekEnum(new Date());
            int weekValue = week.getIso8601Value();
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipFirstFlag ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
            Map<String, Object> localParamMap = new HashMap<>();
            localParamMap.put("key", staticLocalAmapKey);
            localParamMap.put("ip", remoteAddr);
            String localResultStr = HttpUtil.get(ShortLinkConstant.AMAP_REMOTE_URL, localParamMap);
            JSONObject localResultObject = JSON.parseObject(localResultStr);
            String infoCode = localResultObject.getString("infocode");
            LinkLocalStatsDO linkLocalStatsDO;
            String actualProvince;
            String actualCity;
            if (StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode, "10000")) {
                String province = localResultObject.getString("province");
                boolean unknownFlag = StrUtil.equals(province, "[]");
                linkLocalStatsDO = LinkLocalStatsDO.builder()
                        .province(actualProvince = unknownFlag ? "未知" : province)
                        .city(actualCity = unknownFlag ? "未知" : localResultObject.getString("city"))
                        .adcode(unknownFlag ? "未知" : localResultObject.getString("adcode"))
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .country("中国")
                        .gid(gid)
                        .date(new Date())
                        .build();
                linkLocalStatsMapper.shortLinkLocalStats(linkLocalStatsDO);
                String os = LinkUtil.getOs((HttpServletRequest) request);
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .os(os)
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkOsStatsMapper.shortLinkOsStats(linkOsStatsDO);
                String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .browser(browser)
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkBrowserStatsMapper.shortLinkBrowserState(linkBrowserStatsDO);
                String device = LinkUtil.getDevice(((HttpServletRequest) request));
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .device(device)
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);
                String network = LinkUtil.getNetwork(((HttpServletRequest) request));
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .network(network)
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);
                LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                        .user(uv.get())
                        .ip(remoteAddr)
                        .browser(browser)
                        .os(os)
                        .network(network)
                        .device(device)
                        .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .build();
                linkAccessLogsMapper.insert(linkAccessLogsDO);
                // 短链接汇总统计
                baseMapper.incrementStats(gid, fullShortUrl, 1, uvFirstFlag.get() ? 1 : 0, uipFirstFlag ? 1 : 0);
                // 短链接今日汇总统计
                LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
                        .todayPv(1)
                        .todayUv(uvFirstFlag.get() ? 1 : 0)
                        .todayUip(uipFirstFlag ? 1 : 0)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkStatsTodayMapper.shortLinkTodayStats(linkStatsTodayDO);
            }
        } catch (Throwable ex) {
            log.error("短链接统计量异常", ex);
        }
    }

    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        int customGenerateCount = 0;
        String shortUri;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接频繁生成，请稍后再试");
            }
            String originUrl = requestParam.getOriginUrl();
            originUrl += System.currentTimeMillis();
            shortUri = HashUtil.hashToBase62(originUrl);
            if (!shortUriCreateCachePenetrationBloomFilter.contains(requestParam.getDomain() + "/" + shortUri)) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }

    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }
}
