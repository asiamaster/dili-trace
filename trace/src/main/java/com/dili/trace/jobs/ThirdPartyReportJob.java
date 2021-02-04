package com.dili.trace.jobs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.dili.assets.sdk.dto.CusCategoryQuery;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.trace.api.output.UserOutput;
import com.dili.trace.dao.RegisterBillMapper;
import com.dili.trace.dao.UserMapper;
import com.dili.trace.domain.Market;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.dto.ThirdPartyReportDataQueryDto;
import com.dili.trace.dto.thirdparty.report.CodeCountDto;
import com.dili.trace.dto.thirdparty.report.MarketCountDto;
import com.dili.trace.dto.thirdparty.report.RegionCountDto;
import com.dili.trace.dto.thirdparty.report.RegionCountInfo;
import com.dili.trace.dto.thirdparty.report.WaringInfoDto;
import com.dili.trace.glossary.UserQrStatusEnum;
import com.dili.trace.service.*;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import one.util.streamex.StreamEx;

@Component
public class ThirdPartyReportJob implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(ThirdPartyReportJob.class);
    @Autowired
    DataReportService dataReportService;

    @Autowired
    ThirdPartyReportDataService thirdPartyReportDataService;

    @Autowired
    UserService userService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    AssetsRpcService categoryService;

    @Autowired
    RegisterBillMapper registerBillMapper;

    @Autowired
    RegisterBillService registerBillService;

    @Autowired
    private MarketService marketService;

    @Override
    public void run(String... args) throws Exception {
        logger.info("===启动时刷新Token===");
//        this.execute();
    }

    // 每115分钟执行一次(token是两小时有效时间)
//    @Scheduled(fixedRate = 1000L * 60L * 115)
    public void execute() {
        logger.info("---开始刷新Token---");
        try {
            this.dataReportService.refreshToken(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("---结束刷新Token---");
    }

    // 每五分钟提交一次数据
//    @Scheduled(cron = "0 */5 * * * ?")
    public void reportData() {
        Optional<OperatorUser> optUser = Optional.of(new OperatorUser(-1L, "auto"));
        try {
            List<Market> marketList = marketService.listFromUap();
            for (Market market : marketList) {
                Long appId = market.getAppId();
                String appSecret = market.getAppSecret();
                String contextUrl = market.getContextUrl();
                if (appId != null && StringUtils.isNoneBlank(appSecret) && StringUtils.isNoneBlank(contextUrl)) {
                    this.marketCount(optUser, market);
                    this.regionCount(optUser, market);
                    this.reportCount(optUser, market);
                    this.codeCount(optUser, market);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    // 每天凌晨清理数据，防止历史数据太多
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanReportData() {
        logger.info("---开始清理数据---");
        LocalDateTime end = LocalDateTime.now().minusDays(11).withHour(23).withMinute(59).withSecond(59);
        String createdEnd = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        ThirdPartyReportDataQueryDto condition = new ThirdPartyReportDataQueryDto();
        condition.setCreatedEnd(createdEnd);
        try {
            this.thirdPartyReportDataService.deleteByExample(condition);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("---结束清理数据---");
    }

    public BaseOutput marketCount(Optional<OperatorUser> optUser, Market market) {
        Long marketId = market.getId();
        UserInfo query = new UserInfo();
        query.setMarketId(marketId);
        query.setYn(YesOrNoEnum.YES.getCode());
        Integer userCount = this.userService.countUser(query);

        CusCategoryQuery category = new CusCategoryQuery();
        //TODO
/*        category.setMarketId(marketId);
        Integer categoryCount = this.categoryService.count(category);*/
        Integer categoryCount=null;
        MarketCountDto marketCountDto = new MarketCountDto();
        marketCountDto.setPdtCount(userCount == null ? 0 : userCount);
        marketCountDto.setSubjectCount(categoryCount == null ? 0 : categoryCount);
        marketCountDto.setUpdateTime(new Date());

        return this.dataReportService.marketCount(marketCountDto, optUser, market);

    }

    public BaseOutput reportCount(Optional<OperatorUser> optUser, Integer checkBatch, Market market) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime= now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endDateTime = now;
        return this.dataReportService.reportCount(optUser,startDateTime,endDateTime, 0, market);

    }

    public BaseOutput reportCount(Optional<OperatorUser> optUser, Market market) {
        return this.reportCount(optUser, null, market);

    }

    public BaseOutput regionCount(Optional<OperatorUser> optUser, Market market) {

        RegisterBillDto billDto = new RegisterBillDto();
        billDto.setMarketId(market.getId());
        Date updateTime = new Date();
        List<RegionCountInfo> infoList = StreamEx.ofNullable(this.registerBillMapper.selectRegionCountData(billDto))
                .nonNull().flatCollection(Function.identity()).map(info -> {
                    info.setUpdateTime(updateTime);
                    return info;
                }).toList();

        RegionCountDto regionCountDto = new RegionCountDto();
        regionCountDto.setInfo(infoList);
        return this.dataReportService.regionCount(regionCountDto, optUser, market);

    }

    public BaseOutput codeCount(Optional<OperatorUser> optUser, Market market) {
        List<UserOutput> userOutputList = this.userMapper.groupByQrStatus(Lists.newArrayList(
                UserQrStatusEnum.GREEN.getCode(), UserQrStatusEnum.YELLOW.getCode(), UserQrStatusEnum.RED.getCode()), market.getId());
        Map<Integer, Integer> qrStatusMap = StreamEx.of(userOutputList).toMap(UserOutput::getQrStatus,
                UserOutput::getCnt);
        Date updateTime = new Date();
        CodeCountDto codeCountDto = new CodeCountDto();
        codeCountDto.setUpdateTime(updateTime);
        codeCountDto.setGreenCount(qrStatusMap.getOrDefault(UserQrStatusEnum.GREEN.getCode(), 0));
        codeCountDto.setYellowCount(qrStatusMap.getOrDefault(UserQrStatusEnum.YELLOW.getCode(), 0));
        codeCountDto.setRedCount(qrStatusMap.getOrDefault(UserQrStatusEnum.RED.getCode(), 0));

        UserInfo yellowQuery = new UserInfo();
        yellowQuery.setYn(YesOrNoEnum.YES.getCode());
        yellowQuery.setQrStatus(UserQrStatusEnum.YELLOW.getCode());
        yellowQuery.setMarketId(market.getId());

        UserInfo redQuery = new UserInfo();
        redQuery.setYn(YesOrNoEnum.YES.getCode());
        redQuery.setQrStatus(UserQrStatusEnum.RED.getCode());
        redQuery.setMarketId(market.getId());

        List<WaringInfoDto> warninfoList = StreamEx.of(this.userService.listByExample(yellowQuery))
                .append(this.userService.listByExample(redQuery)).map(u -> {

                    WaringInfoDto warn = new WaringInfoDto();
                    warn.setCodeStatus(
                            UserQrStatusEnum.fromCode(u.getQrStatus()).map(UserQrStatusEnum::getDesc).orElse(""));
                    warn.setStallNo(u.getTallyAreaNos());
                    warn.setSubjectName(u.getName());
                    warn.setUpdateTime(updateTime);
                    warn.setMarketId(u.getMarketId() == null ? "" : String.valueOf(u.getMarketId()));
                    warn.setMarketName(u.getMarketName());
                    return warn;
                }).toList();

        codeCountDto.setWarningInfo(warninfoList);
        return this.dataReportService.codeCount(codeCountDto, optUser, market);

    }

    // protected Date start(LocalDateTime now) {
    // Date start =
    // Date.from(now.minusDays(6).atZone(ZoneId.systemDefault()).toInstant());
    // return start;
    // }

    // protected Date end(LocalDateTime now) {
    // Date end = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
    // return end;
    // }

}