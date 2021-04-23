package com.dili.trace.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.trace.domain.CheckinOutRecord;
import com.dili.trace.domain.ProductStock;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.TradeDetail;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.enums.*;
import com.dili.trace.glossary.TFEnum;

import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 交易服务
 */
@Service
@Transactional
public class TradeService {
    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);
    @Autowired
    ProductStockService batchStockService;
    @Autowired
    RegisterBillService registerBillService;
    @Autowired
    TradeDetailService tradeDetailService;
    @Autowired
    TradeOrderService tradeOrderService;
    @Autowired
    CheckinOutRecordService checkinOutRecordService;

    /**
     * 创建库存
     *
     * @param billId
     * @param operatorUser
     * @return
     */
    public Long createBatchStockAfterVerifiedAndCheckin(Long billId, Optional<OperatorUser> operatorUser) {
        RegisterBill billItem = this.registerBillService.get(billId);
        BigDecimal weight = billItem.getWeight();
//      if (!YesOrNoEnum.YES.getCode().equals(billItem.getIsCheckin())) {
        if (!CheckinStatusEnum.ALLOWED.equalsToCode(billItem.getCheckinStatus())) {
            // 还没有进门，不对TradeDetaile及BatchStock进行任何操作
            return billId;
        }

        logger.info("billid:{},billI.verifyStatus:{}", billId, billItem.getVerifyStatusName());
//        logger.info("TradeDetail.checkinStatus:{},TradeDetail.saleStatus:{}", tradeDetailItem.getCheckinStatus(),
//                tradeDetailItem.getSaleStatus());
        // 通过审核状态及进门状态判断是否可以进行销售
        if (CheckinStatusEnum.ALLOWED.equalsToCode(billItem.getCheckinStatus())
//                && CheckoutStatusEnum.NONE.equalsToCode(billItem.getCheckoutStatus())
                && BillVerifyStatusEnum.PASSED.equalsToCode(billItem.getVerifyStatus()))
//                && SaleStatusEnum.NONE.equalsToCode(tradeDetailItem.getSaleStatus())) {
        {
            this.tradeOrderService.createTradeFromRegisterBill(billItem);
            TradeDetail tdq = new TradeDetail();
            tdq.setBillId(billItem.getBillId());
            tdq.setTradeType(TradeTypeEnum.NONE.getCode());
            TradeDetail tradeDetailItem = StreamEx.of(this.tradeDetailService.listByExample(tdq)).findFirst().orElseThrow(() -> {
                throw new TraceBizException("查询报备信息错误");
            });
            CheckinOutRecord torq=new CheckinOutRecord();
            torq.setBillId(billItem.getBillId());
            torq.setInout(CheckinOutTypeEnum.IN.getCode());
            torq.setStatus(CheckinStatusEnum.ALLOWED.getCode());
            torq.setSort("id");
            torq.setOrder("desc");

            CheckinOutRecord checkinOutRecord = StreamEx.of(this.checkinOutRecordService.listByExample(torq)).findFirst().orElseThrow(() -> {
                throw new TraceBizException("查询进门信息错误");
            });
            TradeDetail updatableTD=new TradeDetail();
            updatableTD.setId(tradeDetailItem.getId());
            updatableTD.setCheckinRecordId(checkinOutRecord.getId());
            updatableTD.setCheckinStatus(checkinOutRecord.getStatus());
            updatableTD.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());

            this.tradeDetailService.updateSelective(updatableTD);

            CheckinOutRecord updatableCheckRecord=new CheckinOutRecord();
            updatableCheckRecord.setId(checkinOutRecord.getId());
            updatableCheckRecord.setTradeDetailId(tradeDetailItem.getId());
            this.checkinOutRecordService.updateSelective(updatableCheckRecord);

//            ProductStock batchStock = this.batchStockService.findOrCreateBatchStock(billItem.getUserId(), billItem);
//            batchStock.setStockWeight(batchStock.getStockWeight().add(weight));
//            batchStock.setTotalWeight(batchStock.getTotalWeight().add(weight));
//            batchStock.setTradeDetailNum(batchStock.getTradeDetailNum()+1);
//            this.batchStockService.updateSelective(batchStock);
//
//            TradeDetail updatableRecord = new TradeDetail();
//            updatableRecord.setId(tradeDetailItem.getId());
//            updatableRecord.setModified(new Date());
//            updatableRecord.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
//            updatableRecord.setIsBatched(YesOrNoEnum.YES.getCode());
//            updatableRecord.setProductStockId(batchStock.getId());
//            updatableRecord.setStockWeight(weight);
//            updatableRecord.setTotalWeight(weight);
//            this.tradeDetailService.updateSelective(updatableRecord);

        }
        return billId;

    }

}