package com.dili.trace.api.input;

import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.trace.domain.ImageCert;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.RegisterHead;
import io.swagger.annotations.ApiModelProperty;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

/**
 * 进门主台账单参数接收类
 *
 * @author Lily
 */
public class CreateRegisterHeadInputDto {
    /**
     * 进门主台账单ID
     */
    @ApiModelProperty(value = "进门主台账单ID")
    private Long id;

    /**
     * 单据类型 {@link com.dili.trace.enums.BillTypeEnum}
     */
    @ApiModelProperty(value = "单据类型")
    private Integer billType;

    /**
     * 商品名称
     */
    @ApiModelProperty(value = "商品名称")
    private String productName;

    /**
     * 商品ID
     */
    @ApiModelProperty(value = "商品ID")
    private Long productId;

    /**
     * 计量类型 {@link com.dili.trace.enums.MeasureTypeEnum}
     */
    @ApiModelProperty(value = "计量类型")
    private Integer measureType;

    /**
     * 件数
     */
    @ApiModelProperty(value = "件数")
    private BigDecimal pieceNum;

    /**
     * 件重
     */
    @ApiModelProperty(value = "件重")
    private BigDecimal pieceWeight;

    /**
     * 总重量
     */
    @ApiModelProperty(value = "总重量")
    private BigDecimal weight;

    /**
     * 重量单位 {@link com.dili.trace.enums.WeightUnitEnum}
     */
    @ApiModelProperty(value = "重量单位")
    private Integer weightUnit;

    /**
     * 上游企业ID
     */
    @ApiModelProperty(value = "上游企业ID")
    private Long upStreamId;

    /**
     * 产地ID
     */
    @ApiModelProperty(value = "产地ID")
    private Long originId;

    /**
     * 产地名
     */
    @ApiModelProperty(value = "产地名")
    private String originName;

    /**
     * 规格
     */
    @ApiModelProperty(value = "规格")
    private String specName;

    /**
     * 品牌名称
     */
    @ApiModelProperty(value = "品牌名称")
    private String brandName;

    /**
     * 品牌ID
     */
    @ApiModelProperty(value = "品牌ID")
    private Long brandId;

    /**
     * 车牌
     */
//    @ApiModelProperty(value = "车牌")
//    private String plate;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 是否启用
     */
    @ApiModelProperty(value = "是否启用")
    private Integer active;

    /**
     * 是否废弃
     */
    @ApiModelProperty(value = "是否废弃")
    private Integer isDeleted;

    /**
     * 经营户ID
     */
    @ApiModelProperty(value = "经营户ID")
    private Long userId;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 拼车类型
     */
    private Integer truckType;
    /**
     * 皮重
     */
    private BigDecimal truckTareWeight;

    /**
     * 图片证明列表
     */
    private List<ImageCert> imageCertList;
    /**
     * 预计到场时间
     */

    private LocalDateTime arrivalDatetime;


    /**
     * 到货摊位
     */
    private List<String> arrivalTallynos;

    /**
     * 车牌号
     */
    @Transient
    private List<String> plateList;

    public RegisterHead build(CustomerExtendDto user) {
        RegisterHead registerHead = new RegisterHead();
        registerHead.setId(this.getId());
        registerHead.setUserId(user.getId());
        registerHead.setName(user.getName());
//		registerHead.setAddr(user.getAddr());
//		registerHead.setIdCardNo(user.getCardNo());
        registerHead.setPhone(user.getContactsPhone());
//		registerHead.setThirdPartyCode(user.getThirdPartyCode());
        registerHead.setBillType(this.getBillType());
        registerHead.setProductId(this.getProductId());
        registerHead.setProductName(this.getProductName());
        registerHead.setMeasureType(this.getMeasureType());
        registerHead.setPieceWeight(this.getPieceWeight());
        registerHead.setPieceNum(this.getPieceNum());
        registerHead.setWeight(this.getWeight());
        registerHead.setWeightUnit(this.getWeightUnit());
        registerHead.setUpStreamId(this.getUpStreamId());
        registerHead.setOriginId(this.getOriginId());
        registerHead.setOriginName(this.getOriginName());
        registerHead.setSpecName(StringUtils.trim(this.getSpecName()));
        registerHead.setBrandId(this.getBrandId());
        registerHead.setBrandName(StringUtils.trim(this.getBrandName()));
//        registerHead.setPlate(this.getPlate());
        registerHead.setMarketId(user.getCustomerMarket().getMarketId());
        registerHead.setRemark(this.getRemark());
        registerHead.setActive(this.getActive());
        registerHead.setUnitPrice(this.getUnitPrice());
        registerHead.setTruckType(this.getTruckType());
        registerHead.setArrivalDatetime(this.getArrivalDatetime());
        registerHead.setArrivalTallynos(this.getArrivalTallynos());

        // 车牌转大写
        List<String> plateList = StreamEx.ofNullable(this.getPlateList()).flatCollection(Function.identity())
                .filter(StringUtils::isNotBlank).map(p -> p.toUpperCase()).toList();
        registerHead.setPlateList(plateList);
        registerHead.setTruckTareWeight(this.getTruckTareWeight());
        return registerHead;
    }

    public List<String> getPlateList() {
        return plateList;
    }

    public void setPlateList(List<String> plateList) {
        this.plateList = plateList;
    }

    public List<String> getArrivalTallynos() {
        return arrivalTallynos;
    }

    public void setArrivalTallynos(List<String> arrivalTallynos) {
        this.arrivalTallynos = arrivalTallynos;
    }

    public Integer getTruckType() {
        return truckType;
    }

    public void setTruckType(Integer truckType) {
        this.truckType = truckType;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getBillType() {
        return billType;
    }

    public void setBillType(Integer billType) {
        this.billType = billType;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getMeasureType() {
        return measureType;
    }

    public void setMeasureType(Integer measureType) {
        this.measureType = measureType;
    }

    public BigDecimal getPieceNum() {
        return pieceNum;
    }

    public void setPieceNum(BigDecimal pieceNum) {
        this.pieceNum = pieceNum;
    }

    public BigDecimal getPieceWeight() {
        return pieceWeight;
    }

    public void setPieceWeight(BigDecimal pieceWeight) {
        this.pieceWeight = pieceWeight;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public Integer getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(Integer weightUnit) {
        this.weightUnit = weightUnit;
    }

    public Long getUpStreamId() {
        return upStreamId;
    }

    public void setUpStreamId(Long upStreamId) {
        this.upStreamId = upStreamId;
    }

    public Long getOriginId() {
        return originId;
    }

    public void setOriginId(Long originId) {
        this.originId = originId;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getSpecName() {
        return specName;
    }

    public void setSpecName(String specName) {
        this.specName = specName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

//    public String getPlate() {
//        return plate;
//    }
//
//    public void setPlate(String plate) {
//        this.plate = plate;
//    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<ImageCert> getImageCertList() {
        return imageCertList;
    }

    public void setImageCertList(List<ImageCert> imageCertList) {
        this.imageCertList = imageCertList;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getArrivalDatetime() {
        return arrivalDatetime;
    }

    public void setArrivalDatetime(LocalDateTime arrivalDatetime) {
        this.arrivalDatetime = arrivalDatetime;
    }

    public BigDecimal getTruckTareWeight() {
        return truckTareWeight;
    }

    public void setTruckTareWeight(BigDecimal truckTareWeight) {
        this.truckTareWeight = truckTareWeight;
    }
}
