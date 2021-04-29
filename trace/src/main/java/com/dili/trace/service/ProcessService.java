package com.dili.trace.service;

import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.trace.domain.CheckinOutRecord;
import com.dili.trace.domain.ProcessConfig;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.enums.*;
import com.dili.trace.rpc.service.ProductRpcService;
import com.dili.uap.sdk.domain.Firm;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 流程处理
 */
@Service
public class ProcessService {
    private static final Logger logger = LoggerFactory.getLogger(ProcessService.class);

    @Autowired
    ProductRpcService productRpcService;
    @Autowired
    BillService billService;
    @Autowired
    RegisterBillService registerBillService;

    @Autowired
    private MarketService marketService;
    @Autowired
    CheckinOutRecordService checkinOutRecordService;

    @Autowired
    ProcessConfigService processConfigService;
    @Autowired
    BillVerifyHistoryService billVerifyHistoryService;
    @Autowired
    UserQrHistoryService userQrHistoryService;
    @Autowired
    TradeService tradeService;


    /**
     * 创建报备之后
     *
     * @param billId
     * @param marketId
     */
    public void afterCreateBill(Long billId, Long marketId, ProcessConfig processConfig, Optional<OperatorUser> operatorUser) {
        RegisterBill billItem = this.registerBillService.getAndCheckById(billId).orElseThrow(() -> new TraceBizException("数据不存在"));


        RegisterBill updatableBill = new RegisterBill();
        updatableBill.setId(billId);
        // 补单直接进门状态
        if (RegistTypeEnum.SUPPLEMENT.equalsToCode(billItem.getRegistType())) {
            updatableBill.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
        } else {
            updatableBill.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
        }
        this.billService.updateSelective(updatableBill);

        if (YesOrNoEnum.NO.getCode().equals(processConfig.getIsNeedVerify())) {
            this.updateVerifyStatus(billId, BillVerifyStatusEnum.PASSED, Optional.empty(), operatorUser);
        }

    }


    /**
     * 进门
     *
     * @param operatorUser
     * @param billIdList
     * @param checkinStatusEnum
     * @return
     */
    public List<CheckinOutRecord> doCheckIn(Optional<OperatorUser> operatorUser, List<Long> billIdList,
                                            CheckinStatusEnum checkinStatusEnum) {
        if (billIdList == null) {
            throw new TraceBizException("参数错误");
        }

        if (checkinStatusEnum == null) {
            throw new TraceBizException("参数错误");
        }
        return StreamEx.of(billIdList).nonNull().map(billId -> {
            return this.billService.get(billId);
        }).nonNull().map(bill -> {
            ProcessConfig processConfig = this.processConfigService.findByMarketId(bill.getMarketId());
            Long weightingBillId = bill.getWeightingBillId();
            if (YesOrNoEnum.YES.getCode().equals(processConfig.getCanDoCheckInWithoutWeight()) || weightingBillId != null) {
                return this.updateCheckinstatus(bill.getId(), checkinStatusEnum, operatorUser).orElse(null);
            } else {
                throw new TraceBizException("未称重的登记单,不能进门");
            }

        }).nonNull().toList();
    }

    /**
     * 更新状态到进门
     *
     * @param billId
     * @return
     */
    private Optional<CheckinOutRecord> updateCheckinstatus(Long billId, CheckinStatusEnum checkinStatusEnum, Optional<OperatorUser> operatorUser) {

        if (CheckinStatusEnum.NONE == checkinStatusEnum) {
            throw new TraceBizException("参数错误");
        }
        if (CheckinStatusEnum.NOTALLOWED == checkinStatusEnum) {
            return null;
        }

        RegisterBill billItem = this.registerBillService.getAndCheckById(billId).orElseThrow(() -> new TraceBizException("数据不存在"));

        RegisterBill updatableBill = new RegisterBill();
        updatableBill.setId(billId);
        if (CheckinStatusEnum.ALLOWED == checkinStatusEnum) {
            updatableBill.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
        } else {
            updatableBill.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
        }
        if (BillVerifyStatusEnum.PASSED.equalsToCode(billItem.getVerifyStatus())) {
            if (CheckinStatusEnum.ALLOWED == checkinStatusEnum) {
                updatableBill.setVerifyType(VerifyTypeEnum.PASSED_AFTER_CHECKIN.getCode());
            } else {
                updatableBill.setVerifyType(VerifyTypeEnum.PASSED_BEFORE_CHECKIN.getCode());
            }
        }
        this.billService.updateSelective(updatableBill);
        return this.createCheckInRecord(billId, checkinStatusEnum, operatorUser);
    }

    /**
     * 创建进门记录,并创建进门库存
     *
     * @param billId
     * @param checkinStatusEnum
     * @param operatorUser
     * @return
     */
    private Optional<CheckinOutRecord> createCheckInRecord(Long billId, CheckinStatusEnum checkinStatusEnum, Optional<OperatorUser> operatorUser) {
        RegisterBill billItem = this.billService.get(billId);
        if (CheckinStatusEnum.ALLOWED == checkinStatusEnum) {
            CheckinOutRecord item = new CheckinOutRecord();
//				item.setId(cin.getId());
            item.setStatus(checkinStatusEnum.getCode());
            operatorUser.ifPresent(op -> {
                // item.setOperatorId(op.getId());
                // item.setOperatorName(op.getName());
            });
            item.setModified(new Date());
            item.setProductName(billItem.getProductName());
            item.setInoutWeight(billItem.getWeight());
            item.setWeightUnit(billItem.getWeightUnit());
            item.setUserName(billItem.getName());
            item.setUserId(billItem.getUserId());
            item.setBillType(billItem.getBillType());
            // item.setVerifyStatus(billItem.getVerifyStatus());
            item.setBillId(billItem.getBillId());
            item.setInout(CheckinOutTypeEnum.IN.getCode());
//				item.setTradeDetailId(tradeDetailItem.getId());
//				this.updateSelective(item);
//				return this.get(item.getId());
            this.checkinOutRecordService.insertSelective(item);
            // 创建相关的tradeDetail及batchStock数据
            this.tradeService.createBatchStockAfterVerifiedAndCheckin(billId,
                    operatorUser);
            return Optional.of(item);
        }
        return Optional.empty();

    }

    /**
     * 更新审核状态
     *
     * @param billId
     * @param operatorUser
     * @return
     */
    private int updateVerifyStatus(Long billId, BillVerifyStatusEnum toVerifyStatusEnum, Optional<String> reason, Optional<OperatorUser> operatorUser) {


        RegisterBill updatableBill = new RegisterBill();
        updatableBill.setId(billId);
        updatableBill.setVerifyStatus(toVerifyStatusEnum.getCode());
        // 更新当前报务单数据
        operatorUser.ifPresent(op -> {
            updatableBill.setOperatorId(op.getId());
            updatableBill.setOperatorName(op.getName());
            updatableBill.setOperationTime(new Date());
        });
        reason.ifPresent(rs -> {
            updatableBill.setReason(StringUtils.trimToEmpty(rs));
        });
        RegisterBill billItem = this.billService.get(billId);
        CheckinStatusEnum checkinStatusEnum = CheckinStatusEnum.fromCode(billItem.getCheckinStatus());


        RegistTypeEnum registTypeEnum = RegistTypeEnum.fromCode(billItem.getRegistType()).orElse(null);
        if (RegistTypeEnum.SUPPLEMENT == registTypeEnum) {
            if (BillVerifyStatusEnum.PASSED == toVerifyStatusEnum) {
                updatableBill.setVerifyType(VerifyTypeEnum.PASSED_BEFORE_CHECKIN.getCode());
            }
        }

        updatableBill.setModified(new Date());
        this.billService.updateSelective(updatableBill);

        if (BillVerifyStatusEnum.PASSED == toVerifyStatusEnum) {
            this.createCheckInRecord(billId, checkinStatusEnum, operatorUser);
        }

        this.billVerifyHistoryService.createVerifyHistory(Optional.of(toVerifyStatusEnum), billId, operatorUser);


        // 更新用户颜色码
        this.userQrHistoryService.createUserQrHistoryForVerifyBill(billId);
        return 1;
    }

    /**
     * 报备审核通过之后
     *
     * @param billId
     * @param marketId
     */
    public void afterBillPassed(Long billId, Long marketId, Optional<OperatorUser> optUser) {
        logger.debug("afterBillPassed:billId={},marketId={}", billId, marketId);
        RegisterBill registerBill = billService.get(billId);

        Map<String, String> marketCodeMap = marketService.getMarketCodeMap();
        String marketCode = marketService.getMarketById(marketId).map(Firm::getCode).orElseThrow(() -> {
            return new TraceBizException("市场不存在");
        });
        logger.debug("marketCode={}", marketCode);
        // 杭果和寿光市场，审核通过后系统自动进门
//        if (marketCode.equals(marketCodeMap.get(MarketEnum.HZSG.getCode()))
//                || marketCode.equals(marketCodeMap.get(MarketEnum.SDSG.getCode()))||marketId.equals(8L)) {
//            logger.debug("杭果和寿光自动进门");
//            List<CheckinOutRecord> checkinRecordList = this.checkinOutRecordService
//                    .doCheckin(optUser, Lists.newArrayList(billId), CheckinStatusEnum.ALLOWED);
//        }else{
//            logger.debug("其他市场状态不变");
//        }
    }
}
