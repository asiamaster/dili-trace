package com.dili.trace.api.input;

import com.dili.ss.domain.annotation.Operator;
import com.dili.trace.domain.TradeDetail;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.List;

public class TradeDetailQueryDto extends TradeDetail {
    /**
     * 查询登记开始时间
     */
    @ApiModelProperty(value = "查询登记开始时间")
    @Column(name = "`created`")
    @Operator(Operator.GREAT_EQUAL_THAN)
    private String createdStart;

    /**
     * 查询登记结束时间
     */
    @ApiModelProperty(value = "查询登记结束时间")
    @Column(name = "`created`")
    @Operator(Operator.LITTLE_EQUAL_THAN)
    private String createdEnd;


    @Column(name = "`stock_weight`")
    @Operator(Operator.GREAT_THAN)
    private BigDecimal minStockWeight;


    @Column(name = "`trade_request_id`")
    @Operator(Operator.IN)
    private List<Long> tradeRequestIdList;




    public List<Long> getTradeRequestIdList() {
        return this.tradeRequestIdList;
    }

    public void setTradeRequestIdList(List<Long> tradeRequestIdList) {
        this.tradeRequestIdList = tradeRequestIdList;
    }

    /**
     * @return String return the createdStart
     */
    @Override
    public String getCreatedStart() {
        return this.createdStart;
    }

    /**
     * @param createdStart the createdStart to set
     */
    @Override
    public void setCreatedStart(String createdStart) {
        this.createdStart = createdStart;
    }

    /**
     * @return String return the createdEnd
     */
    @Override
    public String getCreatedEnd() {
        return this.createdEnd;
    }

    /**
     * @param createdEnd the createdEnd to set
     */
    @Override
    public void setCreatedEnd(String createdEnd) {
        this.createdEnd = createdEnd;
    }

    public BigDecimal getMinStockWeight() {
        return this.minStockWeight;
    }

    public void setMinStockWeight(BigDecimal minStockWeight) {
        this.minStockWeight = minStockWeight;
    }
}