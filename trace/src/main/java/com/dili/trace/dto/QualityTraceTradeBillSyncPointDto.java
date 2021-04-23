package com.dili.trace.dto;

import com.dili.ss.domain.annotation.Operator;
import com.dili.trace.domain.QualityTraceTradeBillSyncPoint;

import javax.persistence.Column;

public class QualityTraceTradeBillSyncPointDto extends QualityTraceTradeBillSyncPoint {
    @Column(name = "`bill_id`")
    @Operator(Operator.LITTLE_THAN)
    private Long minBillId;

    public Long getMinBillId() {
        return minBillId;
    }

    public void setMinBillId(Long minBillId) {
        this.minBillId = minBillId;
    }
}
