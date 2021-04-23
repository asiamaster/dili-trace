package com.dili.trace.dto;

import com.dili.ss.domain.annotation.Like;
import com.dili.ss.domain.annotation.Operator;
import com.dili.trace.domain.RegisterHead;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.List;

/**
 * 进门主台账单实体类扩展
 *
 * @author Lily
 */
public class RegisterHeadDto extends RegisterHead {
    /**
     * 查询开始时间
     */
    @ApiModelProperty(value = "查询开始时间")
    @Column(name = "`created`")
    @Operator(Operator.GREAT_EQUAL_THAN)
    private String createdStart;

    /**
     * 查询结束时间
     */
    @ApiModelProperty(value = "查询结束时间")
    @Column(name = "`created`")
    @Operator(Operator.LITTLE_EQUAL_THAN)
    private String createdEnd;


    @Column(name = "`remain_weight`")
    @Operator(Operator.GREAT_THAN)
    private BigDecimal minRemainWeight;

    @Column(name = "`id`")
    @Operator(Operator.IN)
    private List<Long> idList;



    /**
     * 商品名称
     */
    @ApiModelProperty(value = "商品名称")
    @Column(name = "`product_name`")
    @Operator(Like.BOTH)
    private String likeProductName;
    /**
     * 查询关键字
     */
    @Transient
    private String keyword;


    /**
     * 市场名称
     */
    @Transient
    private String marketName;

    public List<Long> getIdList() {
        return idList;
    }

    public void setIdList(List<Long> idList) {
        this.idList = idList;
    }

    public String getCreatedStart() {
        return createdStart;
    }

    public void setCreatedStart(String createdStart) {
        this.createdStart = createdStart;
    }

    public String getCreatedEnd() {
        return createdEnd;
    }

    public void setCreatedEnd(String createdEnd) {
        this.createdEnd = createdEnd;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }


    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public BigDecimal getMinRemainWeight() {
        return minRemainWeight;
    }

    public void setMinRemainWeight(BigDecimal minRemainWeight) {
        this.minRemainWeight = minRemainWeight;
    }

    public String getLikeProductName() {
        return likeProductName;
    }

    public void setLikeProductName(String likeProductName) {
        this.likeProductName = likeProductName;
    }
}
