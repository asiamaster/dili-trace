package com.dili.trace.api.input;

import javax.persistence.Column;
import javax.persistence.Transient;

import com.dili.ss.domain.annotation.Like;
import com.dili.ss.domain.annotation.Operator;

import com.dili.trace.domain.ProductStock;

import io.swagger.annotations.ApiModelProperty;

public class ProductStockQueryDto extends ProductStock {
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

    /**
     * 商品名称LIKE
     */
    @ApiModelProperty(value = "商品名称LIKE")
    @Column(name = "`product_name`")
    @Like(value = "RIGHT")
    private String likeProductName;

    /**
     * 交易单明细数量
     */
    @Column(name = "`trade_detail_num`")
    @Operator(Operator.GREAT_EQUAL_THAN)
    private Integer minTradeDetailNum;


    @Transient
    private Long productStockId;

    @Override
    public Long getProductStockId() {
        return productStockId;
    }

    public void setProductStockId(Long productStockId) {
        this.productStockId = productStockId;
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
     * @return Integer return the minTradeDetailNum
     */
    public Integer getMinTradeDetailNum() {
        return minTradeDetailNum;
    }

    /**
     * @param minTradeDetailNum the minTradeDetailNum to set
     */
    public void setMinTradeDetailNum(Integer minTradeDetailNum) {
        this.minTradeDetailNum = minTradeDetailNum;
    }

}