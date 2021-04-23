package com.dili.trace.dao;

import com.dili.ss.base.MyMapper;
import com.dili.trace.api.input.TradeRequestInputDto;
import com.dili.trace.api.input.UserQueryDto;
import com.dili.trace.domain.TradeRequest;
import com.dili.trace.dto.*;
import com.dili.trace.dto.thirdparty.report.ReportDeletedOrderDto;
import com.dili.trace.dto.thirdparty.report.ReportDeliveryOrderDto;
import com.dili.trace.dto.thirdparty.report.ReportOrderDetailDto;
import com.dili.trace.dto.thirdparty.report.ReportScanCodeOrderDto;

import java.util.List;

public interface TradeRequestMapper extends MyMapper<TradeRequest> {

   public List<TradeRequest> queryListByOrderStatus(TradeRequestInputDto dto);

   /**
    * 查询近7天有买商品的用户
    * @param user
    * @return
    */

   public List<ReportScanCodeOrderDto> selectScanOrderReport(PushDataQueryDto dto);

   public List<ReportDeliveryOrderDto> selectDeliveryOrderReport(PushDataQueryDto dto);

   public List<ReportOrderDetailDto> selectOrderDetailReport(List<String> ids);

    public ReportDeletedOrderDto selectDeletedScanOrderReport(PushDataQueryDto dto);

    public ReportDeletedOrderDto selectDeletedDeliveryOrderReport(PushDataQueryDto dto);

    /**
     * 采购商品交易统计
     * @param queryDto
     * @return
     */
    List<PurchaseGoodsReportDto> queryPurchaseGoodsReportList(PurchaseGoodsReportQueryDto queryDto);

    /**
     * 采购商户交易统计
     * @param queryDto
     * @return
     */
    List<PurchaseGoodsReportDto> queryUserPurchaseReportList(UserPurchaseReportQueryDto queryDto);
}