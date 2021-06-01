package com.dili.trace.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.customer.sdk.enums.CustomerEnum;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.domain.BasePage;
import com.dili.trace.dao.UserQrHistoryMapper;
import com.dili.trace.domain.*;
import com.dili.trace.dto.query.UserQrHistoryQueryDto;
import com.dili.trace.enums.BillVerifyStatusEnum;
import com.dili.trace.enums.QrHistoryEventTypeEnum;
import com.dili.trace.glossary.UserQrStatusEnum;

import java.util.List;

import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import one.util.streamex.StreamEx;

@Service
public class UserQrHistoryService extends TraceBaseService<UserQrHistory, Long> implements CommandLineRunner {

    @Autowired
    UserInfoService userInfoService;
    @Autowired
    RegisterBillService registerBillService;
    @Autowired
    TradeRequestService tradeRequestService;
    @Autowired
    BillService billService;
    @Autowired
    UserQrHistoryMapper qrHistoryMapper;

    public void run(String... args) {

    }

    /**
     * 分页查询数据
     *
     * @param domain
     * @return
     */
    public BasePage<UserQrHistory> listPageByUserQrHistoryQuery(UserQrHistoryQueryDto domain) {
        return super.buildQuery(domain).listPageByFun(q -> {
            return this.qrHistoryMapper.listPageByUserQrHistoryQuery(q);
        });
    }

    /**
     * 更新过期用户的状态
     *
     * @param historyQueryDto
     */
    public void updateUserQrForExpire(UserQrHistoryQueryDto historyQueryDto) {
        UserQrStatusEnum qrStatusEnum = UserQrStatusEnum.BLACK;
        //查询出符合条件的userid
        List<Long> userInfoIdList = this.qrHistoryMapper.selectUserInfoIdWithoutHistory(historyQueryDto.getCreatedStart(), historyQueryDto.getCreatedEnd(), YesOrNoEnum.YES.getCode());

        StreamEx.of(userInfoIdList).forEach(userInfoId -> {
            //锁定对应的userinfo对象
            this.userInfoService.selectByUserIdForUpdate(userInfoId).ifPresent(userInfoItem -> {
                //插入qrhistory对象
                String content = "最近七天无报备且无交易单" + ",变为" + qrStatusEnum.getDesc() + "码";
                this.buildUserQrHistory(userInfoId, qrStatusEnum, QrHistoryEventTypeEnum.NO_DATA, null).ifPresent(userQrHistory -> {
                    userQrHistory.setContent(content);
                    this.insertSelective(userQrHistory);

                    //更新userinfo的qr信息
                    UserInfo userInfo = new UserInfo();
                    userInfo.setId(userInfoItem.getId());
                    userInfo.setQrHistoryId(userQrHistory.getId());
                    userInfo.setPreQrStatus(userInfoItem.getQrStatus());
                    userInfo.setQrStatus(qrStatusEnum.getCode());
                    userInfo.setQrContent(content);
                    this.userInfoService.updateSelective(userInfo);
                });
            });

        });

    }

    /**
     * createUserQrHistoryForUserRegist
     *
     * @param userInfoItem
     * @param marketId
     */
    public void createUserQrHistoryForUserRegist(UserInfo userInfoItem, Long marketId) {

        UserQrStatusEnum qrStatusEnum = UserQrStatusEnum.BLACK;

        StringBuilder content = new StringBuilder();
//        String content = "完成注册,默认为" + qrStatusEnum.getDesc() + "码";
        if (CustomerEnum.ApprovalStatus.PASSED.equalsToCode(userInfoItem.getApprovalStatus())) {
            qrStatusEnum = UserQrStatusEnum.GREEN;
            content.append("用户审核通过,变为黑码");
        } else if (CustomerEnum.ApprovalStatus.UN_PASS.equalsToCode(userInfoItem.getApprovalStatus())) {
            qrStatusEnum = UserQrStatusEnum.RED;
            content.append("用户审核未通过,变为红码");
        } else if (CustomerEnum.ApprovalStatus.WAIT_CONFIRM.equalsToCode(userInfoItem.getApprovalStatus())) {
            qrStatusEnum = UserQrStatusEnum.BLACK;
            content.append("用户待审核,变为黑码");
        }
        //插入qrhistory对象
        this.buildUserQrHistory(userInfoItem.getId(), qrStatusEnum, QrHistoryEventTypeEnum.NEW_USER, null).ifPresent(userQrHistory -> {

            userQrHistory.setContent(content.toString());
            this.insertSelective(userQrHistory);

            //更新userinfo的qr信息
            UserInfo userInfo = new UserInfo();
            userInfo.setId(userInfoItem.getId());
            userInfo.setPreQrStatus(userInfoItem.getQrStatus());
            userInfo.setQrHistoryId(userQrHistory.getId());
            userInfo.setQrStatus(userQrHistory.getQrStatus());
            userInfo.setQrContent(userQrHistory.getContent());
            this.userInfoService.updateSelective(userInfo);
        });
    }

    /**
     * createUserQrHistoryForVerifyBill
     *
     * @param billId
     */
    public Optional<UserQrHistory> createUserQrHistoryForVerifyBill(Long billId) {
        RegisterBill billItem = this.billService.getAvaiableBill(billId).orElseThrow(() -> {
            return new TraceBizException("登记单不存在");
        });
        if (billItem == null) {
            return Optional.empty();
        }
        return this.createUserQrHistoryForVerifyBill(billItem, billItem.getUserId());
    }

    /**
     * createUserQrHistoryForVerifyBill
     *
     * @param billItem
     * @param userId
     */
    public Optional<UserQrHistory> createUserQrHistoryForVerifyBill(RegisterBill billItem, Long userId) {
        if (billItem == null) {
            return Optional.empty();
        }
        BillVerifyStatusEnum billVerifyStatusEnum = BillVerifyStatusEnum.fromCode(billItem.getVerifyStatus())
                .orElse(null);
        if (billVerifyStatusEnum == null) {
            return Optional.empty();
        }
        UserQrStatusEnum userQrStatus = getUserQrStatusEnum(billVerifyStatusEnum);
        String content = "最新操作报备单审核状态是" + billVerifyStatusEnum.getName() + ",变为" + userQrStatus.getDesc() + "码";

        return this.userInfoService.saveUserInfo(userId, billItem.getMarketId()).map(userInfoItem -> {

//            if (userInfoItem.getQrHistoryId() != null) {
//                return;
//            }
            //插入qrhistory对象
            return this.buildUserQrHistory(userInfoItem.getId(), userQrStatus, QrHistoryEventTypeEnum.REGISTER_BILL, billItem.getId())
                    .map(userQrHistory -> {

                        userQrHistory.setContent(content);
                        this.insertSelective(userQrHistory);

                        //更新userinfo的qr信息
                        UserInfo userInfo = new UserInfo();
                        userInfo.setId(userInfoItem.getId());
                        userInfo.setPreQrStatus(userInfoItem.getQrStatus());
                        userInfo.setQrHistoryId(userQrHistory.getId());
                        userInfo.setQrStatus(userQrHistory.getQrStatus());
                        userInfo.setQrContent(userQrHistory.getContent());
                        this.userInfoService.updateSelective(userInfo);
                        return userQrHistory;
                    }).orElse(null);

        });
    }

    private UserQrStatusEnum getUserQrStatusEnum(BillVerifyStatusEnum billVerifyStatusEnum) {
        switch (billVerifyStatusEnum) {
            case PASSED:
                return UserQrStatusEnum.GREEN;
            case NO_PASSED:
                return UserQrStatusEnum.RED;
            case RETURNED:
                return UserQrStatusEnum.YELLOW;
            case WAIT_AUDIT:
                return UserQrStatusEnum.YELLOW;
            default:
                return UserQrStatusEnum.BLACK;
        }

    }

    /**
     * createUserQrHistoryForTradeRequest
     *
     * @param tradeRequestId
     * @param userId
     */
    public void createUserQrHistoryForTradeRequest(Long tradeRequestId, Long userId) {

        UserQrStatusEnum qrStatusEnum = UserQrStatusEnum.GREEN;
        String content = "订单交易完成, 变为" + qrStatusEnum.getDesc() + "码";

        this.userInfoService.saveUserInfo(userId, null).ifPresent(userInfoItem -> {

            if (userInfoItem.getQrHistoryId() != null) {
                return;
            }

            //插入qrhistory对象
            this.buildUserQrHistory(userId, qrStatusEnum, QrHistoryEventTypeEnum.TRADE_REQUEST, tradeRequestId).ifPresent(userQrHistory -> {
                userQrHistory.setContent(content);
                this.insertSelective(userQrHistory);

                //更新userinfo的qr信息
                UserInfo userInfo = new UserInfo();
                userInfo.setId(userInfoItem.getId());
                userInfo.setPreQrStatus(userInfoItem.getQrStatus());
                userInfo.setQrHistoryId(userQrHistory.getId());
                userInfo.setQrStatus(userQrHistory.getQrStatus());
                userInfo.setQrContent(userQrHistory.getContent());
                this.userInfoService.updateSelective(userInfo);
            });

        });


    }

    /**
     * 计算开始时间
     *
     * @param now
     * @return
     */
    protected Date start(LocalDateTime now) {
        Date start = Date.from(
                now.minusDays(6).atZone(ZoneId.systemDefault()).toInstant());
        return start;
    }

    /**
     * 计算结束时间
     *
     * @param now
     * @return
     */
    protected Date end(LocalDateTime now) {
        Date end = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        return end;
    }


    private Optional<UserQrHistory> buildUserQrHistory(Long userInfoId,
                                                       UserQrStatusEnum qrStatusEnum,
                                                       QrHistoryEventTypeEnum historyEventTypeEnum,
                                                       Long qrHistoryEventId) {
        return StreamEx.of(this.userInfoService.get(userInfoId)).nonNull().map(userItem -> {
            return this.buildUserQrHistory(userItem, qrStatusEnum, historyEventTypeEnum, qrHistoryEventId);
        }).findFirst();

    }

    private UserQrHistory buildUserQrHistory(UserInfo userItem,
                                             UserQrStatusEnum qrStatusEnum,
                                             QrHistoryEventTypeEnum historyEventTypeEnum,
                                             Long qrHistoryEventId) {

        UserQrHistory userQrHistory = new UserQrHistory();
        userQrHistory.setUserInfoId(userItem.getId());
        userQrHistory.setQrStatus(qrStatusEnum.getCode());
        userQrHistory.setCreated(new Date());
        userQrHistory.setModified(new Date());
        userQrHistory.setIsValid(YesOrNoEnum.YES.getCode());
        userQrHistory.setQrHistoryEventId(qrHistoryEventId);
        userQrHistory.setQrHistoryEventType(historyEventTypeEnum.getCode());
        return userQrHistory;

    }


    /**
     * 回退交易二维码记录
     *
     * @param tradeRequest
     */
    public void rollBackByTradeRequest(TradeRequest tradeRequest) {

        if (tradeRequest == null || tradeRequest.getId() == null) {
            return;
        }
        TradeRequest item = this.tradeRequestService.get(tradeRequest.getId());
        Long userId = item.getBuyerId();
        Long marketId = item.getBuyerMarketId();
        this.userInfoService.findByUserId(userId, marketId).ifPresent(userInfo -> {
            this.rollback(userInfo.getQrHistoryId(), userInfo.getId());
        });


    }

    /**
     * 回退报备单二维码颜色记录
     *
     * @param registerBill
     */
    public void rollbackByBill(RegisterBill registerBill) {

        if (registerBill == null || registerBill.getId() == null) {
            return;
        }
        RegisterBill item = this.billService.get(registerBill.getId());
        if (item == null) {
            return;
        }
        Long userId = item.getUserId();
        Long marketId = item.getMarketId();
        this.userInfoService.findByUserId(userId, marketId).ifPresent(userInfo -> {
            this.rollback(userInfo.getQrHistoryId(), userInfo.getId());
        });

    }

    /**
     * 回退记录
     *
     * @param userqrhistoryId
     * @param userInfoId
     */
    private void rollback(Long userqrhistoryId, Long userInfoId) {
        if (userqrhistoryId == null || userInfoId == null) {
            return;
        }

        UserQrHistory domain = new UserQrHistory();
        domain.setId(userqrhistoryId);
        domain.setIsValid(YesOrNoEnum.NO.getCode());
        this.updateSelective(domain);

        UserQrHistory query = new UserQrHistory();
        query.setUserInfoId(userInfoId);
        query.setPage(1);
        query.setRows(1);
        query.setSort("id");
        query.setOrder("desc");
        query.setIsValid(YesOrNoEnum.YES.getCode());

        UserQrHistory qrHistoryItem = StreamEx.of(this.listPageByExample(query).getDatas()).findFirst().orElse(null);

        if (qrHistoryItem == null) {
            return;
        }

        UserInfo userInfo = this.userInfoService.get(qrHistoryItem.getUserInfoId());
        if (userInfo != null) {
            UserInfo u = new UserInfo();
            u.setId(userInfo.getId());
            u.setQrHistoryId(qrHistoryItem.getId());
            u.setPreQrStatus(userInfo.getQrStatus());
            u.setQrStatus(qrHistoryItem.getQrStatus());
            u.setQrContent(qrHistoryItem.getContent());
            this.userInfoService.updateSelective(u);

        }

    }

}
