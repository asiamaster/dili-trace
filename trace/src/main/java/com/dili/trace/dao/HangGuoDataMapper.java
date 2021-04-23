package com.dili.trace.dao;

import com.dili.ss.base.MyMapper;
import com.dili.trace.domain.*;
import com.dili.trace.domain.Category;
import com.dili.trace.domain.hangguo.HangGuoTrade;
import com.dili.trace.domain.hangguo.HangGuoUser;
import com.dili.trace.dto.PushDataQueryDto;
import com.dili.trace.dto.thirdparty.report.ReportInspectionDto;
import com.dili.trace.dto.thirdparty.report.ReportScanCodeOrderDto;
import com.dili.trace.dto.thirdparty.report.ReportUnqualifiedDisposalDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author asa.lee
 */
public interface HangGuoDataMapper extends MyMapper<HangGuoUser> {

    /**
     * 插入商品数据到正式表
     *
     * @param commodityList
     */
    void bachInsertCommodityList(@Param("list") List<Category> commodityList);

    /**
     * 更新商品正式表杭果商品数据patentId
     *
     * @param category
     */
    void updateHangGuoCommodityParent(Category category);

    /**
     * 根据第三方编码获取更新列表
     *
     * @param list
     * @return
     */
    List<UserInfo> getUserListByThirdPartyCode(@Param("list") List<String> list);

    /**
     * 根据第三方编码更新用户信息
     *
     * @param updateUserList
     */
    void batchUpdateUserByThirdCode(@Param("list") List<UserInfo> updateUserList);

    /**
     * 根据第三方编码更新商品信息
     *
     * @param tradeList
     * @return
     */
    List<Category> getCategoryListByThirdCode(@Param("list") List<String> tradeList);

    /**
     * 根据第三方编码刪除商品信息
     *
     * @param categoryList
     */
//    void deleteHangGuoCommodityByThirdCode(@Param("list") List<Category> categoryList);

    /**
     * 插入交易缓存表
     *
     * @param tradeList
     */
    void bachInsertCacheTradeList(@Param("list") List<HangGuoTrade> tradeList);

    /**
     * 根据报备单id获取报备详情
     *
     * @param billIds
     * @return
     */
    List<TradeDetail> getBillTradeDetailListByBillIds(@Param("list") List<String> billIds);

    /**
     * 根基id获取库存
     *
     * @param stockIdList
     * @return
     */
    List<ProductStock> getProductStockListByIds(@Param("list") List<Long> stockIdList);

    /**
     * 超过金额patch为不上报
     * @param reportMaxAmountInt
     */
    void updateTradeReportListByBeyondAmount(@Param("maxAmount") Integer reportMaxAmountInt);

    /**
     * 查询所有交易数据
     *
     * @param trade
     * @return
     */
    List<HangGuoTrade> selectTradeReportListByHandleFlag(HangGuoTrade trade);

    /**
     * 更新交易数据标志位
     *
     * @param map
     */
    void batchUpdateCacheTrade(Map<String, Object> map);

    /**
     * 获取报备单编号
     *
     * @param ids
     * @return
     */
    List<RegisterBill> getRegisterBillByIds(List<String> ids);

    /**
     * 批量插入tradeDetail
     *
     * @param addDetailList
     */
    void batchInsertTradeDetail(List<TradeDetail> addDetailList);

    /**
     * 获取检测详情
     * @param headList
     * @return
     */
    List<CheckOrderData> getCheckOrderDataList(List<CheckOrder> headList);
    /**
     * 获取检测主单
     * @param checkOrder
     */
    List<CheckOrder> getReportCheckOrderList(CheckOrder checkOrder);
    /**
     * 不合格处置主单
     * @param dispose
     * @return
     */
    List<CheckOrderDispose> getReportCheckOrderDisposeList(CheckOrderDispose dispose);

    /**
     * 查询缓存表
     * @param que
     * @return
     */
    ThirdPartySourceData getThirdPartySourceData(ThirdPartySourceData que);

    /**
     * 插入缓存表
     * @param addSource
     */
    void insertThirdPartySourceData(ThirdPartySourceData addSource);

    /**
     * 获取杭果交易数据
     * @param queryDto
     * @return
     */
    List<ReportScanCodeOrderDto> getHangGuoScanOrderReport(PushDataQueryDto queryDto);

    /**
     * 更新检测主单上报标志位
     * @param inspectionDtoList
     */
    void updateCheckOrderReportFlag(List<ReportInspectionDto> inspectionDtoList);

    /**
     * 更新检测处置单上报标志位
     * @param disposalDtos
     */
    void updateCheckOrderDisposeReportFlag(List<ReportUnqualifiedDisposalDto> disposalDtos);

    /**
     * 获取商品断层列表
     * @param category
     * @return
     */
    List<Category> getCategoryFaultList(Category category);

    /**
     * 获取商品按code
     * @param parentCode
     * @return
     */
    Category getCategoryByThirdCode(@Param("code") String parentCode);

    /**
     * 更新商品信息
     * @param categoryList
     */
    void batchUpdateCategoryByThirdCode(@Param("list") List<Category> categoryList);

    /**
     * 根据UapId更新parentId
     * @param category
     */
    void updateParentByUapId(Category category);


    /**
     * 新增或忽略
     *
     * @param category
     * @return
     */
    public int insertIgnoreCategory(Category category);


}