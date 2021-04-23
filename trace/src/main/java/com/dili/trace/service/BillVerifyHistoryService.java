package com.dili.trace.service;

import com.dili.trace.domain.BillVerifyHistory;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.enums.BillVerifyStatusEnum;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 * 审核记录
 */
@Service
public class BillVerifyHistoryService extends TraceBaseService<BillVerifyHistory, Long> {
    /**
     * 创建审核历史记录
     *
     * @param previousVerifyStatus
     * @param billId
     * @param verifyOperatorUser
     * @return
     */
    public BillVerifyHistory createVerifyHistory(Optional<BillVerifyStatusEnum> previousVerifyStatus, Long billId, Optional<OperatorUser> verifyOperatorUser) {
        BillVerifyHistory verifyHistory = new BillVerifyHistory();
        verifyHistory.setBillId(billId);
        verifyHistory.setCreated(new Date());
        verifyHistory.setModified(new Date());
        previousVerifyStatus.ifPresent(pvs->{
            verifyHistory.setPreviousVerifyStatus(pvs.getCode());
        });

        verifyHistory.setVerifyDateTime(new Date());
        verifyOperatorUser.ifPresent(opt -> {

            verifyHistory.setVerifyOperatorName(opt.getName());
            verifyHistory.setVerifyOperatorId(opt.getId());
        });
        this.insertSelective(verifyHistory);
        return verifyHistory;
    }

    /**
     * 查询最后一次审核记录
     *
     * @param billId
     * @return
     */
    public Optional<BillVerifyHistory> findVerifyHistoryByBillId(Long billId) {
        if (billId == null) {
            return Optional.empty();
        }
        BillVerifyHistory history = new BillVerifyHistory();
        history.setBillId(billId);
        history.setSort("id");
        history.setOrder("desc");
        history.setPage(1);
        history.setRows(1);
        return this.listPageByExample(history).getDatas().stream().findFirst();

    }
}
