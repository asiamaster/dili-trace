package com.dili.trace.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.dili.ss.domain.annotation.Like;
import com.dili.ss.domain.annotation.Operator;
import com.dili.trace.domain.DetectRecord;
import com.dili.trace.domain.DetectRequest;
import com.dili.trace.domain.ImageCert;
import com.dili.trace.enums.BillTypeEnum;
import com.dili.trace.enums.BillVerifyStatusEnum;
import com.dili.trace.enums.DetectStatusEnum;
import com.dili.trace.enums.WeightUnitEnum;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Alvin.Li
 */
public class DetectRequestOutDto extends DetectRequest {
    /**
     * 报备单类型
     */
    @Transient
    private Integer billType;
    @Transient
    private Integer truckType;
    @Transient
    private Integer detectStatus;
    /**
     * 商品名称
     */
    @Transient
    private String productName;

    /**
     * 业户名称
     */
    @Transient
    private String name;

    /**
     * 摊位号
     */
    @Transient
    private String tallyAreaNo;

    /**
     * 重量
     */
    @Transient
    private BigDecimal weight;

    /**
     * 重量单位
     */
    @Transient
    private Integer weightUnit;


    /**
     * 审核人
     */
    @Transient
    private String operatorName;

    /**
     * 报备时间
     */
    @Transient
    @JSONField(format ="yyyy-MM-dd HH-mm-ss" )
    private Date billCreated;

    /**
     * 审核时间
     */
    @Transient
    @JSONField(format ="yyyy-MM-dd HH-mm-ss" )
    private Date billOperationTime;

    /**
     * 报备单修改时间
     */
    @Transient
    @JSONField(format ="yyyy-MM-dd HH-mm-ss" )
    private Date billModified;

    /**
     * 产地
     */
    @Transient
    private String originName;

    /**
     * 报备单审核状态
     */
    @Transient
    private Integer verifyStatus;

    /**
     * 车牌号
     */
    @Transient
    private String plate;


    /**
     * 上游企业名称
     */
    @Transient
    private String upStreamName;


    @Transient
    private String productAliasName;

    @Transient
    private Long productId;

    @Transient
    private Long originId;


    /**
     * 报备单编号
     */
    @Transient
    private String billCode;

    /**
     * 品牌名称
     */
    @Transient
    private String brandName;
    /**
     * 品牌ID
     */
    @Transient
    private Long brandId;


    /**
     * 规格名称
     */
    @Transient
    private String specName;

    /**
     * 审核时间
     */
    @Transient
    @JSONField(format ="yyyy-MM-dd HH-mm-ss" )
    private Date verifyDateTime;
    /**
     * 审核人
     */
    @Transient
    private String verifyOperatorName;


    /**
     * 检测记录ID
     */
    private Long latestDetectRecordId;
    /**
     * 检测记录
     */
    @Transient
    private DetectRecord latestDetectRecord;

    /**
     * 到货摊位
     */
    @Transient
    private List<String> arrivalTallynos;

    @JSONField(format = "yyyy-MM-dd HH:mm")
    private LocalDateTime arrivalDatetime;

    public LocalDateTime getArrivalDatetime() {
        return arrivalDatetime;
    }

    public void setArrivalDatetime(LocalDateTime arrivalDatetime) {
        this.arrivalDatetime = arrivalDatetime;
    }

    public List<String> getArrivalTallynos() {
        return arrivalTallynos;
    }

    public void setArrivalTallynos(List<String> arrivalTallynos) {
        this.arrivalTallynos = arrivalTallynos;
    }

    public Long getLatestDetectRecordId() {
        return latestDetectRecordId;
    }

    public void setLatestDetectRecordId(Long latestDetectRecordId) {
        this.latestDetectRecordId = latestDetectRecordId;
    }

    public DetectRecord getLatestDetectRecord() {
        return latestDetectRecord;
    }

    public void setLatestDetectRecord(DetectRecord latestDetectRecord) {
        this.latestDetectRecord = latestDetectRecord;
    }

    public Date getVerifyDateTime() {
        return verifyDateTime;
    }

    public void setVerifyDateTime(Date verifyDateTime) {
        this.verifyDateTime = verifyDateTime;
    }

    public String getVerifyOperatorName() {
        return verifyOperatorName;
    }

    public void setVerifyOperatorName(String verifyOperatorName) {
        this.verifyOperatorName = verifyOperatorName;
    }

    public String getBillCode() {
        return billCode;
    }

    public void setBillCode(String billCode) {
        this.billCode = billCode;
    }

    private List<ImageCert> imageCertList;

    private List<DetectRecord> detectRecordList;

    public List<DetectRecord> getDetectRecordList() {
        return detectRecordList;
    }

    public void setDetectRecordList(List<DetectRecord> detectRecordList) {
        this.detectRecordList = detectRecordList;
    }

    public List<ImageCert> getImageCertList() {
        return imageCertList;
    }

    public void setImageCertList(List<ImageCert> imageCertList) {
        this.imageCertList = imageCertList;
    }

    public Integer getBillType() {
        return billType;
    }

    public void setBillType(Integer billType) {
        this.billType = billType;
    }

    public Integer getDetectStatus() {
        return detectStatus;
    }

    public void setDetectStatus(Integer detectStatus) {
        this.detectStatus = detectStatus;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTallyAreaNo() {
        return tallyAreaNo;
    }

    public void setTallyAreaNo(String tallyAreaNo) {
        this.tallyAreaNo = tallyAreaNo;
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

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Date getBillCreated() {
        return billCreated;
    }

    public void setBillCreated(Date billCreated) {
        this.billCreated = billCreated;
    }

    public Date getBillOperationTime() {
        return billOperationTime;
    }

    public void setBillOperationTime(Date billOperationTime) {
        this.billOperationTime = billOperationTime;
    }

    public Date getBillModified() {
        return billModified;
    }

    public void setBillModified(Date billModified) {
        this.billModified = billModified;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public Integer getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(Integer verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getUpStreamName() {
        return upStreamName;
    }

    public void setUpStreamName(String upStreamName) {
        this.upStreamName = upStreamName;
    }

    public String getProductAliasName() {
        return productAliasName;
    }

    public void setProductAliasName(String productAliasName) {
        this.productAliasName = productAliasName;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getOriginId() {
        return originId;
    }

    public void setOriginId(Long originId) {
        this.originId = originId;
    }

    public Integer getTruckType() {
        return truckType;
    }

    public void setTruckType(Integer truckType) {
        this.truckType = truckType;
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

    public String getSpecName() {
        return specName;
    }

    public void setSpecName(String specName) {
        this.specName = specName;
    }

    @Transient
    public String getBillTypeName() {
        return BillTypeEnum.fromCode(this.billType).map(BillTypeEnum::getName).orElse("");

    }

    @Transient

    public String getWeightUnitName() {
        return WeightUnitEnum.toName(this.weightUnit);
    }

    @Transient
    public String getDetectStatusName() {
        return DetectStatusEnum.fromCode(this.detectStatus).map(DetectStatusEnum::getName).orElse("");
    }

    @Transient
    public String getVerifyStatusName() {
        return BillVerifyStatusEnum.fromCode(this.verifyStatus).map(BillVerifyStatusEnum::getName).orElse("");
    }
}

