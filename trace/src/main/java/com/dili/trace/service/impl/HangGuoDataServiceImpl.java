package com.dili.trace.service.impl;

import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.util.DateUtils;
import com.dili.trace.dao.HangGuoDataMapper;
import com.dili.trace.domain.*;
import com.dili.trace.domain.Category;
import com.dili.trace.domain.hangguo.HangGuoTrade;
import com.dili.trace.domain.hangguo.HangGuoUser;
import com.dili.trace.dto.PushDataQueryDto;
import com.dili.trace.dto.thirdparty.report.ReportInspectionDto;
import com.dili.trace.dto.thirdparty.report.ReportScanCodeOrderDto;
import com.dili.trace.dto.thirdparty.report.ReportUnqualifiedDisposalDto;
import com.dili.trace.service.HangGuoDataService;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author asa.lee
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class HangGuoDataServiceImpl extends BaseServiceImpl<HangGuoUser, Long> implements HangGuoDataService {
    private static final Logger logger = LoggerFactory.getLogger(BaseServiceImpl.class);
    /**
     * patch 交易数据缓存标志位时间段 往前24小时
     */
    private Integer lastDayHour = -24;
    /**
     * 最大处理条数，超出的则在下一次执行job中再处理
     */
    private Integer maxPageCountSize = 15000;
    @Autowired
    private HangGuoDataMapper hangGuoDataMapper;

    @Override
    public void bachInsertCommodityList(List<Category> commodityList) {
        hangGuoDataMapper.bachInsertCommodityList(commodityList);
    }

    @Override
    public void updateHangGuoCommodityParent(Category category) {
        hangGuoDataMapper.updateHangGuoCommodityParent(category);
    }

    @Override
    public List<UserInfo> getUserListByThirdPartyCode(List<String> list) {
        return hangGuoDataMapper.getUserListByThirdPartyCode(list);
    }

    @Override
    public void batchUpdateUserByThirdCode(List<UserInfo> updateUserList) {
        hangGuoDataMapper.batchUpdateUserByThirdCode(updateUserList);
    }

    @Override
    public List<Category> getCategoryListByThirdCode(List<String> codeList) {
        return hangGuoDataMapper.getCategoryListByThirdCode(codeList);
    }

    /*@Override
    public void deleteHangGuoCommodityByThirdCode(List<Category> categoryList) {
        hangGuoDataMapper.deleteHangGuoCommodityByThirdCode(categoryList);
    }*/

    @Override
    public void batchInsertCacheTradeList(List<HangGuoTrade> tradeList) {
        hangGuoDataMapper.bachInsertCacheTradeList(tradeList);
    }

    @Override
    public List<TradeDetail> getBillTradeDetailListByBillIds(List<String> billIds) {
        return hangGuoDataMapper.getBillTradeDetailListByBillIds(billIds);
    }

    @Override
    public List<ProductStock> getProductStockListByIds(List<Long> stockIdList) {
        return hangGuoDataMapper.getProductStockListByIds(stockIdList);
    }

    @Override
    public void updateTradeReportListByBeyondAmount(Integer reportMaxAmountInt) {
        hangGuoDataMapper.updateTradeReportListByBeyondAmount(reportMaxAmountInt);
    }

    @Override
    public List<HangGuoTrade> selectTradeReportListByHandleFlag(HangGuoTrade trade) {
        trade.setCreatedStart(DateUtils.addHours(DateUtils.getCurrentDate(), lastDayHour));
        trade.setCreatedEnd(DateUtils.getCurrentDate());
        int firstPage = 1;
        PageHelper.startPage(firstPage, maxPageCountSize);
        return hangGuoDataMapper.selectTradeReportListByHandleFlag(trade);
    }

    @Override
    public List<RegisterBill> getRegisterBillByIds(List<String> ids) {
        return hangGuoDataMapper.getRegisterBillByIds(ids);
    }

    @Override
    public void batchInsertTradeDetail(List<TradeDetail> addDetailList) {
        hangGuoDataMapper.batchInsertTradeDetail(addDetailList);
    }

    @Override
    public void batchUpdateCacheTradeHandleFlag(Map<String, Object> map) {
        hangGuoDataMapper.batchUpdateCacheTrade(map);
    }

    @Override
    public List<CheckOrderData> getCheckOrderDataList(List<CheckOrder> headList) {
        return hangGuoDataMapper.getCheckOrderDataList(headList);
    }

    @Override
    public List<CheckOrder> getReportCheckOrderList(CheckOrder checkOrder) {
        return hangGuoDataMapper.getReportCheckOrderList(checkOrder);
    }

    @Override
    public List<CheckOrderDispose> getReportCheckOrderDisposeList(CheckOrderDispose dispose) {
        return hangGuoDataMapper.getReportCheckOrderDisposeList(dispose);
    }

    @Override
    public ThirdPartySourceData getThirdPartySourceData(ThirdPartySourceData que) {
        return hangGuoDataMapper.getThirdPartySourceData(que);
    }

    @Override
    public void insertThirdPartySourceData(ThirdPartySourceData addSource) {
        hangGuoDataMapper.insertThirdPartySourceData(addSource);
    }

    @Override
    public List<ReportScanCodeOrderDto> getHangGuoScanOrderReport(PushDataQueryDto queryDto) {
        return hangGuoDataMapper.getHangGuoScanOrderReport(queryDto);
    }

    @Override
    public void updateCheckOrderReportFlag(List<ReportInspectionDto> inspectionDtoList) {
        hangGuoDataMapper.updateCheckOrderReportFlag(inspectionDtoList);
    }

    @Override
    public void updateCheckOrderDisposeReportFlag(List<ReportUnqualifiedDisposalDto> disposalDtos) {
        hangGuoDataMapper.updateCheckOrderDisposeReportFlag(disposalDtos);
    }

    @Override
    public List<Category> getCategoryFaultList(Category category) {
        return hangGuoDataMapper.getCategoryFaultList(category);
    }

    @Override
    public Category getCategoryByThirdCode(String parentCode) {
        return hangGuoDataMapper.getCategoryByThirdCode(parentCode);
    }

    @Override
    public void batchUpdateCategoryByThirdCode(List<Category> categoryList) {
        hangGuoDataMapper.batchUpdateCategoryByThirdCode(categoryList);
    }

}
