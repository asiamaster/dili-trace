package com.dili.trace.jobs;

import com.alibaba.fastjson.JSON;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.util.DateUtils;
import com.dili.trace.dao.RegisterBillMapper;
import com.dili.trace.domain.*;
import com.dili.trace.domain.hangguo.*;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.enums.MarketEnum;
import com.dili.trace.enums.ReportInterfaceEnum;
import com.dili.trace.enums.SysConfigTypeEnum;
import com.dili.trace.enums.ThirdSourceTypeEnum;
import com.dili.trace.glossary.EnabledStateEnum;
import com.dili.trace.service.*;
import com.dili.uap.sdk.domain.Firm;
import one.util.streamex.StreamEx;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 对接杭果溯源入口，通过定时任务每6小时调用杭果的接口，交易数据每小时调用一次
 *
 * @author asa.lee
 */
//@Component
public class HangGuoTraceabilityDataJob implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(HangGuoTraceabilityDataJob.class);

    @Autowired
    RegisterBillMapper registerBillMapper;
    @Autowired
    HangGuoDataUtil hangGuoDataUtil;
    @Autowired
    HangGuoDataService hangGuoDataService;
    @Autowired
    ThirdPartyPushDataService thirdPartyPushDataService;
    @Autowired
    UserService userService;
    @Autowired
    SysConfigService sysConfigService;
    @Autowired
    MarketService marketService;

    @Value("${thrid.insert.batch.size}")
    private Integer insertBatchSize;
    @Value("${thrid.datasource.hangguo.service.url}")
    private String hangGuoServiceUrl;
    @Value("${thrid.datasource.hangguo.service.appid}")
    private String appid;
    @Value("${thrid.datasource.hangguo.service.key}")
    private String key;

    //当前时间往前推6小时
    private Integer generalTimeInterval = -6;
    /**
     * 交易数据当前时间往前推1小时
     */
    private Integer tradeInterval = -1;
    private String queDateFormatter = "yyyyMMddHH:mm:ss";
    /**
     * 定义一个杭果市场id用于区分上报数据表中数据
     */
    final public static Long HGMarketId = 2L;

    @Override
    public void run(String... args) throws Exception {
        //启动时初始化一次基础数据
        //getBaseData();
        // 商品信息
        //Date endTime = this.registerBillMapper.selectCurrentTime();
        //getThirdGoodsData(endTime);
        //getTradeData();
    }


    private SysConfig getCallDataSwitch() {
        SysConfig sysConfig = new SysConfig();
        sysConfig.setOptType(SysConfigTypeEnum.CALL_DATA_SWITCH.getCode());
        sysConfig.setOptCategory(SysConfigTypeEnum.CALL_DATA_HANG_GUO.getCode());
        List<SysConfig> list = sysConfigService.listByExample(sysConfig);
        if (!CollectionUtils.isEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 商品信息、供应商信息、会员信息每六小时调用一次
     */
    //@Scheduled(cron = "0 */15 * * * ?")
    //@Scheduled(cron = "0 10 */6 * * ?")
    public void getBaseData() {
        try {
            SysConfig dataSwitch = getCallDataSwitch();
            if (null != dataSwitch) {
                String optValue = dataSwitch.getOptValue();
                String allow = "Y";
                if (null != optValue && !allow.equals(optValue)) {
                    logger.info("远程调用数据已关闭");
                    return;
                }
            }
            Date endTime = this.registerBillMapper.selectCurrentTime();
            // 商品信息
            getThirdGoodsData(endTime);
            //供应商
            getThirdSupplierData(endTime);
            //会员
            getThirdMemberData(endTime);
            //供应商证件照
            getThirdSupplierPicData(endTime);
            //会员证件照
            getThirdMemberPicData(endTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    /**
     * 交易数据每小时调用一次
     */
    //@Scheduled(cron = "0 */10 * * * ?")
    //@Scheduled(cron = "0 10 */1 * * ?")
    public void getTradeData() {
        try {
            SysConfig dataSwitch = getCallDataSwitch();
            if (null != dataSwitch) {
                String optValue = dataSwitch.getOptValue();
                String allow = "Y";
                if (null != optValue && !allow.equals(optValue)) {
                    logger.info("远程调用数据已关闭");
                    return;
                }
            }

            Date endTime = this.registerBillMapper.selectCurrentTime();
            // 商品信息
            List<HangGuoTrade> tradeList = this.getTradeList(endTime);
            if (!CollectionUtils.isEmpty(tradeList)) {
                hangGuoDataUtil.createTrade(tradeList, endTime);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void getThirdSupplierPicData(Date endTime) {
        UserInfo user = new UserInfo();
        Firm firm = marketService.getMarketByCode(MarketEnum.HZSG);
        user.setMarketId(firm.getId());
        user.setYn(YesOrNoEnum.YES.getCode());
        List<UserInfo> userList = userService.getUserByCredentialUrl(user);

        if (!CollectionUtils.isEmpty(userList)) {
            String supType = "S";
            List<UserInfo> updateList = new ArrayList<>();
            StreamEx.of(userList).nonNull().forEach(u -> {
                String picContent = getSupplierPic(u.getThirdPartyCode(), endTime);
//                String picUrl = hangGuoDataUtil.createHangGuoUserPic(u.getThirdPartyCode(), supType, picContent);
//                if (StringUtils.isNotBlank(picUrl)) {
//                    u.setCardNoFrontUrl(picUrl);
//                    u.setCardNoBackUrl(picUrl);
//                    updateList.add(u);
//                }
            });
            if (!CollectionUtils.isEmpty(updateList)) {
                hangGuoDataService.batchUpdateUserByThirdCode(updateList);
            }
        }
    }

    private void getThirdMemberPicData(Date endTime) {
        UserInfo user = new UserInfo();
        Firm firm = marketService.getMarketByCode(MarketEnum.HZSG);
        user.setMarketId(firm.getId());
        user.setYn(YesOrNoEnum.YES.getCode());
        List<UserInfo> userList = userService.getUserByCredentialUrl(user);
        if (!CollectionUtils.isEmpty(userList)) {
            String memType = "M";
            List<UserInfo> updateList = new ArrayList<>();
            StreamEx.of(userList).nonNull().forEach(u -> {
                String picContent = getMemberPic(u.getThirdPartyCode(), endTime);
//                String picUrl = hangGuoDataUtil.createHangGuoUserPic(u.getThirdPartyCode(), memType, picContent);
//                if (StringUtils.isNotBlank(picUrl)) {
////                    u.setCredentialUrl(picUrl);
//                    u.setCardNoBackUrl(picUrl);
//                    u.setCardNoFrontUrl(picUrl);
//                    updateList.add(u);
//                }
            });
            if (!CollectionUtils.isEmpty(updateList)) {
                hangGuoDataService.batchUpdateUserByThirdCode(updateList);
            }
        }
    }


    private void getThirdMemberData(Date endTime) {
        Firm firm = marketService.getMarketByCode(MarketEnum.HZSG);
        ThirdPartyPushData pushData = thirdPartyPushDataService.getThredPartyPushData(ReportInterfaceEnum.SOURCE_HANGGUO_MEMBER.getCode(), firm.getId());
        boolean isFirst = false;
        if (null == pushData) {
            isFirst = true;
            pushData = new ThirdPartyPushData();
            pushData.setTableName(ReportInterfaceEnum.SOURCE_HANGGUO_MEMBER.getCode());
            pushData.setInterfaceName(ReportInterfaceEnum.SOURCE_HANGGUO_MEMBER.getName());
            pushData.setPushTime(endTime);
            pushData.setMarketId(firm.getId());
        }
        List<HangGuoUser> memberList = this.getMemberList(endTime, isFirst);
        if (!CollectionUtils.isEmpty(memberList)) {
//            hangGuoDataUtil.createHangGuoMember(memberList, endTime);
            this.thirdPartyPushDataService.updatePushTime(pushData, endTime);
        }
    }

    private void getThirdSupplierData(Date endTime) {
        Firm firm = marketService.getMarketByCode(MarketEnum.HZSG);
        ThirdPartyPushData pushData = thirdPartyPushDataService.getThredPartyPushData(ReportInterfaceEnum.SOURCE_HANGGUO_SUPPLIER.getCode(), firm.getId());
        boolean isFirst = false;
        if (null == pushData) {
            isFirst = true;
            pushData = new ThirdPartyPushData();
            pushData.setTableName(ReportInterfaceEnum.SOURCE_HANGGUO_SUPPLIER.getCode());
            pushData.setInterfaceName(ReportInterfaceEnum.SOURCE_HANGGUO_SUPPLIER.getName());
            pushData.setPushTime(endTime);
            pushData.setMarketId(firm.getId());
        }
        // 供应商信息
        List<HangGuoUser> supplierList = this.getSupplierList(endTime, isFirst);
        if (!CollectionUtils.isEmpty(supplierList)) {
//            hangGuoDataUtil.createHangGuoSupplier(supplierList, endTime);
            this.thirdPartyPushDataService.updatePushTime(pushData, endTime);
        }
    }

    private void getThirdGoodsData(Date endTime) {
        ThirdPartyPushData pushData = thirdPartyPushDataService.getThredPartyPushData(ReportInterfaceEnum.SOURCE_HANGGUO_GOODS.getCode(), HGMarketId);
        boolean isFirst = false;
        //由于updatetime为预留参数没有实际意义.因此获取了一次后不会再获取,否则又是全量数据
        if (null == pushData) {
            isFirst = true;
            pushData = new ThirdPartyPushData();
            pushData.setTableName(ReportInterfaceEnum.SOURCE_HANGGUO_GOODS.getCode());
            pushData.setInterfaceName(ReportInterfaceEnum.SOURCE_HANGGUO_GOODS.getName());
            pushData.setPushTime(endTime);
            pushData.setMarketId(HGMarketId);
            List<HangGuoCommodity> categoryList = this.getGoodsCategory(endTime, isFirst);
            if (!CollectionUtils.isEmpty(categoryList)) {
               hangGuoDataUtil.createCommodity(categoryList, endTime);
                this.thirdPartyPushDataService.updatePushTime(pushData, endTime);
            }
        } else {
            List<HangGuoCommodity> categoryList = this.getGoodsCategory(pushData.getPushTime(), isFirst);
            if (!CollectionUtils.isEmpty(categoryList)) {
                hangGuoDataUtil.createCommodity(categoryList, endTime);
                this.thirdPartyPushDataService.updatePushTime(pushData, endTime);
            }
        }
    }

    private List<HangGuoTrade> getTradeList(Date endTime) {
        List<HangGuoTrade> tradeList = new ArrayList<>();
        String tradeStr = null;
        Firm firm = marketService.getMarketByCode(MarketEnum.HZSG);
        ThirdPartyPushData pushData = thirdPartyPushDataService.getThredPartyPushData(ReportInterfaceEnum.SOURCE_HANGGUO_TRADE.getCode(), firm.getId());
        boolean isFirst = false;
        if (null == pushData) {
            isFirst = true;
            pushData = new ThirdPartyPushData();
            pushData.setTableName(ReportInterfaceEnum.SOURCE_HANGGUO_TRADE.getCode());
            pushData.setInterfaceName(ReportInterfaceEnum.SOURCE_HANGGUO_TRADE.getName());
            pushData.setPushTime(endTime);
            pushData.setMarketId(firm.getId());
        }

        HangGuoResult result1 = getHangGuoTradeResult(endTime, isFirst);
        if (null != result1 && StringUtils.isBlank(result1.getRecode())) {
            tradeStr = result1.getData();
        }
        if (StringUtils.isNotBlank(tradeStr)) {

            Integer restrInt = tradeStr.length();
            Integer batchSize = (insertBatchSize == null || insertBatchSize == 0) ? 1000 : insertBatchSize;
            batchSize = batchSize * 80;
            Integer part = tradeStr.length() / batchSize;
            for (int i = 0; i <= part; i++) {
                Integer endPos = i == part ? tradeStr.length() : (i + 1) * batchSize;
                String partBills = tradeStr.substring(i * batchSize, endPos);
                addSource(partBills, ThirdSourceTypeEnum.TRADE.getCode(), ThirdSourceTypeEnum.TRADE.getDesc(),
                        endTime, firm.getId());
            }
            tradeList = JSON.parseArray(tradeStr, HangGuoTrade.class);
            logger.info("-----===>获取交易数量总数为：" + tradeList.size());
            this.thirdPartyPushDataService.updatePushTime(pushData, endTime);
        } else {
            if (null != result1) {
                tradeStr = JSON.toJSONString(result1);
            }
            addSource(tradeStr, ThirdSourceTypeEnum.TRADE.getCode(), ThirdSourceTypeEnum.TRADE.getDesc(),
                    endTime, firm.getId());
        }
        logger.info("====>end getTradeList time:" + (DateUtils.getCurrentDate().getTime() - endTime.getTime()));
        return tradeList;
    }

    private List<HangGuoUser> getMemberList(Date endTime, boolean isFirst) {
        List<HangGuoUser> hangGuoUserList = new ArrayList<>();
        Date startdate = DateUtils.getCurrentDate();
        String userStr = null;
        Firm firm = marketService.getMarketByCode(MarketEnum.HZSG);

        HangGuoResult result1 = getHangGuoMemberResult(endTime, isFirst);
        if (null != result1 && StringUtils.isBlank(result1.getRecode())) {
            userStr = result1.getData();
        }
        if (StringUtils.isNotBlank(userStr)) {

            Integer restrInt = userStr.length();
            Integer batchSize = (insertBatchSize == null || insertBatchSize == 0) ? 1000 : insertBatchSize;
            batchSize = batchSize * 80;
            Integer part = userStr.length() / batchSize;
            for (int i = 0; i <= part; i++) {
                Integer endPos = i == part ? userStr.length() : (i + 1) * batchSize;
                String partBills = userStr.substring(i * batchSize, endPos);
                addSource(partBills, ThirdSourceTypeEnum.MEMBER.getCode(), ThirdSourceTypeEnum.MEMBER.getDesc(),
                        endTime, firm.getId());
            }
            hangGuoUserList = JSON.parseArray(userStr, HangGuoUser.class);
            logger.info("-----===>getMemberList:" + hangGuoUserList.size());
            handleUserParam(hangGuoUserList);
        } else {
            if (null != result1) {
                userStr = JSON.toJSONString(result1);
            }
            addSource(userStr, ThirdSourceTypeEnum.MEMBER.getCode(), ThirdSourceTypeEnum.MEMBER.getDesc(),
                    startdate, firm.getId());
        }
        logger.info("====>end getMemberList time:" + (DateUtils.getCurrentDate().getTime() - startdate.getTime()));
        return hangGuoUserList;
    }


    private List<HangGuoUser> getSupplierList(Date endTime, boolean isFirst) {
        List<HangGuoUser> hangGuoUserList = new ArrayList<>();
        Date startdate = DateUtils.getCurrentDate();
        String userStr = null;
        Firm firm = marketService.getMarketByCode(MarketEnum.HZSG);
        HangGuoResult result = getHangGuoSupplierResult(endTime, isFirst);
        if (null != result && StringUtils.isBlank(result.getRecode())) {
            userStr = result.getData();
        }
        if (StringUtils.isNotBlank(userStr)) {
            Integer restrInt = userStr.length();
            Integer batchSize = (insertBatchSize == null || insertBatchSize == 0) ? 1000 : insertBatchSize;
            batchSize = batchSize * 80;
            Integer part = userStr.length() / batchSize;
            for (int i = 0; i <= part; i++) {
                Integer endPos = i == part ? userStr.length() : (i + 1) * batchSize;
                String partBills = userStr.substring(i * batchSize, endPos);
                addSource(partBills, ThirdSourceTypeEnum.SUPPLIER.getCode(), ThirdSourceTypeEnum.SUPPLIER.getDesc(),
                        endTime, firm.getId());
            }
            hangGuoUserList = JSON.parseArray(userStr, HangGuoUser.class);
            logger.info("-----===>getSupplierList:" + hangGuoUserList.size());
            handleUserParam(hangGuoUserList);
        } else {
            if (null != result) {
                userStr = JSON.toJSONString(result);
            }
            addSource(userStr, ThirdSourceTypeEnum.SUPPLIER.getCode(), ThirdSourceTypeEnum.SUPPLIER.getDesc(),
                    endTime, firm.getId());
        }
        logger.info("====>end getMemberList time:" + (DateUtils.getCurrentDate().getTime() - startdate.getTime()));
        return hangGuoUserList;
    }

    private List<HangGuoCommodity> getGoodsCategory(Date endTime, boolean isFirst) {
        List<HangGuoCommodity> commodityList = new ArrayList<>();
        Date startdate = DateUtils.getCurrentDate();
        String resultStr = null;

        HangGuoResult result = getHangGuoGoodsResult(endTime, isFirst);
        if (null != result && StringUtils.isBlank(result.getRecode())) {
            resultStr = result.getData();
        }
        if (StringUtils.isNotBlank(resultStr)) {
            Integer batchSize = (insertBatchSize == null || insertBatchSize == 0) ? 1000 : insertBatchSize;
            batchSize = batchSize * 80;
            Integer part = resultStr.length() / batchSize;
            for (int i = 0; i <= part; i++) {
                Integer endPos = i == part ? resultStr.length() : (i + 1) * batchSize;
                String partBills = resultStr.substring(i * batchSize, endPos);
                addSource(partBills, ThirdSourceTypeEnum.CATEGORY.getCode(), ThirdSourceTypeEnum.CATEGORY.getDesc(),
                        endTime, HGMarketId);
            }
            commodityList = JSON.parseArray(resultStr, HangGuoCommodity.class);
        } else {
            if (null != result) {
                resultStr = JSON.toJSONString(result);
            }
            addSource(resultStr, ThirdSourceTypeEnum.CATEGORY.getCode(), ThirdSourceTypeEnum.CATEGORY.getDesc(),
                    endTime, HGMarketId);
        }
        logger.info("====>end getGoodsCategory time:" + (DateUtils.getCurrentDate().getTime() - startdate.getTime()));
        return commodityList;
    }

    private String getMemberPic(String memberNo, Date endTime) {
        String resultStr = null;
        List<HangGuoPic> picList = new ArrayList<>();
        Date startdate = DateUtils.getCurrentDate();
        HangGuoResult result = getHangGuoMemberPicResult(memberNo, endTime);
        if (null != result && StringUtils.isBlank(result.getRecode())) {
            resultStr = result.getData();
        }
        if (StringUtils.isNotBlank(resultStr)) {
            picList = JSON.parseArray(resultStr, HangGuoPic.class);
        } else {
            if (null != result) {
                resultStr = JSON.toJSONString(result);
            }
        }
        logger.info("====>end getMemberPic time:" + (DateUtils.getCurrentDate().getTime() - startdate.getTime()));
        if (!CollectionUtils.isEmpty(picList)) {
            return picList.get(0).getMemberPic();
        }
        return null;
    }

    private String getSupplierPic(String supplierNo, Date endTime) {
        String resultStr = null;
        List<HangGuoPic> picList = new ArrayList<>();
        Date startdate = DateUtils.getCurrentDate();
        HangGuoResult result = getHangGuoSupplierPicResult(supplierNo, endTime);
        if (null != result && StringUtils.isBlank(result.getRecode())) {
            resultStr = result.getData();
        }
        if (StringUtils.isNotBlank(resultStr)) {
            picList = JSON.parseArray(resultStr, HangGuoPic.class);
        } else {
            if (null != result) {
                resultStr = JSON.toJSONString(result);
            }
        }
        logger.info("====>end getSupplierPic time:" + (DateUtils.getCurrentDate().getTime() - startdate.getTime()));
        if (!CollectionUtils.isEmpty(picList)) {
            return picList.get(0).getSupplierPic();
        }
        return null;
    }


    /**
     * 处理非空项和参数转换
     *
     * @param hangGuoUserList
     */
    private void handleUserParam(List<HangGuoUser> hangGuoUserList) {
        String active = "激活";
        String nullStr = "''";
        String effDateFormatter = "yyyy-MM-dd HH:mm:ss";
        StreamEx.of(hangGuoUserList).nonNull().forEach(u -> {
            if (StringUtils.isNotBlank(u.getStatus()) && active.equals(u.getStatus())) {
                u.setStatusCode(EnabledStateEnum.ENABLED.getCode());
            } else {
                u.setStatusCode(EnabledStateEnum.DISABLED.getCode());
            }
            if (StringUtils.isNotBlank(u.getEffectiveDate())) {
                //2010022700:00:00
                u.setEffectiveDateTime(DateUtils.dateStr2Date(u.getEffectiveDate(), effDateFormatter));
            }
            if (StringUtils.isBlank(u.getPhoneNumber())) {
                u.setPhoneNumber(nullStr);
            }
            if (StringUtils.isBlank(u.getMobileNumber())) {
                u.setMobileNumber(nullStr);
            }
        });
    }

    private HangGuoResult getHangGuoMemberPicResult(String memberNo, Date endTime) {
        String url = "memberidpic";
        String timp = String.valueOf(new Date().getTime() / 1000L);
        String mdfive = getGeneralMd5Key(timp, memberNo);
        url = url + "?appid=" + appid + "&timestamp=" + timp + "&sign=" + mdfive + "&memberno=" + memberNo;
        return getHangGuoServiceUrlResult(url);
    }

    private HangGuoResult getHangGuoSupplierPicResult(String supplierNo, Date endTime) {
        String queDate = supplierNo;
        String url = "supidpic";
        String timp = String.valueOf(new Date().getTime() / 1000L);
        String mdfive = getGeneralMd5Key(timp, queDate);
        url = url + "?appid=" + appid + "&timestamp=" + timp + "&sign=" + mdfive + "&supplierno=" + queDate;
        return getHangGuoServiceUrlResult(url);
    }

    private HangGuoResult getHangGuoGoodsResult(Date endTime, boolean isFirst) {
        String queDate = "";
        if (!isFirst) {
            queDate = DateUtils.format(DateUtils.addHours(endTime, generalTimeInterval), "yyyyMMddHH:mm:ss");
        }
        String url = "goodsinfo";
        String timp = String.valueOf(new Date().getTime() / 1000L);
        String mdfive = getGoodsMD5(timp);
        url = url + "?appid=" + appid + "&timestamp=" + timp + "&sign=" + mdfive + "&updatetime=" + queDate;
        return getHangGuoServiceUrlResult(url);
    }

    private HangGuoResult getHangGuoTradeResult(Date endTime, boolean isFirst) {
        String url = "orderdata";
        String timp = String.valueOf(new Date().getTime() / 1000L);
        String stime = DateUtils.format(DateUtils.addHours(endTime, tradeInterval), queDateFormatter);
        String etime = DateUtils.format(endTime, queDateFormatter);
        String mdfive = getTradeMd5Key(stime, etime, timp);
        url = url + "?appid=" + appid + "&timestamp=" + timp + "&sign=" + mdfive + "&stime=" + stime + "&etime=" + etime;
        return getHangGuoServiceUrlResult(url);
    }

    private HangGuoResult getHangGuoMemberResult(Date endTime, boolean isFirst) {
        String url = getGeneralUrl(endTime, "memberdata", isFirst);
        return getHangGuoServiceUrlResult(url);
    }

    private HangGuoResult getHangGuoSupplierResult(Date endTime, boolean isFirst) {
        String url = getGeneralUrl(endTime, "supplierdata", isFirst);
        return getHangGuoServiceUrlResult(url);
    }

    private String getGeneralUrl(Date endTime, String url, boolean isFirst) {
        String queDate = "";
        if (!isFirst) {
            queDate = DateUtils.format(DateUtils.addHours(endTime, generalTimeInterval), queDateFormatter);
        }
        String timp = String.valueOf(new Date().getTime() / 1000L);
        String mdfive = getGeneralMd5Key(timp, queDate);
        url = url + "?appid=" + appid + "&timestamp=" + timp + "&sign=" + mdfive + "&updatedtime=" + queDate;
        return url;
    }

    private String getTradeMd5Key(String stime, String etime, String timp) {
        String model = stime + etime + timp + appid + key;
        return DigestUtils.md5Hex(model).toUpperCase();
    }

    private String getGeneralMd5Key(String timp, String que) {
        String model = timp + appid + que + key;
        return DigestUtils.md5Hex(model).toUpperCase();
    }

    private String getGoodsMD5(String stime) {
        String model = stime + appid + key;
        return DigestUtils.md5Hex(model).toUpperCase();
    }

    /**
     * 新增来源缓存记录
     *
     * @param dataStr
     * @param code
     * @param desc
     * @param startdate
     * @param marketId
     */
    private void addSource(String dataStr, Integer code, String desc, Date startdate, Long marketId) {
        OperatorUser optUser = new OperatorUser(-1L, "auto");
        ThirdPartySourceData sourceData = new ThirdPartySourceData();
        sourceData.setType(code);
        sourceData.setName(desc);
        sourceData.setData(dataStr);
        sourceData.setCreated(startdate);
        sourceData.setModified(startdate);
        sourceData.setMarketId(marketId);
        sourceData.setOperatorId(optUser.getId());
        sourceData.setOperatorName(optUser.getName());
        hangGuoDataService.insertThirdPartySourceData(sourceData);
    }

    private HangGuoResult getHangGuoServiceUrlResult(String url) {
        HangGuoResult result = new HangGuoResult();
        try {
            RestTemplate restTemplate = new RestTemplate();
            logger.info("===========>>>>getHangGuoServiceUrlResult Url:" + hangGuoServiceUrl + "/" + url);
            String notice = restTemplate.getForObject(hangGuoServiceUrl + "/" + url, String.class);
            if (StringUtils.isNotBlank(notice)) {
                result = JSON.parseObject(notice, HangGuoResult.class);

                //异常重调最多两次
                if (null == result || StringUtils.isNotBlank(result.getRecode())) {
                    Integer retryCount = 2;
                    result=retryHangGuoServiceUrlResult(url,retryCount);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }


    private HangGuoResult retryHangGuoServiceUrlResult(String url,Integer count) {
        HangGuoResult result = new HangGuoResult();
        if(count<=0){
            return result;
        }
        count--;
        RestTemplate restTemplate = new RestTemplate();
        logger.info("===========>>>>getHangGuoServiceUrlResult Url:" + hangGuoServiceUrl + "/" + url);
        String notice = restTemplate.getForObject(hangGuoServiceUrl + "/" + url, String.class);
        if(StringUtils.isNotBlank(notice)){
            result = JSON.parseObject(notice, HangGuoResult.class);
            if (null == result || StringUtils.isNotBlank(result.getRecode())) {
               return retryHangGuoServiceUrlResult(url,count);
            }
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
    }

}
