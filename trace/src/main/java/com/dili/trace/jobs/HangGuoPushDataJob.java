package com.dili.trace.jobs;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.dto.IDTO;
import com.dili.ss.util.DateUtils;
import com.dili.trace.dao.RegisterBillMapper;
import com.dili.trace.domain.*;
import com.dili.trace.domain.Category;
import com.dili.trace.dto.DetectRecordParam;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.dto.PushDataQueryDto;
import com.dili.trace.dto.thirdparty.report.*;
import com.dili.trace.enums.*;
import com.dili.trace.glossary.EnabledStateEnum;
import com.dili.trace.glossary.UserTypeEnum;
import com.dili.trace.service.*;
import one.util.streamex.StreamEx;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对接天下粮仓入口，通过定时任务每5min调用天下粮仓的接口
 *
 * @author asa.lee
 */
//@Component
public class HangGuoPushDataJob implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(HangGuoPushDataJob.class);
    @Autowired
    DataReportService dataReportService;
    @Autowired
    RegisterBillMapper registerBillMapper;
    @Autowired
    private MarketService marketService;
    @Autowired
    private ThirdPartyPushDataService thirdPartyPushDataService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private UserService userService;
    @Autowired
    private HangGuoDataService hangGuoDataService;
    @Autowired
    private ImageCertService imageCertService;
    @Autowired
    TradeRequestService tradeRequestService;
    @Autowired
    DetectRecordService detectRecordService;
    @Autowired
    SysConfigService sysConfigService;
    @Value("${current.baseWebPath}")
    private String baseWebPath;
    @Value("${push.batch.size}")
    private Integer pushBatchSize;

    @Override
    public void run(String... args) throws Exception {
        //pushHangGuoTradeData();
        // pushData();
    }

    private boolean isCallDataSwitch() {
        return sysConfigService.isCallDataSwitch(SysConfigTypeEnum.PUSH_DATA_SUBJECT.getCode(),SysConfigTypeEnum.PUSH_DATA_CATEGORY.getCode());
    }

    /**
     * 每五分钟提交一次数据
     */
    //@Scheduled(cron = "0 */5 * * * ?")
    public void pushData() {
        if(isCallDataSwitch()){
            if(logger.isInfoEnabled()){
                logger.info("=====>>>>>未配置上报开关，或上报开关已关闭");
            }
            return;
        }
        Optional<OperatorUser> optUser = Optional.of(new OperatorUser(-1L, "auto"));
        try {
            List<Market> marketList = marketService.listFromUap();
            Map<String, String> marketCodeMap = marketService.getMarketCodeMap();
            for (Market market : marketList) {
                Long appId = market.getAppId();
                String appSecret = market.getAppSecret();
                String contextUrl = market.getContextUrl();
                String marketCode = market.getCode();
                boolean isValidate = null != market.getAppId() && StringUtils.isNotBlank(contextUrl) && StringUtils.isNotBlank(appSecret);
                boolean isHangGuo = marketCode.equals(marketCodeMap.get(MarketEnum.HZSG.getCode())) && appId != null && StringUtils.isNoneBlank(appSecret) && StringUtils.isNoneBlank(contextUrl);
                if (isHangGuo && isValidate) {
                    Date endTime = this.registerBillMapper.selectCurrentTime();
                    // 商品上报
                    this.pushFruitsCategory(optUser, endTime, market);
                    // 经营户上报
                    this.pushFruitsUser(optUser, endTime, market);
                    //检测数据上报
                    this.pushFruitsInspectionData(optUser, endTime, market);
                    //不合格检测上报
                    //this.pushFruitsUnqualifiedInspectionData(optUser, endTime, market);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    //@Scheduled(cron = "0 */5 * * * ?")
    public void pushHangGuoTradeData() {
        if(isCallDataSwitch()){
            if(logger.isInfoEnabled()){
                logger.info("=====>>>>>未配置上报开关，或上报开关已关闭");
            }
            return;
        }
        Optional<OperatorUser> optUser = Optional.of(new OperatorUser(-1L, "auto"));
        try {
            List<Market> marketList = marketService.listFromUap();
            Map<String, String> marketCodeMap = marketService.getMarketCodeMap();
            for (Market market : marketList) {
                Long appId = market.getAppId();
                String appSecret = market.getAppSecret();
                String contextUrl = market.getContextUrl();
                String marketCode = market.getCode();
                boolean isValidate = null != market.getAppId() && StringUtils.isNotBlank(contextUrl) && StringUtils.isNotBlank(appSecret);
                boolean isHangGuo = marketCode.equals(marketCodeMap.get(MarketEnum.HZSG.getCode())) && appId != null && StringUtils.isNoneBlank(appSecret) && StringUtils.isNoneBlank(contextUrl);
                if (isHangGuo && isValidate) {
                    Date endTime = this.registerBillMapper.selectCurrentTime();
                    //交易单
                    this.pushFruitsTradeData(optUser, endTime, market);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private BaseOutput pushFruitsTradeData(Optional<OperatorUser> optUser, Date endTime, Market market) {
        Date updateTime = null;
        Date startTime = null;
        boolean newPushFlag = true;
        Long marketId = market.getId();
        Long platformMarketId = market.getPlatformMarketId();
        ThirdPartyPushData pushData = thirdPartyPushDataService.getThredPartyPushData(ReportInterfaceEnum.HANGGUO_TRADE.getCode(), marketId);

        if (pushData == null) {
            updateTime = endTime;
            pushData = new ThirdPartyPushData();
            pushData.setTableName(ReportInterfaceEnum.HANGGUO_TRADE.getCode());
            pushData.setInterfaceName(ReportInterfaceEnum.HANGGUO_TRADE.getName());
            pushData.setPushTime(endTime);
            pushData.setMarketId(marketId);
        } else {
            updateTime = pushData.getPushTime();
            startTime = pushData.getPushTime();
            newPushFlag = false;
        }

        List<ReportScanCodeOrderDto> partTrade = getFruitsTradeData(platformMarketId, startTime, endTime);
        if (CollectionUtils.isEmpty(partTrade)) {
            logger.info("Report Hangguo Unqualified Disposal IS NULL");
            return BaseOutput.success("Report Hangguo Unqualified Disposal IS NULL");
        }

        BaseOutput baseOutput = new BaseOutput();
        // 分批上报
        Integer batchSize = (pushBatchSize == null || pushBatchSize == 0) ? 64 : pushBatchSize;
        // 分批数
        Integer part = partTrade.size() / batchSize;
        for (int i = 0; i <= part; i++) {
            Integer endPos = i == part ? partTrade.size() : (i + 1) * batchSize;
            List<ReportScanCodeOrderDto> partBills = partTrade.subList(i * batchSize, endPos);
            baseOutput = this.dataReportService.reportScanCodeOrder(partBills, optUser, market);
        }

        if (baseOutput.isSuccess()) {
            this.thirdPartyPushDataService.updatePushTime(pushData, endTime);
        } else {
            logger.error("上报:{} 失败，原因:{}", ReportInterfaceEnum.HANGGUO_TRADE.getName(), baseOutput.getMessage());
        }
        return baseOutput;
    }

    private List<ReportScanCodeOrderDto> getFruitsTradeData(Long platformMarketId, Date startdTime, Date endTime) {
        PushDataQueryDto queryDto = new PushDataQueryDto();
        queryDto.setModifiedEnd(DateUtil.format(endTime, "yyyy-MM-dd HH:mm:ss"));
        if (startdTime != null) {
            queryDto.setModifiedStart(DateUtil.format(startdTime, "yyyy-MM-dd HH:mm:ss"));
        }
        List<String> requestIdList = new ArrayList<>();
        List<ReportScanCodeOrderDto> scanCodeOrderDtoList = StreamEx.of(hangGuoDataService.getHangGuoScanOrderReport(queryDto)).
                nonNull().map(order -> {
            order.setMarketId(String.valueOf(platformMarketId));
            order.setThirdQrCode(this.baseWebPath + "/user?userId=" + order.getThirdBuyId());
            requestIdList.add(order.getThirdOrderId());
            return order;
        }).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(scanCodeOrderDtoList)) {
            // 设置 detail
            Map<String, List<ReportOrderDetailDto>> detailMap = tradeRequestService.selectOrderDetailReport(requestIdList)
                    .stream().collect(Collectors.groupingBy(ReportOrderDetailDto::getRequestId));
            scanCodeOrderDtoList.forEach(order -> {
                List<ReportOrderDetailDto> dtos = detailMap.get(order.getThirdOrderId());
                dtos.forEach(d -> d.setPrice(order.getPrice()));
                order.setTradeList(dtos);
            });
        }
        return scanCodeOrderDtoList;
    }


    /*private BaseOutput pushFruitsUnqualifiedInspectionData(Optional<OperatorUser> optUser, Date endTime, Market market) {
        Date updateTime = null;
        Long marketId = market.getId();
        Long platformMarketId = market.getPlatformMarketId();
        ThirdPartyPushData pushData = thirdPartyPushDataService.getThredPartyPushData(ReportInterfaceEnum.HANGGUO_DISPOSE.getCode(), marketId);

        if (pushData == null) {
            updateTime = endTime;
            pushData = new ThirdPartyPushData();
            pushData.setTableName(ReportInterfaceEnum.HANGGUO_DISPOSE.getCode());
            pushData.setInterfaceName(ReportInterfaceEnum.HANGGUO_DISPOSE.getName());
            pushData.setPushTime(endTime);
            pushData.setMarketId(marketId);
        } else {
            updateTime = pushData.getPushTime();
        }
        List<ReportUnqualifiedDisposalDto> disposalDtos = getReportDisposeList(platformMarketId,updateTime);
        logger.info("Report Hangguo Unqualified Disposal Dto ： " + JSON.toJSONString(disposalDtos));
        if (CollectionUtils.isEmpty(disposalDtos)) {
            logger.info("Report Hangguo Unqualified Disposal IS NULL");
            return BaseOutput.success("Report Hangguo Unqualified Disposal IS NULL");
        }
        //BaseOutput baseOutput = this.dataReportService.reportFruitsUnqualifiedDisposal(disposalDtos, optUser, market);
        BaseOutput baseOutput = new BaseOutput();
        // 分批上报
        Integer batchSize = (pushBatchSize == null || pushBatchSize == 0) ? 64 : pushBatchSize;
        // 分批数
        Integer part = disposalDtos.size() / batchSize;
        for (int i = 0; i <= part; i++) {
            Integer endPos = i == part ? disposalDtos.size() : (i + 1) * batchSize;
            List<ReportUnqualifiedDisposalDto> partBills = disposalDtos.subList(i * batchSize, endPos);
            baseOutput = this.dataReportService.reportFruitsUnqualifiedDisposal(partBills, optUser, market);
        }

        if (baseOutput.isSuccess()) {
            this.thirdPartyPushDataService.updatePushTime(pushData, endTime);
            hangGuoDataService.updateCheckOrderDisposeReportFlag(disposalDtos);
        } else {
            logger.error("上报:{} 失败，原因:{}", ReportInterfaceEnum.HANGGUO_DISPOSE.getName(), baseOutput.getMessage());
        }
        return baseOutput;
    }*/

    /*private List<ReportUnqualifiedDisposalDto> getReportDisposeList(Long platformMarketId,Date endTime) {

        List<DetectRecordParam> disposeList = getReportCheckOrderList(platformMarketId,DetectRecordStateEnum.UNQUALIFIED.getCode(),endTime);;
        if (CollectionUtils.isEmpty(disposeList)) {
            return null;
        }
        List<ReportUnqualifiedDisposalDto> reportList = transformationDisposeExample(disposeList, platformMarketId);
        String ids = getCheckDisposeIds(disposeList);
        Map<Long, List<ImageCert>> imageCertMap = getCheckOrderImgMap(ImageCertBillTypeEnum.DISPOSE_TYPE.getCode(), ids);

        StreamEx.of(reportList).nonNull().forEach(h -> {
            List<ReportInspectionImgDto> imgList = buildReportImgList(h.getId(),imageCertMap);
            h.setCheckFailImgList(imgList);
        });
        return reportList;
    }*/

    private String getCheckDisposeIds(List<CheckOrderDispose> disposeList) {
        List<String> idList = StreamEx.of(disposeList).nonNull().map(h -> String.valueOf(h.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(idList)) {
            return idList.stream().collect(Collectors.joining("','"));
        }
        return null;
    }

    /*private List<ReportUnqualifiedDisposalDto> transformationDisposeExample(List<DetectRecordParam> disposeList, Long platformMarketId) {
        return StreamEx.of(disposeList).nonNull().map(m -> {
            ReportUnqualifiedDisposalDto r = new ReportUnqualifiedDisposalDto();
            r.setId(m.getId());
            r.setMarketId(platformMarketId);
            r.setChuZhiType(m.getDisposeType());
            r.setCheckNo(m.getDetectBatchNo());
            r.setChuZhiDate(m.getDisposeDate());
            r.setChuZhiDesc(m.getDisposeDesc());
            r.setChuZhier(m.getDisposer());
            r.setChuZhiNum(m.getDisposeNum());
            r.setChuZhiResult(m.getDisposeResult());
            return r;
        }).collect(Collectors.toList());
    }*/

    private BaseOutput pushFruitsInspectionData(Optional<OperatorUser> optUser, Date endTime, Market market) {
        Date updateTime = null;
        Long marketId = market.getId();
        Long platformMarketId = market.getPlatformMarketId();
        ThirdPartyPushData pushData = thirdPartyPushDataService.getThredPartyPushData(ReportInterfaceEnum.HANGGUO_INSPECTION.getCode(), marketId);

        if (pushData == null) {
            updateTime = endTime;
            pushData = new ThirdPartyPushData();
            pushData.setTableName(ReportInterfaceEnum.HANGGUO_INSPECTION.getCode());
            pushData.setInterfaceName(ReportInterfaceEnum.HANGGUO_INSPECTION.getName());
            pushData.setPushTime(endTime);
            pushData.setMarketId(marketId);
        } else {
            updateTime = pushData.getPushTime();
        }

        List<ReportInspectionDto> inspectionDtoList = getReportInspectionList(platformMarketId,updateTime);
        logger.info("Report HangGuo checkOrder Dto ： " + JSON.toJSONString(inspectionDtoList));
        if (CollectionUtils.isEmpty(inspectionDtoList)) {
            logger.info("report checkOrder is null");
            return BaseOutput.success("report checkOrder is null");
        }

        BaseOutput baseOutput = new BaseOutput();
        // 分批上报
        Integer batchSize = (pushBatchSize == null || pushBatchSize == 0) ? 64 : pushBatchSize;
        // 分批数
        Integer part = inspectionDtoList.size() / batchSize;
        for (int i = 0; i <= part; i++) {
            Integer endPos = i == part ? inspectionDtoList.size() : (i + 1) * batchSize;
            List<ReportInspectionDto> partBills = inspectionDtoList.subList(i * batchSize, endPos);
            baseOutput = this.dataReportService.reportFruitsInspection(partBills, optUser, market);
        }

        if (baseOutput.isSuccess()) {
            this.thirdPartyPushDataService.updatePushTime(pushData, endTime);
            hangGuoDataService.updateCheckOrderReportFlag(inspectionDtoList);
        } else {
            logger.error("上报:{} 失败，原因:{}", ReportInterfaceEnum.HANGGUO_INSPECTION.getName(), baseOutput.getMessage());
        }
        return baseOutput;
    }

    /**
     * 获取检测数据
     * @param platformMarketId
     * @param endTime
     * @return
     */
    private List<ReportInspectionDto> getReportInspectionList(Long platformMarketId, Date endTime) {
        List<DetectRecordParam> checkOrderList = getReportCheckOrderList(platformMarketId,DetectRecordStateEnum.QUALIFIED.getCode(),endTime);
        if (CollectionUtils.isEmpty(checkOrderList)) {
            return null;
        }
        List<ReportInspectionDto> headList = transformationExample(checkOrderList, platformMarketId);
        String ids = getCheckIds(headList);
        Map<Long, List<ImageCert>> imageCertMap = getCheckOrderImgMap(BillTypeEnum.CHECK_ORDER.getCode(), ids);

        StreamEx.of(headList).nonNull().forEach(h -> {
            List<ReportInspectionImgDto> imgList = buildReportImgList(h.getId(),imageCertMap);
            h.setCheckImgList(imgList);
        });
        return headList;
    }

    private List<ReportInspectionImgDto> buildReportImgList(Long id, Map<Long, List<ImageCert>> imageCertMap) {
        List<ImageCert> imgList = imageCertMap.get(id);
        if (CollectionUtils.isNotEmpty(imgList)) {
            return StreamEx.of(imgList).nonNull().map(m -> {
                ReportInspectionImgDto reportImg = new ReportInspectionImgDto();
                reportImg.setCredentialName(BillTypeEnum.CHECK_ORDER.getName());
                if (StringUtils.isNotBlank(m.getUid())) {
                    reportImg.setPicUrl(baseWebPath + m.getUid());
                }
                return reportImg;
            }).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private List<ReportInspectionDto> transformationExample(List<DetectRecordParam> checkOrderList, Long platformMarketId) {
        return StreamEx.of(checkOrderList).nonNull().map(c -> {
            ReportInspectionDto re = new ReportInspectionDto();
            re.setId(c.getId());
            re.setIdCard(c.getIdCardNo());
            re.setChecker(c.getDetectOperator());
            re.setCheckNo(c.getDetectBatchNo());
            re.setCheckOrgName(c.getDetectCompany());
            if(DetectRecordStateEnum.QUALIFIED.getCode().equals(c.getDetectState())){
                re.setCheckResult("0");
            }else{
                re.setCheckResult("1");
            }
            re.setCheckTime(c.getDetectTime());
            //默认为定量
            re.setCheckType(CheckTypeEnum.QUANTITATIVE.getCode().toString());
            re.setGoodsName(c.getProductName());
            re.setGoodsCode(c.getGoodsCode());
            re.setMarketId(platformMarketId);
            ReportInspectionItemDto itemDto = new ReportInspectionItemDto();
            itemDto.setNormalValue(c.getNormalResult());
            itemDto.setValue(c.getPdResult());
            itemDto.setResult(DetectRecordStateEnum.name(c.getDetectState()));
            itemDto.setProject(DetectTypeEnum.name(c.getDetectType()));
            re.setCheckItem(Arrays.asList(itemDto));
            return re;
        }).collect(Collectors.toList());
    }

    /**
     * 查询合格检测信息
     *
     * @param platformMarketId
     * @param endTime
     * @return
     */
    private List<DetectRecordParam> getReportCheckOrderList(Long platformMarketId,Integer state, Date endTime) {
        DetectRecordParam detectRecord = new DetectRecordParam();
        detectRecord.setDetectState(state);
        detectRecord.setDetectTimeStart(endTime);
        detectRecord.setMarketId(platformMarketId);
        return detectRecordService.listBillByRecord(detectRecord);
    }

    private Map<Long, List<ImageCert>> getCheckOrderImgMap(Integer billType, String ids) {
        ImageCert imageCert = new ImageCert();
        imageCert.setBillType(billType);
        if (StringUtils.isNotBlank(ids)) {
            imageCert.setMetadata(IDTO.AND_CONDITION_EXPR, " bill_id in ('" + ids + "')");
        }
        List<ImageCert> imageCerts = imageCertService.listByExample(imageCert);
        return StreamEx.of(imageCerts).nonNull().collect(Collectors.groupingBy(ImageCert::getBillId));
    }


    private String getCheckIds(List<ReportInspectionDto> headList) {
        List<String> idList = StreamEx.of(headList).nonNull().map(h -> String.valueOf(h.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(idList)) {
            return idList.stream().collect(Collectors.joining("','"));
        }
        return null;
    }

    /**
     * 上报杭果用户
     *
     * @param optUser
     * @param endTime
     * @param market
     * @return
     */
    private BaseOutput pushFruitsUser(Optional<OperatorUser> optUser, Date endTime, Market market) {
        Date updateTime = null;
        boolean newPushFlag = true;
        Long marketId = market.getId();
        Long platformMarketId = market.getPlatformMarketId();
        ThirdPartyPushData pushData = thirdPartyPushDataService.getThredPartyPushData(ReportInterfaceEnum.HANGGUO_USER.getCode(), marketId);

        if (pushData == null) {
            updateTime = endTime;
            pushData = new ThirdPartyPushData();
            pushData.setTableName(ReportInterfaceEnum.HANGGUO_USER.getCode());
            pushData.setInterfaceName(ReportInterfaceEnum.HANGGUO_USER.getName());
            pushData.setPushTime(endTime);
            pushData.setMarketId(marketId);
        } else {
            updateTime = pushData.getPushTime();
            newPushFlag = false;
        }

        UserInfo user = new UserInfo();
        user.setMarketId(market.getId());
        user.setState(EnabledStateEnum.ENABLED.getCode());
        user.setYn(YesOrNoEnum.YES.getCode());
        user.setValidateState(ValidateStateEnum.PASSED.getCode());
        String addQue = " phone <> '' and phone <> '''''' ";
        if (!newPushFlag) {
            addQue += " and  modified >= '" + DateUtils.format(updateTime) + "' ";
        }
        user.mset(IDTO.AND_CONDITION_EXPR, addQue);
        List<UserInfo> userList = userService.listByExample(user);
        if (CollectionUtils.isEmpty(userList)) {
            return BaseOutput.success("NULL USER NEED PUSH");
        }
        Map<String, List<UserInfo>> listMap = StreamEx.of(userList).nonNull().collect(Collectors.groupingBy(u -> u.getName() + "_" + u.getPhone() + "_" + u.getMarketId()));
        List<UserInfo> gourpUser = new ArrayList<>();
        StreamEx.of(listMap.entrySet()).nonNull().forEach(ul -> {
            gourpUser.add(ul.getValue().get(0));
        });
        List<ReportUserDto> reportUserDtoList = new ArrayList<>();
        StreamEx.of(gourpUser).nonNull().forEach(u -> {
            ReportUserDto userDto = new ReportUserDto();
            userDto.setAccountName(u.getName());
            userDto.setTel(u.getPhone());
            userDto.setBoothNo(u.getTallyAreaNos());
            if (UserTypeEnum.CORPORATE.getCode().equals(u.getUserType())) {
                userDto.setIdCard(u.getLicense());
            } else {
                userDto.setIdCard(u.getCardNo());
            }
            userDto.setMarketId(platformMarketId);
            reportUserDtoList.add(userDto);
        });
        logger.info("Report Hangguo User Dto ： " + JSON.toJSONString(reportUserDtoList));
        if (CollectionUtils.isEmpty(reportUserDtoList)) {
            logger.info("Report Hangguo User ISNULL");
            return BaseOutput.success("Report Hangguo User ISNULL");
        }
        //BaseOutput baseOutput = this.dataReportService.reportFruitsUser(reportUserDtoList, optUser, market);

        BaseOutput baseOutput = new BaseOutput();
        // 分批上报
        Integer batchSize = (pushBatchSize == null || pushBatchSize == 0) ? 64 : pushBatchSize;
        // 分批数
        Integer part = reportUserDtoList.size() / batchSize;
        for (int i = 0; i <= part; i++) {
            Integer endPos = i == part ? reportUserDtoList.size() : (i + 1) * batchSize;
            List<ReportUserDto> partBills = reportUserDtoList.subList(i * batchSize, endPos);
            baseOutput = this.dataReportService.reportFruitsUser(partBills, optUser, market);
        }

        if (baseOutput.isSuccess()) {
            this.thirdPartyPushDataService.updatePushTime(pushData, endTime);
        } else {
            logger.error("上报:{} 失败，原因:{}", ReportInterfaceEnum.HANGGUO_USER.getName(), baseOutput.getMessage());
        }
        return baseOutput;
    }

    /**
     * 上报杭果商品
     *
     * @param optUser
     * @param endTime
     * @param market
     * @return
     */
    private BaseOutput pushFruitsCategory(Optional<OperatorUser> optUser, Date endTime, Market market) {
        Date updateTime = null;
        boolean newPushFlag = true;
        Long marketId = market.getId();
        Long platformMarketId = market.getPlatformMarketId();
        ThirdPartyPushData pushData = thirdPartyPushDataService.getThredPartyPushData(ReportInterfaceEnum.HANGGUO_GOODS.getCode(), marketId);

        if (pushData == null) {
            updateTime = endTime;
            pushData = new ThirdPartyPushData();
            pushData.setTableName(ReportInterfaceEnum.HANGGUO_GOODS.getCode());
            pushData.setInterfaceName(ReportInterfaceEnum.HANGGUO_GOODS.getName());
            pushData.setPushTime(endTime);
            pushData.setMarketId(marketId);
        } else {
            updateTime = pushData.getPushTime();
            newPushFlag = false;
        }

        Category category = new Category();
        category.setType(CategoryTypeEnum.SUPPLEMENT.getCode());
        category.setIsShow(CategoryIsShowEnum.IS_SHOW.getCode());
        if (!newPushFlag) {
            category.setMetadata(IDTO.AND_CONDITION_EXPR, " modified >= '" + DateUtils.format(updateTime) + "'");
        }
        List<Category> allQrHistories = categoryService.listByExample(category);
        List<GoodsDto> categoryDtos = new ArrayList<>();
        StreamEx.of(allQrHistories).nonNull().forEach(c -> {
            GoodsDto categoryDto = new GoodsDto();
            categoryDto.setGoodsName(c.getName());
            categoryDto.setGoodsCode(c.getCode());
            categoryDto.setMarketId(String.valueOf(platformMarketId));
            categoryDtos.add(categoryDto);
        });
        logger.info("Report Hangguo Category Dto ： " + JSON.toJSONString(categoryDtos));
        if (CollectionUtils.isEmpty(categoryDtos)) {
            logger.info("report Category ISNULL");
            return BaseOutput.success("report Category ISNULL");
        }
        //BaseOutput baseOutput = this.dataReportService.reportFruitsGoods(categoryDtos, optUser, market);

        BaseOutput baseOutput = new BaseOutput();
        // 分批上报
        Integer batchSize = (pushBatchSize == null || pushBatchSize == 0) ? 64 : pushBatchSize;
        // 分批数
        Integer part = categoryDtos.size() / batchSize;
        for (int i = 0; i <= part; i++) {
            Integer endPos = i == part ? categoryDtos.size() : (i + 1) * batchSize;
            List<GoodsDto> partBills = categoryDtos.subList(i * batchSize, endPos);
            baseOutput = this.dataReportService.reportFruitsGoods(partBills, optUser, market);
        }

        if (baseOutput.isSuccess()) {
            this.thirdPartyPushDataService.updatePushTime(pushData, endTime);
        } else {
            logger.error("上报:{} 失败，原因:{}", ReportInterfaceEnum.HANGGUO_GOODS.getName(), baseOutput.getMessage());
        }
        return baseOutput;
    }


}