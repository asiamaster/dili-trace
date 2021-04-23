package com.dili.trace.dto;

import com.dili.ss.domain.annotation.Operator;
import com.dili.trace.domain.QualityTraceTradeBill;

import javax.persistence.Column;
import java.util.List;

public class QualityTraceTradeBillDto extends QualityTraceTradeBill {
    @Column(name = "`match_status`")
    @Operator(Operator.IN)
    private List<Integer> matchStatusList;

    public List<Integer> getMatchStatusList() {
        return matchStatusList;
    }

    public void setMatchStatusList(List<Integer> matchStatusList) {
        this.matchStatusList = matchStatusList;
    }
}
