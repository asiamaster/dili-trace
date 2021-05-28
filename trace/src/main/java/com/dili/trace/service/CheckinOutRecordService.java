package com.dili.trace.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.trace.api.input.CheckOutApiInput;
import com.dili.trace.domain.CheckinOutRecord;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.TradeDetail;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.enums.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import one.util.streamex.StreamEx;

/**
 * @author Alvin.Li
 */
@SuppressWarnings("deprecation")
@Service
public class CheckinOutRecordService extends BaseServiceImpl<CheckinOutRecord, Long> {

    @Autowired
    UserService userService;
    @Autowired
    RegisterBillService registerBillService;
    @Autowired
    UpStreamService upStreamService;
    @Autowired
    TradeDetailService tradeDetailService;
    @Autowired
    TradeService tradeService;
    @Autowired
    ProcessService processService;
    @Autowired
    BillVerifyHistoryService billVerifyHistoryService;

    /**
     * 出门操作
     *
     * @param operateUser
     * @param checkOutApiInput
     * @return
     */
    @Transactional
    public List<CheckinOutRecord> doCheckout(OperatorUser operateUser, CheckOutApiInput checkOutApiInput) {
        if (checkOutApiInput == null || checkOutApiInput.getTradeDetailIdList() == null
                || checkOutApiInput.getCheckoutStatus() == null) {
            throw new TraceBizException("参数错误");
        }
        CheckoutStatusEnum checkoutStatusEnum = CheckoutStatusEnum.fromCode(checkOutApiInput.getCheckoutStatus());

        if (checkoutStatusEnum == null) {
            throw new TraceBizException("参数错误");
        }
        return StreamEx.of(checkOutApiInput.getTradeDetailIdList()).nonNull().map(id -> {
            TradeDetail tradeDetailItem = this.tradeDetailService.get(id);
            if (tradeDetailItem == null) {
                throw new TraceBizException("请求出门的数据不存在");
            }
            if (SaleStatusEnum.FOR_SALE.equalsToCode(tradeDetailItem.getSaleStatus())
                    && CheckoutStatusEnum.NONE.equalsToCode(tradeDetailItem.getCheckoutStatus())) {

            } else {
                throw new TraceBizException("请求出门的数据状态错误");
            }
            return tradeDetailItem;

        }).nonNull().mapToEntry(tradeDetailItem -> {
            RegisterBill bill = this.registerBillService.get(tradeDetailItem.getBillId());
            return bill;

        }).map(e -> {
            TradeDetail tradeDetailItem = e.getKey();
            RegisterBill registerBillItem = e.getValue();
            UserInfo user = this.getUser(registerBillItem.getUserId())
                    .orElseThrow(() -> new TraceBizException("用户信息不存在"));
            CheckinOutRecord checkoutRecord = new CheckinOutRecord();
            checkoutRecord.setStatus(checkoutStatusEnum.getCode());
            checkoutRecord.setInout(CheckinOutTypeEnum.OUT.getCode());
            checkoutRecord.setProductName(registerBillItem.getProductName());
            checkoutRecord.setInoutWeight(tradeDetailItem.getStockWeight());
            checkoutRecord.setWeightUnit(tradeDetailItem.getWeightUnit());
            checkoutRecord.setUserName(user.getName());
            checkoutRecord.setUserId(user.getId());

            checkoutRecord.setOperatorId(operateUser.getId());
            checkoutRecord.setOperatorName(operateUser.getName());
            checkoutRecord.setRemark(checkOutApiInput.getRemark());
            checkoutRecord.setCreated(new Date());
            checkoutRecord.setModified(new Date());
            checkoutRecord.setTradeDetailId(tradeDetailItem.getId());

            this.insertSelective(checkoutRecord);

            TradeDetail updatable = new TradeDetail();
            updatable.setId(tradeDetailItem.getId());
            updatable.setCheckoutRecordId(checkoutRecord.getId());

            updatable.setCheckoutStatus(checkoutStatusEnum.getCode());
            this.tradeDetailService.updateSelective(updatable);
            return checkoutRecord;
        }).toList();

    }

    /**
     * 获取操作用户
     *
     * @param userId
     * @return
     */
    private Optional<UserInfo> getUser(Long userId) {
        UserInfo user = this.userService.get(userId);
        return Optional.ofNullable(user);
    }


    /**
     * 单个进门
     *
     * @param billId
     * @param checkinStatusEnum
     * @param operateUser
     * @return
     */
    public CheckinOutRecord doOneCheckin(Long billId, CheckinStatusEnum checkinStatusEnum,
                                         Optional<OperatorUser> operateUser) {
        RegisterBill billItem = registerBillService.getAndCheckById(billId).orElseThrow(() -> new TraceBizException("数据不存在"));

        if (CheckinStatusEnum.NONE == checkinStatusEnum) {
            throw new TraceBizException("参数错误");
        }
        if (CheckinStatusEnum.NOTALLOWED == checkinStatusEnum) {
            return null;
        }

        // 补报单已经是进门状态(但是没有进门数据)，所以此处放弃用isCheckin来判断，而是用tradedetail来做判断
        // if (YnEnum.YES.equalsToCode(billItem.getIsCheckin())) {
        // throw new TraceBusinessException("当前报备单已进门");
        // }
        // CheckinOutRecord checkinOutRecordItem = this.tradeDetailService.findBilledTradeDetailByBillId(billId)
        // 		.map(td -> {
        // 			if (CheckinStatusEnum.ALLOWED.equalsToCode(td.getCheckinStatus())) {
        // 				return this.get(td.getCheckinRecordId());
        // 			}
        // 			return null;
        // 		}).filter(Objects::nonNull).orElse(null);

        // if (checkinOutRecordItem != null) {
        // 	return checkinOutRecordItem;
        // }
        if (CheckinStatusEnum.ALLOWED == checkinStatusEnum) {
//			TradeDetail tradeDetailItem = this.tradeDetailService.findBilledTradeDetailByBillId(billId).map(td->{
//				TradeDetail item = new TradeDetail();
//				item.setId(td.getId());
//				item.setCheckinStatus(checkinStatusEnum.getCode());
//				item.setStockWeight(billItem.getWeight());
//				item.setTotalWeight(billItem.getWeight());
//				item.setWeightUnit(billItem.getWeightUnit());
//				item.setProductName(billItem.getProductName());
//				item.setModified(new Date());
//				this.tradeDetailService.updateSelective(item);
//				return this.tradeDetailService.get(item.getId());
//			}).orElseGet(()->{
//			 	return this.tradeDetailService.createTradeDetailForCheckInBill(billItem);
//			});

//			CheckinOutRecord checkinRecordItem =Optional.ofNullable(tradeDetailItem.getCheckinRecordId()).map(cinId->{
//				return this.get(cinId);
//			}).map(cin->{
            CheckinOutRecord item = new CheckinOutRecord();
//				item.setId(cin.getId());
            item.setStatus(checkinStatusEnum.getCode());
            operateUser.ifPresent(op -> {
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
            this.insertSelective(item);
//			}).orElseGet(()->{
//				 return this.createRecordForCheckin(billItem,tradeDetailItem.getTradeDetailId(), checkinStatusEnum, operateUser);
//			});


//			TradeDetail tradeDetail = new TradeDetail();
//			tradeDetail.setId(tradeDetailItem.getId());
//			tradeDetail.setCheckinStatus(checkinStatusEnum.getCode());
//			tradeDetail.setCheckinRecordId(checkinRecordItem.getId());
//			this.tradeDetailService.updateSelective(tradeDetail);
            // 更新报备单进门状态
            RegisterBill bill = new RegisterBill();
            bill.setId(billItem.getId());
            if (CheckinStatusEnum.ALLOWED == checkinStatusEnum) {
//				bill.setIsCheckin(YesOrNoEnum.YES.getCode());
                bill.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
            } else {
//				bill.setIsCheckin(YesOrNoEnum.NO.getCode());
                bill.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
            }
            this.registerBillService.updateSelective(bill);

            this.tradeService.createBatchStockAfterVerifiedAndCheckin(billItem.getId(),
                    operateUser);

            // 本地库存处理完成后。同步库存到UAP
//			processService.afterCheckIn(billItem.getId(), billItem.getMarketId(),operateUser);

            return item;
        }
        return null;

    }

    /**
     * 新的审核方法
     *
     * @param billId
     * @param toVerifyStatusEnum
     * @param operatorUser
     */
    public void doVerify(Long billId, BillVerifyStatusEnum toVerifyStatusEnum, Optional<OperatorUser> operatorUser) {

        RegisterBill billItem = this.registerBillService.get(billId);
        if (billItem == null) {
            throw new TraceBizException("报备单不存在");
        }
        BillVerifyStatusEnum oldVerifyStatusEnum = BillVerifyStatusEnum.fromCode(billItem.getVerifyStatus()).orElseThrow(() -> {
            return new TraceBizException("审核状态不能为空");
        });

        RegisterBill up = new RegisterBill();
        up.setId(billItem.getId());

        RegistTypeEnum registTypeEnum = RegistTypeEnum.fromCode(billItem.getRegistType()).orElseThrow(() -> {
            return new TraceBizException("报备类型不能为空");
        });

        up.setVerifyStatus(toVerifyStatusEnum.getCode());
        if (BillVerifyStatusEnum.PASSED == toVerifyStatusEnum) {
            if (RegistTypeEnum.SUPPLEMENT == registTypeEnum) {
                up.setVerifyType(VerifyTypeEnum.CHECKIN_WITHOUT_VERIFY.getCode());
            } else {
                up.setVerifyType(VerifyTypeEnum.VERIFY_BEFORE_CHECKIN.getCode());
            }
        }
        this.registerBillService.updateSelective(up);

        this.billVerifyHistoryService.createVerifyHistory(Optional.of(oldVerifyStatusEnum), billId, operatorUser);

        Optional<CheckinOutRecord> checkinRecordOpt = this.findOrCreateAllowedCheckInRecord(billId);

    }


    /**
     * 查询或创建进门记录
     *
     * @param billId
     * @return
     */
    public Optional<CheckinOutRecord> findOrCreateAllowedCheckInRecord(Long billId) {
        RegisterBill billItem = this.registerBillService.get(billId);
        if (billItem == null) {
            throw new TraceBizException("报备单不存在");
        }
        CheckinStatusEnum checkinStatusEnum = CheckinStatusEnum.fromCode(billItem.getCheckinStatus());
        if (CheckinStatusEnum.ALLOWED != checkinStatusEnum) {
            return Optional.empty();
        }
        CheckinOutRecord q = new CheckinOutRecord();
        q.setBillId(billId);
        q.setStatus(CheckinStatusEnum.ALLOWED.getCode());
        q.setInout(CheckinOutTypeEnum.IN.getCode());
        return Optional.of(StreamEx.of(this.listByExample(q)).findFirst().orElseGet(() -> {
            return this.createAllowedCheckInRecord(billItem);
        }));

    }

    /**
     * 查找已经进门的信息
     * @param billId
     * @return
     */
    public Optional<CheckinOutRecord> findAllowedCheckInRecord(Long billId) {

        CheckinOutRecord q = new CheckinOutRecord();
        q.setBillId(billId);
        q.setStatus(CheckinStatusEnum.ALLOWED.getCode());
        q.setInout(CheckinOutTypeEnum.IN.getCode());
        return StreamEx.of(this.listByExample(q)).findFirst();

    }

    /**
     * 创建允许进门数据
     *
     * @param billItem
     * @return
     */
    private CheckinOutRecord createAllowedCheckInRecord(RegisterBill billItem) {
        CheckinOutRecord item = new CheckinOutRecord();

        item.setStatus(CheckinStatusEnum.ALLOWED.getCode());

        item.setModified(new Date());
        item.setProductName(billItem.getProductName());
        item.setInoutWeight(billItem.getWeight());
        item.setWeightUnit(billItem.getWeightUnit());
        item.setUserName(billItem.getName());
        item.setUserId(billItem.getUserId());
        item.setBillType(billItem.getBillType());

        item.setBillId(billItem.getBillId());
        item.setInout(CheckinOutTypeEnum.IN.getCode());

        this.insertSelective(item);
        return item;
    }
}
