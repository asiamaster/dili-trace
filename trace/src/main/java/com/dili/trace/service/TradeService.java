package com.dili.trace.service;

import com.dili.common.exception.TraceBizException;
import com.dili.trace.domain.*;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.enums.*;
import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
    @Autowired
    DetectRequestService detectRequestService;
    @Autowired
    ProductStockService productStockService;


    /**
     * 判断是否可以创建库存信息
     *
     * @param billItem
     * @return
     */
    private boolean productStockIsCreatable(RegisterBill billItem) {
        CheckinStatusEnum checkinStatusEnum = CheckinStatusEnum.fromCode(billItem.getCheckinStatus());
        if (checkinStatusEnum == null) {
            throw new TraceBizException("进门状态错误");
        }

        if (CheckinStatusEnum.ALLOWED != checkinStatusEnum) {
            // 还没有进门，不对TradeDetaile及BatchStock进行任何操作
            return false;
        }

        BillVerifyStatusEnum verifyStatusEnum = BillVerifyStatusEnum.fromCodeOrEx(billItem.getVerifyStatus());
        return (BillVerifyStatusEnum.RETURNED == verifyStatusEnum || BillVerifyStatusEnum.PASSED == verifyStatusEnum);
        //(退回+检测合格)/(审核通过)+进门
//        if (BillVerifyStatusEnum.RETURNED == verifyStatusEnum) {
//            boolean detectPassed = StreamEx.ofNullable(billItem.getDetectRequestId()).map(this.detectRequestService::get)
//                    .nonNull().map(DetectRequest::getDetectResult)
//                    .nonNull().allMatch(DetectResultEnum.PASSED::equalsToCode);
//            if (!detectPassed) {
//                return false;
//            }
//        } else if (BillVerifyStatusEnum.PASSED != verifyStatusEnum) {
//            return false;
//        }
//        return true;

    }

    /**
     * 创建库存
     *
     * @param billId
     * @param operatorUser
     * @return
     */
    public Long createBatchStockAfterVerifiedAndCheckin(Long billId, Optional<OperatorUser> operatorUser) {
        RegisterBill billItem = this.registerBillService.getAndCheckById(billId).orElseThrow(() -> {
            return new TraceBizException("报备单数据不存在或已删除");
        });
        boolean creatable = this.productStockIsCreatable(billItem);
        if (!creatable) {
            return billId;
        }

        RegistTypeEnum registTypeEnum = RegistTypeEnum.fromCodeOrEx(billItem.getRegistType());
        BillVerifyStatusEnum verifyStatusEnum = BillVerifyStatusEnum.fromCodeOrEx(billItem.getVerifyStatus());


        DetectResultEnum detectResultEnum = StreamEx.ofNullable(billItem.getDetectRequestId()).map(this.detectRequestService::get)
                .nonNull().map(DetectRequest::getDetectResult)
                .nonNull().map(DetectResultEnum::fromCode).filter(Optional::isPresent).findFirst().map(Optional::get).orElse(DetectResultEnum.NONE);
        logger.info("billid:{},billI.verifyStatus:{},weight:{}", billId, billItem.getVerifyStatusName(), billItem.getWeight());

        // 通过审核状态及进门状态判断是否可以进行销售
        Optional<TradeDetail> tradeOrderOptional = this.tradeOrderService.createTradeFromRegisterBill(billItem);
        if (!tradeOrderOptional.isPresent()) {
            //相应的tradeDetail已经存在
            return billId;
        }
        TradeDetail tdq = new TradeDetail();
        tdq.setBillId(billItem.getBillId());
        tdq.setTradeType(TradeTypeEnum.NONE.getCode());
        TradeDetail tradeDetailItem = StreamEx.of(this.tradeDetailService.listByExample(tdq)).findFirst().orElseThrow(() -> {
            throw new TraceBizException("查询报备信息错误");
        });
        CheckinOutRecord torq = new CheckinOutRecord();
        torq.setBillId(billItem.getBillId());
        torq.setInout(CheckinOutTypeEnum.IN.getCode());
        torq.setStatus(CheckinStatusEnum.ALLOWED.getCode());
        torq.setSort("id");
        torq.setOrder("desc");

        CheckinOutRecord checkinOutRecord = StreamEx.of(this.checkinOutRecordService.listByExample(torq)).findFirst().orElseThrow(() -> {
            throw new TraceBizException("查询进门信息错误");
        });
        TradeDetail updatableTD = new TradeDetail();
        updatableTD.setId(tradeDetailItem.getId());
        updatableTD.setCheckinRecordId(checkinOutRecord.getId());
        updatableTD.setCheckinStatus(checkinOutRecord.getStatus());

        //审核通过+进门  或者 退回+进门(检测合格:可售,检测不合格:不可售)
        if (BillVerifyStatusEnum.PASSED == verifyStatusEnum) {
            updatableTD.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
        } else if (BillVerifyStatusEnum.RETURNED == verifyStatusEnum) {
            if (DetectResultEnum.PASSED == detectResultEnum) {
                updatableTD.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
            } else {
                updatableTD.setSaleStatus(SaleStatusEnum.NOT_FOR_SALE.getCode());
            }
        }

        this.tradeDetailService.updateSelective(updatableTD);

        CheckinOutRecord updatableCheckRecord = new CheckinOutRecord();
        updatableCheckRecord.setId(checkinOutRecord.getId());
        updatableCheckRecord.setTradeDetailId(tradeDetailItem.getId());
        this.checkinOutRecordService.updateSelective(updatableCheckRecord);

        this.productStockService.updateProductStock(tradeDetailItem.getProductStockId());

        return billId;

    }

}