package com.dili.trace.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.alibaba.fastjson.annotation.JSONField;
import com.dili.ss.domain.BaseDomain;

import com.dili.trace.enums.WeightUnitEnum;
import io.swagger.annotations.ApiModelProperty;

/**
 * 由MyBatis Generator工具自动生成
 * <p>
 * This file was generated on 2019-07-26 09:20:34.
 */
@SuppressWarnings("serial")
@Table(name = "`product_stock`")
public class ProductStock extends BaseDomain {

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @JSONField(serialize = false)
    private Long id;

    /**
     * 业户姓名
     */
    @ApiModelProperty(value = "业户姓名")
    @Column(name = "`user_name`")
    private String userName;

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户iD")
    @Column(name = "`user_id`")
    private Long userId;

    /**
     * 市场ID
     */
    @ApiModelProperty(value = "市场ID")
    @Column(name = "`market_id`")
    private Long marketId;

    /**
     * 保存类型 {@link com.dili.trace.enums.PreserveTypeEnum}
     */
    @ApiModelProperty(value = "保存类型")
    @Column(name = "`preserve_type`")
    private Integer preserveType;

    /**
     * 商品名称
     */
    @ApiModelProperty(value = "商品名称")
    @Column(name = "`product_name`")
    private String productName;

    /**
     * 商品ID
     */
    @ApiModelProperty(value = "商品ID")
    @Column(name = "`product_id`")
    private Long productId;

    /**
     * 库存重量
     */
    @ApiModelProperty(value = "库存重量")
    @Column(name = "`stock_weight`")
    private BigDecimal stockWeight;

    /**
     * 累计总重量
     */
    @ApiModelProperty(value = "累计总重量")
    @Column(name = "`total_weight`")
    private BigDecimal totalWeight;

    /**
     * 重量单位 {@link com.dili.trace.enums.WeightUnitEnum}
     */
    @ApiModelProperty(value = "重量单位")
    @Column(name = "`weight_unit`")
    private Integer weightUnit;

    /**
     * 规格名称
     */
    @ApiModelProperty(value = "规格名称")
    @Column(name = "`spec_name`")
    private String specName;

    /**
     * 品牌ID
     */
    @ApiModelProperty(value = "品牌ID")
    @Column(name = "`brand_id`")
    private Long brandId;

    /**
     * 创建时间
     */
    @Column(name = "`created`")
    private Date created;

    /**
     * 修改时间
     */
    @Column(name = "`modified`")
    private Date modified;

    /**
     * 批次数量
     */
    @Column(name = "`trade_detail_num`")
    private Integer tradeDetailNum;

    /**
     * 品牌名称
     */
    @ApiModelProperty(value = "品牌名称")
    @Column(name = "`brand_name`")
    private String brandName;

    /**
     * 单位名称
     */
    @Transient
    private String weightUnitName;

    public String getWeightUnitName() {
        return WeightUnitEnum.toName(this.getWeightUnit());
    }

    public void setWeightUnitName(String weightUnitName) {
        this.weightUnitName = weightUnitName;
    }

    @Transient
    public Long getProductStockId() {
        return this.id;
    }

    /**
     * @return Long return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return Long return the userId
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * @return Integer return the preserveType
     */
    public Integer getPreserveType() {
        return preserveType;
    }

    /**
     * @param preserveType the preserveType to set
     */
    public void setPreserveType(Integer preserveType) {
        this.preserveType = preserveType;
    }

    /**
     * @return String return the productName
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @param productName the productName to set
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * @return Long return the productId
     */
    public Long getProductId() {
        return productId;
    }

    /**
     * @param productId the productId to set
     */
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    /**
     * @return BigDecimal return the stockWeight
     */
    public BigDecimal getStockWeight() {
        return stockWeight;
    }

    /**
     * @param stockWeight the stockWeight to set
     */
    public void setStockWeight(BigDecimal stockWeight) {
        this.stockWeight = stockWeight;
    }

    /**
     * @return BigDecimal return the totalWeight
     */
    public BigDecimal getTotalWeight() {
        return totalWeight;
    }

    /**
     * @param totalWeight the totalWeight to set
     */
    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }

    /**
     * @return String return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return Integer return the weightUnit
     */
    public Integer getWeightUnit() {
        return weightUnit;
    }

    /**
     * @param weightUnit the weightUnit to set
     */
    public void setWeightUnit(Integer weightUnit) {
        this.weightUnit = weightUnit;
    }

    /**
     * @return Date return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @param created the created to set
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * @return Date return the modified
     */
    public Date getModified() {
        return modified;
    }

    /**
     * @param modified the modified to set
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * @return String return the specName
     */
    public String getSpecName() {
        return specName;
    }

    /**
     * @param specName the specName to set
     */
    public void setSpecName(String specName) {
        this.specName = specName;
    }

    /**
     * @return Long return the brandId
     */
    public Long getBrandId() {
        return brandId;
    }

    /**
     * @param brandId the brandId to set
     */
    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }


    /**
     * @return String return the brandName
     */
    public String getBrandName() {
        return brandName;
    }

    /**
     * @param brandName the brandName to set
     */
    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }


    /**
     * @return Integer return the tradeDetailNum
     */
    public Integer getTradeDetailNum() {
        return tradeDetailNum;
    }

    /**
     * @param tradeDetailNum the tradeDetailNum to set
     */
    public void setTradeDetailNum(Integer tradeDetailNum) {
        this.tradeDetailNum = tradeDetailNum;
    }

    public Long getMarketId() {
        return marketId;
    }

    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }
}