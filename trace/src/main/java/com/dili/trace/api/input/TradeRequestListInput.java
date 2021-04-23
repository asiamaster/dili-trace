package com.dili.trace.api.input;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Transient;

import com.dili.ss.domain.annotation.Like;
import com.dili.ss.domain.annotation.Operator;
import com.dili.trace.domain.TradeRequest;

import io.swagger.annotations.ApiModelProperty;

/**
 * 交易列表查询实体
 */
public class TradeRequestListInput extends TradeRequest {

    /**
     * 商品名称（模糊查询条件）
     */
    @Column(name = "`product_name`")
    @Like()
    private String likeProductName;

    /**
     * 登记开始时间
     */
    @ApiModelProperty(value = "查询登记开始时间")
    @Column(name = "`created`")
    @Operator(Operator.GREAT_EQUAL_THAN)
    private String createdStart;

    /**
     * 登记结束时间
     */
    @ApiModelProperty(value = "查询登记结束时间")
    @Column(name = "`created`")
    @Operator(Operator.LITTLE_EQUAL_THAN)
    private String createdEnd;

    /**
     * 库存列表
     */
    @Transient
    private List<ProductStockInput> batchStockList;

    @Transient
    private Long tradeMarketId;

    public Long getTradeMarketId() {
        return tradeMarketId;
    }

    public void setTradeMarketId(Long tradeMarketId) {
        this.tradeMarketId = tradeMarketId;
    }

    /**
     * @return List<BatchStockInput> return the batchStockList
     */
    public List<ProductStockInput> getBatchStockList() {
        return batchStockList;
    }

    /**
     * @param batchStockList the batchStockList to set
     */
    public void setBatchStockList(List<ProductStockInput> batchStockList) {
        this.batchStockList = batchStockList;
    }

    /**
     * @return String return the likeProductName
     */
    public String getLikeProductName() {
        return likeProductName;
    }

    /**
     * @param likeProductName the likeProductName to set
     */
    public void setLikeProductName(String likeProductName) {
        this.likeProductName = likeProductName;
    }

    /**
     * @return String return the createdStart
     */
    public String getCreatedStart() {
        return createdStart;
    }

    /**
     * @param createdStart the createdStart to set
     */
    public void setCreatedStart(String createdStart) {
        this.createdStart = createdStart;
    }

    /**
     * @return String return the createdEnd
     */
    public String getCreatedEnd() {
        return createdEnd;
    }

    /**
     * @param createdEnd the createdEnd to set
     */
    public void setCreatedEnd(String createdEnd) {
        this.createdEnd = createdEnd;
    }

}