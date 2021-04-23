package com.dili.trace.service;

import com.dili.ss.base.BaseService;
import com.dili.trace.domain.*;
import com.dili.trace.domain.Category;
import com.dili.trace.domain.hangguo.HangGuoTrade;
import com.dili.trace.domain.hangguo.HangGuoUser;
import com.dili.trace.dto.PushDataQueryDto;
import com.dili.trace.dto.thirdparty.report.ReportInspectionDto;
import com.dili.trace.dto.thirdparty.report.ReportScanCodeOrderDto;
import com.dili.trace.dto.thirdparty.report.ReportUnqualifiedDisposalDto;

import java.util.List;
import java.util.Map;

/**
 * @author asa.lee
 */
public interface HangGuoDataService extends BaseService<HangGuoUser, Long> {

    /**
     * 杭果商品信息存储到溯源系统
     *
     * @param commodityList
     */
    void bachInsertCommodityList(List<Category> commodityList);

    /**
     * 杭果商品插入后更新parentId
     *
     * @param category
     */
    void updateHangGuoCommodityParent(Category category);

    /**
     * 根据第三方编码获取用户更新列表
     *
     * @param list
     * @return
     */
    List<UserInfo> getUserListByThirdPartyCode(List<String> list);

    /**
     * 根据第三方编码更新用户
     *
     * @param updateUserList
     */
    void batchUpdateUserByThirdCode(List<UserInfo> updateUserList);

    /**
     * 根据商品编码获取商品列表
     *
     * @param codeList
     * @return
     */
    List<Category> getCategoryListByThirdCode(List<String> codeList);

    /**
     * 根据第三方编码删除商品
     *
     * @param categoryList
     *//*
    void deleteHangGuoCommodityByThirdCode(List<Category> categoryList);*/

    /**
     * 插入交易缓存数据
     *
     * @param tradeList
     */
    void batchInsertCacheTradeList(List<HangGuoTrade> tradeList);

    /**
     * 根据报备单id获取报备详情
     *
     * @param billIds
     * @return
     */
    List<TradeDetail> getBillTradeDetailListByBillIds(List<String> billIds);

    /**
     * 根据ids获取stock详情
     *
     * @param stockIdList
     * @return
     */
    List<ProductStock> getProductStockListByIds(List<Long> stockIdList);

    /**
     * 更新超过金额标志位
     *
     * @param reportMaxAmountInt
     * @return
     */
    void updateTradeReportListByBeyondAmount(Integer reportMaxAmountInt);

    /**
     * 查询的交易数据
     *
     * @param trade
     * @return
     */
    List<HangGuoTrade> selectTradeReportListByHandleFlag(HangGuoTrade trade);

    /**
     * 根据报备单id获取报备单号
     *
     * @param ids
     * @return
     */
    List<RegisterBill> getRegisterBillByIds(List<String> ids);

    /**
     * 批量插入tradeList（由于Column同名导致batchinsert无法调用）
     *
     * @param addDetailList
     */
    void batchInsertTradeDetail(List<TradeDetail> addDetailList);

    /**
     * 更新交易数据处理标志位
     *
     * @param map
     */
    void batchUpdateCacheTradeHandleFlag(Map<String, Object> map);

    /**
     * 获取检测值
     *
     * @param headList
     * @return
     */
    List<CheckOrderData> getCheckOrderDataList(List<CheckOrder> headList);

    /**
     * 获取检测主单
     *
     * @param checkOrder
     * @return
     */
    List<CheckOrder> getReportCheckOrderList(CheckOrder checkOrder);

    /**
     * 不合格处置主单
     *
     * @param dispose
     * @return
     */
    List<CheckOrderDispose> getReportCheckOrderDisposeList(CheckOrderDispose dispose);

    /**
     * 获取来源缓存表数据
     *
     * @param que
     * @return
     */
    ThirdPartySourceData getThirdPartySourceData(ThirdPartySourceData que);

    /**
     * 插入缓存表数据
     *
     * @param addSource
     */
    void insertThirdPartySourceData(ThirdPartySourceData addSource);

    /**
     * 获取杭果交易数据
     *
     * @param queryDto
     * @return
     */
    List<ReportScanCodeOrderDto> getHangGuoScanOrderReport(PushDataQueryDto queryDto);

    /**
     * 更新上报标志位
     *
     * @param inspectionDtoList
     */
    void updateCheckOrderReportFlag(List<ReportInspectionDto> inspectionDtoList);

    /**
     * 更新处置上报标志位
     *
     * @param disposalDtos
     */
    void updateCheckOrderDisposeReportFlag(List<ReportUnqualifiedDisposalDto> disposalDtos);
    /*
     */

    /**
     * 获取不符合规范的断层商品列表
     *
     * @param category
     * @return
     */
    List<Category> getCategoryFaultList(Category category);

    /**
     * 获取商品详情
     *
     * @param parentCode
     * @return
     */
    Category getCategoryByThirdCode(String parentCode);

    /**
     * 更新商品信息
     *
     * @param categoryList
     */
    void batchUpdateCategoryByThirdCode(List<Category> categoryList);
}
