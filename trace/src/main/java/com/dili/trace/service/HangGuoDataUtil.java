package com.dili.trace.service;

import com.dili.common.config.DefaultConfiguration;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.util.DateUtils;
import com.dili.trace.domain.*;
import com.dili.trace.domain.hangguo.*;
import com.dili.trace.enums.*;
import com.dili.trace.glossary.hanguo.HangGuoGoodsLevelEnum;
import com.dili.trace.jobs.HangGuoTraceabilityDataJob;
import com.dili.trace.rpc.service.UidRestfulRpcService;
import one.util.streamex.StreamEx;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author asa.lee
 */
@Service
public class HangGuoDataUtil {
    private static final Logger logger = LoggerFactory.getLogger(HangGuoDataUtil.class);
    @Autowired
    private UserService userService;
    @Autowired
    private UserExtService userExtService;
    @Autowired
    private HangGuoDataService hangGuoDataService;
    @Autowired
    private UidRestfulRpcService uidRestfulRpcService;
    @Autowired
    private TradeOrderService tradeOrderService;
    @Autowired
    private TradeRequestService tradeRequestService;
    @Autowired
    private TradeDetailService tradeDetailService;
    @Autowired
    private ProductStockService productStockService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DefaultConfiguration defaultConfiguration;

    @Value("${thrid.insert.batch.size}")
    private Integer insertBatchSize;
    //private static final String TRADE_REQUEST_CODE_TYPE = "TRADE_REQUEST_CODE";
    /**
     * 上报金额上限
     */
    private Integer reportMaxAmountInt = 300000;
    /**
     * 杭果市场code
     */
    private String HGMarketCode = MarketEnum.HZSG.getCode();
    /**
     * 定义一个杭果市场id用于区分上报数据表中数据
     */
    private Long HGMarketId = HangGuoTraceabilityDataJob.HGMarketId;

    /**
     * 商品信息
     */
    public void createCommodity(List<HangGuoCommodity> commodityList, Date createTime) {
        if (CollectionUtils.isEmpty(commodityList)) {
            logger.info("获取杭果商品数据为空");
            return;
        }
        StreamEx.of(commodityList).nonNull().forEach(c -> {
            c.setFirstCateg(c.getFirstCateg().trim());
            c.setSecondCateg(c.getSecondCateg().trim());
            c.setItemName(c.getItemName().trim());
            c.setItemNumber(c.getItemNumber().trim());
            c.setItemUnitName(c.getItemUnitName() == null ? null : c.getItemUnitName().trim());
        });
        List<String> list = StreamEx.of(commodityList).nonNull().map(c -> c.getItemNumber()).collect(Collectors.toList());
        List<Category> existsCateGoryList = getCategoryListByThirdCode(list);

        Set<String> existsCodeSet = new HashSet<>();
        StreamEx.of(existsCateGoryList).nonNull().forEach(u -> {
            existsCodeSet.add(u.getCode());
        });
        List<HangGuoCommodity> updateHangGuoCommodityList = new ArrayList<>();
        if (!existsCodeSet.isEmpty()) {
            updateHangGuoCommodityList = StreamEx.of(commodityList).nonNull().filter(trade -> existsCodeSet.contains(trade.getItemNumber())).collect(Collectors.toList());
        }

        if (CollectionUtils.isNotEmpty(updateHangGuoCommodityList)) {
            commodityList.removeAll(updateHangGuoCommodityList);
        }
        if (CollectionUtils.isNotEmpty(commodityList)) {
            addCateGory(commodityList, createTime);
        }
        if (CollectionUtils.isNotEmpty(updateHangGuoCommodityList)) {
            updateCateGoryByThirdCode(updateHangGuoCommodityList, createTime);
        }
    }

    /**
     * @return
     */
    private Map<String, UserInfo> getUserMap() {
        UserInfo u = new UserInfo();
        u.setMarketId(HGMarketId);
        u.setYn(YesOrNoEnum.YES.getCode());
        List<UserInfo> userList = userService.listByExample(u);
        return StreamEx.of(userList).nonNull().collect(Collectors.toMap(us -> us.getThirdPartyCode(), user -> user, (a, b) -> a));
    }


    /**
     * 交易信息
     *
     * @param tradeList
     * @param createTime
     */
    public void createTrade(List<HangGuoTrade> tradeList, Date createTime) {
        if (CollectionUtils.isEmpty(tradeList)) {
            logger.info("获取杭果交易数据为空");
            return;
        }

        //插入交易缓存表
        Integer batchSize = (insertBatchSize == null || insertBatchSize == 0) ? 1000 : insertBatchSize;
        // 分批数
        Integer part = tradeList.size() / batchSize;
        for (int i = 0; i <= part; i++) {
            Integer endPos = i == part ? tradeList.size() : (i + 1) * batchSize;
            List<HangGuoTrade> partBills = tradeList.subList(i * batchSize, endPos);

            StreamEx.of(partBills).nonNull().forEach(p -> {
                p.setRegisterNo(p.getRegisterNo() == null ? null : p.getRegisterNo().trim());
            });
            hangGuoDataService.batchInsertCacheTradeList(partBills);
        }
        //patch标志位
        updateCacheTradeReportFlag(createTime);
        //新增交易
        addTradeList(createTime);
        logger.info("============>>> 处理交易数据用时：" + (DateUtils.getCurrentDate().getTime() - createTime.getTime()));
    }

    /**
     * 新增交易单
     *
     * @param createTime
     */
    private void addTradeList(Date createTime) {

        List<HangGuoTrade> tradeList = getPendingHandleTradeList();
        if (CollectionUtils.isEmpty(tradeList)) {
            logger.info("处理交易单数据为空");
            return;
        }
        //插入交易缓存表
        Integer batchSize = (insertBatchSize == null || insertBatchSize == 0) ? 1000 : insertBatchSize;
        // 分批数
        Integer part = tradeList.size() / batchSize;
        for (int i = 0; i <= part; i++) {
            Integer endPos = i == part ? tradeList.size() : (i + 1) * batchSize;
            List<HangGuoTrade> partBills = tradeList.subList(i * batchSize, endPos);
            doPartTrade(partBills, createTime);
        }
    }

    /**
     * 新增交易信息到正式表
     *
     * @param tradeList
     * @param createTime
     */
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void doPartTrade(List<HangGuoTrade> tradeList, Date createTime) {
        List<String> userCode = StreamEx.of(tradeList).map(t -> t.getSupplierNo()).collect(Collectors.toList());
        List<String> commodityCode = new ArrayList<>();
        List<String> billNos = new ArrayList<>();
        for (HangGuoTrade t : tradeList) {
            if (t != null) {
                userCode.add(t.getMemberNo());
                commodityCode.add(t.getItemNumber());
                if (StringUtils.isNotBlank(t.getRegisterNo())) {
                    billNos.add(t.getRegisterNo());
                }
            }
        }
        //用户，商品
        userCode = StreamEx.of(userCode).nonNull().distinct().collect(Collectors.toList());
        commodityCode = StreamEx.of(commodityCode).nonNull().distinct().collect(Collectors.toList());
        billNos = StreamEx.of(billNos).nonNull().distinct().collect(Collectors.toList());
        List<UserInfo> userList = getUserListByThirdPartyCode(userCode);
        List<Category> categoryList = getCategoryListByThirdCode(commodityCode);
        if (CollectionUtils.isEmpty(userList) || CollectionUtils.isEmpty(categoryList)) {
            logger.info("交易数据没有对应的经营户或者商品,无法关联,不创建正式交易单");
            return;
        }
        Map<String, UserInfo> userMap = StreamEx.of(userList).nonNull().collect(Collectors.toMap(UserInfo::getThirdPartyCode, user -> user, (key1, key2) -> key1));
        Map<String, Category> categoryMap = StreamEx.of(categoryList).nonNull().collect(Collectors.toMap(Category::getCode, category -> category, (key1, key2) -> key1));

        //报备单库存map
        Map<Long, TradeDetail> billMap = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(billNos)) {
            billMap = getTradeBillMap(billNos);
        }
        //新增主单
        List<TradeOrder> addTradeList = getTradeOrderList(tradeList, userMap, createTime);
        if (CollectionUtils.isNotEmpty(addTradeList)) {
            tradeOrderService.batchInsert(addTradeList);
        }
        Map<String, TradeOrder> orderMap = StreamEx.of(addTradeList).nonNull().collect(Collectors.toMap(TradeOrder::getTradeNo, Function.identity(), (key1, key2) -> key1));

        //新增request
        List<TradeRequest> addTradeRequestList = getTradeOrderRequestList(tradeList, userMap, categoryMap, orderMap, billMap, createTime);
        if (CollectionUtils.isNotEmpty(addTradeRequestList)) {
            tradeRequestService.batchInsert(addTradeRequestList);
        }
        Map<String, TradeRequest> tradeRequestMap = StreamEx.of(addTradeRequestList).nonNull().collect(Collectors.toMap(TradeRequest::getThirdTradeNo, Function.identity(), (a, b) -> a));
        //新增detail
        List<TradeDetail> addDetailList = getTradeOrderDetailRequestList(tradeList, userMap, categoryMap, billMap, tradeRequestMap, createTime);
        if (CollectionUtils.isNotEmpty(addDetailList)) {
            hangGuoDataService.batchInsertTradeDetail(addDetailList);
        }

        //更新标志位
        updateCacheTradeHandleFlag(DataHandleFlagEnum.PROCESSED.getCode(), tradeList);
    }

    /**
     * @param handleFlag
     * @param tradeList
     */
    private void updateCacheTradeHandleFlag(Integer handleFlag, List<HangGuoTrade> tradeList) {
        if (CollectionUtils.isNotEmpty(tradeList)) {
            Map<String, Object> map = new HashMap<>();
            map.put("list", tradeList);
            map.put("handleFlag", handleFlag);
            hangGuoDataService.batchUpdateCacheTradeHandleFlag(map);
        }
    }


    /**
     * 生成交易单详情
     *
     * @param tradeList
     * @param userMap
     * @param categoryMap
     * @param billMap
     * @param tradeRequestMap
     * @param createTime
     * @return
     */
    private List<TradeDetail> getTradeOrderDetailRequestList(List<HangGuoTrade> tradeList, Map<String, UserInfo> userMap, Map<String, Category> categoryMap,
                                                             Map<Long, TradeDetail> billMap, Map<String, TradeRequest> tradeRequestMap, Date createTime) {
        List<TradeDetail> detailList = new ArrayList<>();
        Integer isBatched = 1;
        StreamEx.of(tradeList).nonNull().forEach(t -> {
            Long parentId = null;
            Long checkInId = null;
            Long billId = Long.valueOf(-1);
            if (null != billMap && !billMap.isEmpty()) {
                TradeDetail detail = billMap.get(t.getRegisterId());
                if (null != detail) {
                    parentId = detail.getId();
                    checkInId = detail.getCheckinRecordId();
                    billId = detail.getBillId();
                }
            }

            Long buyId = userMap.get(t.getMemberNo().trim()).getId();
            String buyName = userMap.get(t.getMemberNo().trim()).getName();
            Long sellerId = userMap.get(t.getSupplierNo().trim()).getId();
            String sellerName = userMap.get(t.getSupplierNo().trim()).getName();
            String productName = categoryMap.get(t.getItemNumber().trim()).getName();
            BigDecimal weight = new BigDecimal(t.getWeight());
            String nowDate = DateUtils.format(DateUtils.getCurrentDate());
            Long tradeRequestId = tradeRequestMap.get(t.getTradeNo().trim()).getId();

            TradeDetail tradeDetail = new TradeDetail();
            tradeDetail.setCheckinRecordId(checkInId);

            tradeDetail.setBillId(billId);

            tradeDetail.setParentId(parentId);
            tradeDetail.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
            tradeDetail.setCheckoutStatus(CheckoutStatusEnum.NONE.getCode());
            tradeDetail.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
            tradeDetail.setTradeType(TradeTypeEnum.SEPARATE_SALES.getCode());
            tradeDetail.setBuyerId(buyId);
            tradeDetail.setBuyerName(buyName);
            tradeDetail.setSellerId(sellerId);
            tradeDetail.setSellerName(sellerName);
            tradeDetail.setProductName(productName);
            tradeDetail.setStockWeight(weight);
            tradeDetail.setTotalWeight(weight);
            tradeDetail.setWeightUnit(WeightUnitEnum.KILO.getCode());

            tradeDetail.setTradeRequestId(tradeRequestId);

            tradeDetail.setCreated(createTime);
            tradeDetail.setModified(createTime);
            tradeDetail.setIsBatched(isBatched);
            tradeDetail.setBatchNo(nowDate);
            tradeDetail.setParentBatchNo(nowDate);

            detailList.add(tradeDetail);
        });
        return detailList;
    }

    /**
     * @param addSize
     * @param now
     * @return
     */
    private Map<String, String> generNextCode(int addSize, LocalDateTime now) {
        int tradeRequestSize = 5;
        String preKey = "prefix";
        String seqKey = "seq";
        CodeGenerate codeGenerate = new CodeGenerate();
        //codeGenerate.setType(TRADE_REQUEST_CODE_TYPE);
        //List<CodeGenerate> codeGenerateList = codeGenerateService.listByExample(codeGenerate);
        List<CodeGenerate> codeGenerateList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(codeGenerateList)) {
            codeGenerate = codeGenerateList.get(0);
        }
        if (codeGenerate == null) {
            throw new TraceBizException("生成编号错误");
        }
        String nextSegment = now.format(DateTimeFormatter.ofPattern(codeGenerate.getPattern()));
        if (!nextSegment.equals(codeGenerate.getSegment())) {
            codeGenerate.setSeq(1L);
            codeGenerate.setSegment(nextSegment);
            codeGenerate.setModified(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));
        } else {
            codeGenerate.setSeq(codeGenerate.getSeq() + addSize);
            codeGenerate.setModified(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));
        }
        //codeGenerateService.updateSelective(codeGenerate);
        Map<String, String> map = new HashMap<>(16);
        map.put(preKey, StringUtils.trimToEmpty(codeGenerate.getPrefix()).concat(nextSegment));
        map.put(seqKey, String.valueOf(codeGenerate.getSeq()));
        return map;
    }

    /**
     * 生成交易请求
     *
     * @param tradeList
     * @param userMap
     * @param categoryMap
     * @param orderMap
     * @param billMap
     * @param createTime
     * @return
     */
    private List<TradeRequest> getTradeOrderRequestList(List<HangGuoTrade> tradeList, Map<String, UserInfo> userMap, Map<String, Category> categoryMap, Map<String, TradeOrder> orderMap, Map<Long, TradeDetail> billMap, Date createTime) {
        List<TradeRequest> requestList = new ArrayList<>();
        BigDecimal reportMaxAmount = new BigDecimal(reportMaxAmountInt);
        //createTime
        LocalDateTime dateTime = Instant.ofEpochMilli(createTime.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        String preKey = "prefix";
        String seqKey = "seq";
        Map<String, String> map = generNextCode(tradeList.size(), dateTime);
        String codePrefix = map.get(preKey);
        String codeSeq = map.get(seqKey);
        AtomicInteger index = new AtomicInteger(0);
        StreamEx.of(tradeList).nonNull().forEach(t -> {
            int currentIndex = index.getAndIncrement();
            String code = codePrefix;
            Integer codeSeqInt = Integer.valueOf(codeSeq) + currentIndex;
            code = code.concat(String.valueOf(codeSeqInt));
            logger.info("===>生成编号为：" + code);
            Long buyId = userMap.get(t.getMemberNo().trim()).getId();
            String buyName = userMap.get(t.getMemberNo().trim()).getName();
            Long sellerId = userMap.get(t.getSupplierNo().trim()).getId();
            String sellerName = userMap.get(t.getSupplierNo().trim()).getName();
            Long orderId = orderMap.get(t.getTradeNo().trim()).getId();
            Long buyMarketId = userMap.get(t.getMemberNo().trim()).getMarketId();
            Long sellerMarketId = userMap.get(t.getSupplierNo().trim()).getMarketId();
            Long productStockId = null;
            if (null != billMap && !billMap.isEmpty() && StringUtils.isNotBlank(t.getRegisterId())) {
                if (null != billMap.get(t.getRegisterId())) {
                    productStockId = billMap.get(t.getRegisterId()).getProductStockId();
                }
            }
            String productName = categoryMap.get(t.getItemNumber().trim()).getName();
            TradeRequest request = new TradeRequest();
            request.setBuyerId(buyId);
            request.setBuyerName(buyName);
            request.setSellerId(sellerId);
            request.setSellerName(sellerName);
            request.setTradeWeight(new BigDecimal(t.getWeight()));
            request.setTradeOrderId(orderId);
            request.setProductStockId(productStockId);
            request.setCreated(createTime);
            request.setModified(createTime);
            request.setReturnStatus(TradeReturnStatusEnum.NONE.getCode());

            request.setWeightUnit(WeightUnitEnum.KILO.getCode());
            request.setProductName(productName);
            request.setHandleTime(createTime);
            request.setThirdTradeNo(t.getTradeNo());
            request.setBatchNo(t.getBatchNo());
            request.setOriginName(t.getOriginName());
            request.setPositionNo(t.getPositionNo());
            request.setPositionName(t.getPositionName());
            request.setPrice(t.getPrice());
            request.setPackageNumber(t.getPackageNumber());
            request.setNumber(t.getNumber());
            request.setAmount(t.getAmount());
            request.setPosNo(t.getPosNo());
            request.setPayWay(t.getPayWay());
            request.setTotalAmount(t.getTotalAmount());
            request.setOperator(t.getOperator());
            request.setPayer(t.getPayer());
            request.setPayNo(t.getPayNo());
            request.setSourceType(TradeRequestSourceTypeEnum.THIRD_HANGGUO.getCode());
            if (null != t.getAmount() && t.getAmount().compareTo(reportMaxAmount) >= 0) {
                request.setReportFlag(CheckOrderReportFlagEnum.UNTREATED.getCode());
            } else {
                request.setReportFlag(CheckOrderReportFlagEnum.PROCESSED.getCode());
            }
            request.setCode(code);
            requestList.add(request);
        });
        return requestList;
    }


    /**
     * 生成交易主单
     *
     * @param tradeList
     * @param userMap
     * @param createTime
     * @return
     */
    private List<TradeOrder> getTradeOrderList(List<HangGuoTrade> tradeList, Map<String, UserInfo> userMap, Date createTime) {
        List<TradeOrder> orderList = new ArrayList<>();
        StreamEx.of(tradeList).nonNull().forEach(t -> {
            Long buyId = userMap.get(t.getMemberNo().trim()).getId();
            String buyName = userMap.get(t.getMemberNo().trim()).getName();
            Long buyMarketId = userMap.get(t.getMemberNo().trim()).getMarketId();
            Long sellerId = userMap.get(t.getSupplierNo().trim()).getId();
            String sellerName = userMap.get(t.getSupplierNo().trim()).getName();
            Long sellerMarketId = userMap.get(t.getSupplierNo().trim()).getMarketId();
            TradeOrder order = new TradeOrder();
            order.setBuyerId(buyId);
            order.setBuyerName(buyName);
            order.setSellerId(sellerId);
            order.setSellerName(sellerName);
//            order.setOrderStatus(TradeOrderStatusEnum.FINISHED.getCode());
            order.setOrderType(TradeOrderTypeEnum.BUY.getCode());
            order.setCreated(createTime);
            order.setModified(createTime);
            order.setTradeNo(t.getTradeNo());
            orderList.add(order);
        });
        return orderList;
    }

    /**
     * @param billNos
     * @return
     */
    private Map<Long, TradeDetail> getTradeBillMap(List<String> billNos) {
        List<RegisterBill> billList = hangGuoDataService.getRegisterBillByIds(billNos);
        List<String> billIds = StreamEx.of(billList).nonNull().map(b -> b.getCode()).collect(Collectors.toList());
        List<TradeDetail> orderDetailList = hangGuoDataService.getBillTradeDetailListByBillIds(billIds);
        return StreamEx.of(orderDetailList).nonNull().collect(Collectors.toMap(TradeDetail::getBillId, Function.identity(), (a, b) -> a));
    }

    /**
     * @param userCode
     * @return
     */
    private List<UserInfo> getUserListByThirdPartyCode(List<String> userCode) {
        if (CollectionUtils.isEmpty(userCode)) {
            return null;
        }
        return hangGuoDataService.getUserListByThirdPartyCode(userCode);
    }

    /**
     * 待处理交易数据
     *
     * @return
     */
    private List<HangGuoTrade> getPendingHandleTradeList() {
        HangGuoTrade trade = new HangGuoTrade();
        trade.setHandleFlag(DataHandleFlagEnum.PENDING_HANDLE.getCode());
        return hangGuoDataService.selectTradeReportListByHandleFlag(trade);
    }

    /**
     * @param createTime
     */
    private void updateCacheTradeReportFlag(Date createTime) {

        //获取其用户商品集合
        Category category = new Category();
        category.setMarketId(HGMarketId);
        List<Category> categoryList = categoryService.list(category);
        Map<String, UserInfo> userMap = getUserMap();
        Map<String, Category> categoryMap = StreamEx.of(categoryList).nonNull().collect(Collectors.toMap(Category::getCode, c -> c, (a, b) -> a));

        //patch由于没有对应用户或商品导致之前的交易单没有处理
        updateCacheTradeReportFlagToTrue(userMap, categoryMap, createTime);
        //大于50W的交易金额patch为无需上报,交易单对应商品或用户关联不到为无需处理
        updateCacheTradeReportFlagToFalse(userMap, categoryMap, createTime);

    }

    /**
     * @param userMap
     * @param categoryMap
     * @param createTime
     */
    private void updateCacheTradeReportFlagToFalse(Map<String, UserInfo> userMap, Map<String, Category> categoryMap, Date createTime) {
        //大于指定的交易金额patch为无需上报
        hangGuoDataService.updateTradeReportListByBeyondAmount(reportMaxAmountInt);
        //交易单对应商品或用户关联不到
        HangGuoTrade trade = new HangGuoTrade();
        trade.setHandleFlag(DataHandleFlagEnum.PENDING_HANDLE.getCode());
        List<HangGuoTrade> tradeList = hangGuoDataService.selectTradeReportListByHandleFlag(trade);
        List<HangGuoTrade> collect = StreamEx.of(tradeList).nonNull().filter(t -> !userMap.containsKey(t.getMemberNo().trim())
                || !userMap.containsKey(t.getSupplierNo().trim()) || !categoryMap.containsKey(t.getItemNumber().trim())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
            String handleRemark = "交易单对应商品或用户无法关联，标记为暂不处理";
            //分批处理
            Integer batchSize = (insertBatchSize == null || insertBatchSize == 0) ? 1000 : insertBatchSize;
            // 分批数
            Integer part = collect.size() / batchSize;
            for (int i = 0; i <= part; i++) {
                Integer endPos = i == part ? collect.size() : (i + 1) * batchSize;
                List<HangGuoTrade> partBills = collect.subList(i * batchSize, endPos);

                Map<String, Object> map = new HashMap<>(16);
                map.put("list", partBills);
                map.put("handleRemark", handleRemark);
                map.put("handleFlag", DataHandleFlagEnum.UN_NEED_HANDLE.getCode());
                hangGuoDataService.batchUpdateCacheTradeHandleFlag(map);
            }


        }
    }

    /**
     * 将无需处理交易数据patch一次标志位
     *
     * @param createTime
     */
    private void updateCacheTradeReportFlagToTrue(Map<String, UserInfo> userMap, Map<String, Category> categoryMap, Date createTime) {
        //查询当前缓存表中无需处理的交易数据昨天-今天
        HangGuoTrade trade = new HangGuoTrade();
        trade.setHandleFlag(DataHandleFlagEnum.UN_NEED_HANDLE.getCode());
        List<HangGuoTrade> tradeList = hangGuoDataService.selectTradeReportListByHandleFlag(trade);
        if (CollectionUtils.isEmpty(tradeList)) {
            logger.info("==>暂无状态为 暂时无需处理 的数据");
            return;
        }
        // 判断是否后续拉渠道商品或者经营户，从而需要处理交易单
        List<HangGuoTrade> trades = new ArrayList<>();
        StreamEx.of(tradeList).nonNull().forEach(c -> {
            boolean needUpdate = userMap.containsKey(c.getMemberNo().trim()) && userMap.containsKey(c.getSupplierNo().trim()) && categoryMap.containsKey(c.getItemNumber().trim());
            if (needUpdate) {
                trades.add(c);
            }
        });
        //将需要更新的集合patch为待处理
        if (CollectionUtils.isNotEmpty(trades)) {
            //分批处理
            Integer batchSize = (insertBatchSize == null || insertBatchSize == 0) ? 1000 : insertBatchSize;
            // 分批数
            Integer part = trades.size() / batchSize;
            for (int i = 0; i <= part; i++) {
                Integer endPos = i == part ? trades.size() : (i + 1) * batchSize;
                List<HangGuoTrade> partBills = trades.subList(i * batchSize, endPos);

                Map<String, Object> map = new HashMap<>(16);
                map.put("list", partBills);
                map.put("handleFlag", DataHandleFlagEnum.PENDING_HANDLE.getCode());
                hangGuoDataService.batchUpdateCacheTradeHandleFlag(map);
            }
        }
    }

    /**
     * 新增商品到正式表
     *
     * @param commodityList
     * @param createTime
     */
    private void addCateGory(List<HangGuoCommodity> commodityList, Date createTime) {

        List<Category> categoryList = conversionCommodityList(commodityList, createTime);

        //先将杭果商品插入到溯源系统
        hangGuoDataService.bachInsertCommodityList(categoryList);
        //patch当天杭果商品的parent_id
        Category category = new Category();
        category.setType(CategoryTypeEnum.SUPPLEMENT.getCode());
        category.setCreated(createTime);
        category.setMarketId(HGMarketId);
        hangGuoDataService.updateHangGuoCommodityParent(category);

        hangGuoCategoryLevelFaultTolerant();
    }

    /**
     * 杭果商品容错处理,由于其上级品种编码与预定规则不符时,作一次特殊处理patch
     */
    private void hangGuoCategoryLevelFaultTolerant() {
        Category category = new Category();
        category.setMarketId(HGMarketId);
        List<Category> categoryFaultList = hangGuoDataService.getCategoryFaultList(category);
        StreamEx.of(categoryFaultList).nonNull().forEach(c -> {
            if (StringUtils.isNotBlank(c.getParentCode())) {
                String parentCode = c.getParentCode();
                if (StringUtils.isNotBlank(parentCode)) {
                    recursionCategoty(c, c.getParentCode(), 3);
                }
            }
        });
        if (CollectionUtils.isNotEmpty(categoryFaultList)) {
            logger.info("商品断层处理,处理数据量:" + categoryFaultList.size());
            categoryService.batchUpdate(categoryFaultList);
        }
    }

    /**
     * 递归处理
     *
     * @param c
     * @param count
     */
    private void recursionCategoty(Category c, String parentCode, int count) {
        if (count <= 0) {
            logger.info("递归结束");
            return;
        }
        logger.info("递归处理商品父节点,parentCode:" + parentCode + " |count:" + count);
        parentCode = turnSubStr(parentCode);
        Category parentCategory = hangGuoDataService.getCategoryByThirdCode(parentCode);
        if (null != parentCategory) {
            c.setParentCode(parentCategory.getCode());
            c.setParentId(parentCategory.getId());
        } else {
            count--;
            recursionCategoty(c, parentCode, count);
        }
    }

    /**
     * @param parentCode
     * @return
     */
    private String turnSubStr(String parentCode) {
        if (StringUtils.isNotBlank(parentCode)) {
            return parentCode.substring(0, parentCode.length() - 1);
        }
        return null;
    }


    /**
     * 更新杭果商品
     *
     * @param updateHangGuoCommodityList
     * @param createTime
     */
    private void updateCateGoryByThirdCode(List<HangGuoCommodity> updateHangGuoCommodityList, Date createTime) {
        List<Category> categoryList = conversionCommodityList(updateHangGuoCommodityList, createTime);
        //更新商品信息
        hangGuoDataService.batchUpdateCategoryByThirdCode(categoryList);
    }

    /**
     * 杭果商品转换为溯源商品
     *
     * @param commodityList
     * @param createTime
     * @return
     */
    private List<Category> conversionCommodityList(List<HangGuoCommodity> commodityList, Date createTime) {
        List<Category> categoryList = new ArrayList();
        //品种集合
        CopyOnWriteArraySet<String> varietySet = new CopyOnWriteArraySet<>();
        StreamEx.of(commodityList).nonNull().forEach(c -> {
            Category category = new Category();
            category.setFullName(c.getItemName() == null ? null : c.getItemName().trim());
            category.setName(c.getItemName() == null ? null : c.getItemName().trim());
            category.setSpecification(c.getItemUnitName() == null ? null : c.getItemUnitName().trim());
            category.setCode(c.getItemNumber() == null ? null : c.getItemNumber().trim());
            category.setCreated(createTime);
            category.setModified(createTime);
            category.setMarketId(HGMarketId);
            category.setType(CategoryTypeEnum.SUPPLEMENT.getCode());
            setCommodityLevel(category, c);

            varietySet.add(c.getCategoryNumber());
            categoryList.add(category);
        });
        //第5层级单独处理
        StreamEx.of(categoryList).nonNull().forEach(category -> {
            Integer categoryLevel = category.getLevel();
            if (categoryLevel.equals(HangGuoGoodsLevelEnum.GOODS_FIVE.getCode())) {
                String goodsCode = category.getCode().trim();
                String parentGoodsCode = goodsCode.substring(0, goodsCode.length() - 1);
                //商品码删除最后一位后与品种码一致时为第四层级
                if (varietySet.contains(parentGoodsCode)) {
                    category.setLevel(HangGuoGoodsLevelEnum.GOODS_FOUR.getCode());
                } else {
                    category.setParentCode(parentGoodsCode);
                }
            }
        });
        return categoryList;
    }

    /**
     * 获取商品等级
     *
     * @param category
     * @param commodity
     * @return
     */
    private void setCommodityLevel(Category category, HangGuoCommodity commodity) {
        String goodsCode = commodity.getItemNumber().trim();
        String first = commodity.getFirstCateg().trim();
        String second = commodity.getSecondCateg().trim();
        String categoryCode = commodity.getCategoryNumber().trim();
        category.setIsShow(CategoryIsShowEnum.IS_SHOW.getCode());
        //商品编码与大类码一致，商品为第一层级.无parentId
        if (goodsCode.equals(first)) {
            category.setLevel(HangGuoGoodsLevelEnum.GOODS_ONE.getCode());
        }
        //商品编码与小类码一致，商品为第二层级
        else if (goodsCode.equals(second)) {
            category.setLevel(HangGuoGoodsLevelEnum.GOODS_TWO.getCode());
            category.setParentCode(first);
        }
        //商品编码与品种码一致，商品为第三层级
        else if (goodsCode.equals(categoryCode)) {
            category.setLevel(HangGuoGoodsLevelEnum.GOODS_THREE.getCode());
            category.setParentCode(second);
        } else {
            category.setLevel(HangGuoGoodsLevelEnum.GOODS_FIVE.getCode());
            Integer psize = 5;
            String parentGoodsCode = categoryCode;
            if (categoryCode.length() > psize) {
                parentGoodsCode = categoryCode.substring(0, categoryCode.length() - 1);
            }
            category.setParentCode(parentGoodsCode);
            category.setIsShow(CategoryIsShowEnum.NOT_SHOW.getCode());
        }
    }

    /**
     * @param commodityCode
     * @return
     */
    private List<Category> getCategoryListByThirdCode(List<String> commodityCode) {
        if (CollectionUtils.isEmpty(commodityCode)) {
            return null;
        }
        return hangGuoDataService.getCategoryListByThirdCode(commodityCode);
    }


}
