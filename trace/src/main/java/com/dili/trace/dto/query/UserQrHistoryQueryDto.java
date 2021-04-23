package com.dili.trace.dto.query;

import com.dili.ss.domain.annotation.Operator;
import com.dili.trace.domain.UserQrHistory;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.List;

public class UserQrHistoryQueryDto extends UserQrHistory {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`created`")
    @Operator(Operator.GREAT_EQUAL_THAN)
    private LocalDateTime createdStart;

    /**
     * 登记时间结束
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`created`")
    @Operator(Operator.LITTLE_EQUAL_THAN)
    private LocalDateTime createdEnd;

    @Column(name = "`user_info_id`")
    @Operator(Operator.IN)
    private List<Long> userInfoIdList;

    @Transient
    private Long userId;
    @Transient
    private Long marketId;

    public List<Long> getUserInfoIdList() {
        return userInfoIdList;
    }

    public void setUserInfoIdList(List<Long> userInfoIdList) {
        this.userInfoIdList = userInfoIdList;
    }

    public Long getMarketId() {
        return marketId;
    }

    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedStart() {
        return createdStart;
    }

    public void setCreatedStart(LocalDateTime createdStart) {
        this.createdStart = createdStart;
    }

    public LocalDateTime getCreatedEnd() {
        return createdEnd;
    }

    public void setCreatedEnd(LocalDateTime createdEnd) {
        this.createdEnd = createdEnd;
    }
}
