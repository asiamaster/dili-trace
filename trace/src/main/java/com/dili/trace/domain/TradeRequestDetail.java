package com.dili.trace.domain;

import com.dili.ss.domain.BaseDomain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "`trade_request_detail`")
public class TradeRequestDetail extends BaseDomain {
    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private Long id;
    @Column(name = "`trade_request_id`")
    private Long tradeRequestId;
    @Column(name = "`trade_detail_id`")
    private Long tradeDetailId;

    @Column(name = "`bill_Id`")
    private Long billId;

    @Column(name = "`trade_weight`")
    private BigDecimal tradeWeight;

    /**
     * 创建时间
     */
    @Column(name = "`created`")
    private Date created;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Long getTradeRequestId() {
        return tradeRequestId;
    }

    public void setTradeRequestId(Long tradeRequestId) {
        this.tradeRequestId = tradeRequestId;
    }

    public Long getTradeDetailId() {
        return tradeDetailId;
    }

    public void setTradeDetailId(Long tradeDetailId) {
        this.tradeDetailId = tradeDetailId;
    }

    public BigDecimal getTradeWeight() {
        return tradeWeight;
    }

    public void setTradeWeight(BigDecimal tradeWeight) {
        this.tradeWeight = tradeWeight;
    }
}
