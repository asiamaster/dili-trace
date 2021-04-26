package com.dili.trace.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.dili.ss.domain.BaseDomain;

import javax.persistence.*;

@Table(name = "`process_config`")
public class ProcessConfig extends BaseDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @JSONField(serialize = false)
    private Long id;

    /**
     * 市场ID
     */
    @Column(name = "`market_id`")
    private Long marketId;

    /**
     * 是否登记审核
     */
    @Column(name = "`is_need_verify`")//is_audit_after_regist
    private Integer isNeedVerify;

    /**
     * 称重后自动进门
     */
    @Column(name = "`can_docheckin_without_weight`")//is_weight_before_checkin
    private Integer canDoCheckInWithoutWeight;

    /**
     * 是否进门审核
     */
    @Column(name = "`is_manully_checkIn`")//is_audit_before_checkin
    private Integer isManullyCheckIn;



    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getMarketId() {
        return marketId;
    }

    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }

    public Integer getIsNeedVerify() {
        return isNeedVerify;
    }

    public void setIsNeedVerify(Integer isNeedVerify) {
        this.isNeedVerify = isNeedVerify;
    }

    public Integer getCanDoCheckInWithoutWeight() {
        return canDoCheckInWithoutWeight;
    }

    public void setCanDoCheckInWithoutWeight(Integer canDoCheckInWithoutWeight) {
        this.canDoCheckInWithoutWeight = canDoCheckInWithoutWeight;
    }

    public Integer getIsManullyCheckIn() {
        return isManullyCheckIn;
    }

    public void setIsManullyCheckIn(Integer isManullyCheckIn) {
        this.isManullyCheckIn = isManullyCheckIn;
    }
}
