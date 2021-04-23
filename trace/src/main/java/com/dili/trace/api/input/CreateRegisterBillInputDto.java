package com.dili.trace.api.input;

import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.customer.sdk.domain.TallyingArea;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.trace.domain.ImageCert;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.enums.CheckinStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class CreateRegisterBillInputDto {
    /**
     * 市场ID
     */
    private Long marketId;
    /**
     * 商品别名
     */
    private String productAliasName;


    /**
     * 报备单ID
     */
    @ApiModelProperty(value = "报备单ID")
    private Long billId;

    /**
     * 保存类型
     */
    @ApiModelProperty(value = "保存类型")
    private Integer preserveType;

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
     * 重量
     */
    @ApiModelProperty(value = "重量")
    private BigDecimal weight;

    /**
     * 重量单位
     */
    @ApiModelProperty(value = "重量单位")
    private Integer weightUnit;


    /**
     * 皮重
     */
    @ApiModelProperty(value = "皮重")
    private BigDecimal truckTareWeight;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 规格
     */
    @ApiModelProperty(value = "规格")
    private String specName;

    /**
     * 拼车类型
     */
    @ApiModelProperty(value = "拼车类型")
    private Integer truckType;

    /**
     * 报备类型
     */
    @ApiModelProperty(value = "报备类型")
    private Integer billType;

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
    @ApiModelProperty(value = "车牌")
    private String plate;
    /**
     * 上游企业ID
     */
    @ApiModelProperty(value = "上游企业ID")
    private Long upStreamId;

    /**
     * 主台账编号
     */
    @ApiModelProperty(value = "主台账编号")
    private String registerHeadCode;

    /**
     * 计量类型。10-计件 20-计重。默认计件。
     */
    @ApiModelProperty(value = "计量类型。10-计件 20-计重。默认计件。")
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
     * 区号
     */
    @ApiModelProperty(value = "区号")
    private String area;

    /**
     * 包装
     */
    @ApiModelProperty(value = "包装")
    private String packaging;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 查验状态
     */
    @ApiModelProperty(value = "查验状态")
    private Integer verifyStatus;

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
     * 原因
     */
    @ApiModelProperty(value = "原因")
    private String reason;

    /**
     * 单据类型
     */
    @ApiModelProperty(value = "单据类型")
    private Integer registType;
    /**
     * 创建来源
     */
    @ApiModelProperty(value = "创建来源")
    private Integer creationSource;

    /**
     * 是否打印检测报告
     */
    @ApiModelProperty(value = "是否打印检测报告")
    private Integer isPrintCheckSheet;

    /**
     * 业户 名
     */
    private String name;
    /**
     * 企业名
     */
    private String corporateName;


    /**
     * 到场时间
     */
    private Date arrivalDatetime;

    /**
     * 到货摊位
     */
    private List<String> arrivalTallynos;

    public Date getArrivalDatetime() {
        return arrivalDatetime;
    }

    public void setArrivalDatetime(Date arrivalDatetime) {
        this.arrivalDatetime = arrivalDatetime;
    }

    public List<String> getArrivalTallynos() {
        return arrivalTallynos;
    }

    public void setArrivalTallynos(List<String> arrivalTallynos) {
        this.arrivalTallynos = arrivalTallynos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
//
//    /**
//     * 检测报告url
//     */
//    private String detectReportUrl;
//    /**
//     * 产地凭证url
//     */
//    private String originCertifiyUrl;
//
//    /**
//     * 处理结果url
//     */
//    private String handleResultUrl;

//    public String getDetectReportUrl() {
//        return detectReportUrl;
//    }
//
//    public void setDetectReportUrl(String detectReportUrl) {
//        this.detectReportUrl = detectReportUrl;
//    }
//
//    public String getOriginCertifiyUrl() {
//        return originCertifiyUrl;
//    }
//
//    public void setOriginCertifiyUrl(String originCertifiyUrl) {
//        this.originCertifiyUrl = originCertifiyUrl;
//    }
//
//    public String getHandleResultUrl() {
//        return handleResultUrl;
//    }
//
//    public void setHandleResultUrl(String handleResultUrl) {
//        this.handleResultUrl = handleResultUrl;
//    }

    public Integer getCreationSource() {
        return creationSource;
    }

    public void setCreationSource(Integer creationSource) {
        this.creationSource = creationSource;
    }

    /**
     * 图片证明列表
     */
    private List<ImageCert> imageCertList;

    public RegisterBill build(CustomerExtendDto user, Long marketId) {
        RegisterBill registerBill = new RegisterBill();
        registerBill.setId(this.getBillId());
        registerBill.setRegistType(this.getRegistType());
        // registerBill.setOperatorName(user.getName());
        // registerBill.setOperatorId(user.getId());
        registerBill.setUserId(user.getId());
        registerBill.setName(user.getName());
        registerBill.setCorporateName(this.getCorporateName());
        registerBill.setMarketId(marketId);
        registerBill.setHasOriginCertifiy(YesOrNoEnum.NO.getCode());
        registerBill.setHasDetectReport(YesOrNoEnum.NO.getCode());
        registerBill.setHasHandleResult(YesOrNoEnum.NO.getCode());
        List<TallyingArea> tallyingAreaList = user.getTallyingAreaList();
        StringBuffer sourceIds = new StringBuffer();
        StringBuffer sourceNames = new StringBuffer();
        if (!CollectionUtils.isEmpty(tallyingAreaList)) {
            tallyingAreaList.forEach(t -> {
                sourceIds.append(t.getAssetsId()).append(",");
                sourceNames.append(t.getAssetsName()).append(",");
            });

            registerBill.setSourceId(sourceIds.substring(0, sourceIds.length() - 1));
            registerBill.setSourceName(sourceNames.substring(0, sourceNames.length() - 1));
        }

//        registerBill.setAddr(user.getAddr());
//        registerBill.setIdCardNo(user.getCardNo());
        registerBill.setPhone(user.getContactsPhone());
//        registerBill.setThirdPartyCode(user.getThirdPartyCode());
        registerBill.setWeight(this.getWeight());
        registerBill.setWeightUnit(this.getWeightUnit());
        registerBill.setOriginId(this.getOriginId());
        registerBill.setOriginName(this.getOriginName());
        registerBill.setProductId(this.getProductId());
        registerBill.setProductName(this.getProductName());
        registerBill.setSpecName(StringUtils.trim(this.getSpecName()));
        registerBill.setPreserveType(this.getPreserveType());
        registerBill.setBillType(this.getBillType());
        registerBill.setTruckType(this.getTruckType());
        registerBill.setBrandId(this.getBrandId());
        registerBill.setBrandName(StringUtils.trim(this.getBrandName()));
        registerBill.setPlate(this.getPlate());
        registerBill.setUpStreamId(this.getUpStreamId());
        registerBill.setRegisterHeadCode(this.getRegisterHeadCode());
        registerBill.setMeasureType(this.getMeasureType());
        registerBill.setPieceNum(this.getPieceNum());
        registerBill.setPieceWeight(this.getPieceWeight());
        registerBill.setArea(this.getArea());
        registerBill.setPackaging(this.getPackaging());
        registerBill.setRemark(this.getRemark());
        registerBill.setVerifyStatus(this.getVerifyStatus());
        registerBill.setReason(this.getReason());
        registerBill.setIsPrintCheckSheet(this.getIsPrintCheckSheet());
        registerBill.setTruckTareWeight(this.getTruckTareWeight());
//        registerBill.setTareWeight(this.getTareWeight());
        registerBill.setUnitPrice(this.getUnitPrice());
//        registerBill.setIsCheckin(YesOrNoEnum.NO.getCode());
        registerBill.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
//        registerBill.setOrderType(this.getOrderType());

        registerBill.setArrivalTallynos(this.getArrivalTallynos());
        registerBill.setArrivalDatetime(this.getArrivalDatetime());
        return registerBill;
    }

    public List<ImageCert> getImageCertList() {
        return imageCertList;
    }

    public void setImageCertList(List<ImageCert> imageCertList) {
        this.imageCertList = imageCertList;
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
     * @return Integer return the billType
     */
    public Integer getBillType() {
        return billType;
    }

    /**
     * @param billType the billType to set
     */
    public void setBillType(Integer billType) {
        this.billType = billType;
    }


    /**
     * @return Long return the billId
     */
    public Long getBillId() {
        return billId;
    }

    /**
     * @param billId the billId to set
     */
    public void setBillId(Long billId) {
        this.billId = billId;
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
     * @return Integer return the truckType
     */
    public Integer getTruckType() {
        return truckType;
    }

    /**
     * @param truckType the truckType to set
     */
    public void setTruckType(Integer truckType) {
        this.truckType = truckType;
    }


    /**
     * @return String return the plate
     */
    public String getPlate() {
        return plate;
    }

    /**
     * @param plate the plate to set
     */
    public void setPlate(String plate) {
        this.plate = plate;
    }


    /**
     * @return Long return the upStreamId
     */
    public Long getUpStreamId() {
        return upStreamId;
    }

    /**
     * @param upStreamId the upStreamId to set
     */
    public void setUpStreamId(Long upStreamId) {
        this.upStreamId = upStreamId;
    }

    public String getRegisterHeadCode() {
        return registerHeadCode;
    }

    public void setRegisterHeadCode(String registerHeadCode) {
        this.registerHeadCode = registerHeadCode;
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

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(Integer verifyStatus) {
        this.verifyStatus = verifyStatus;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getRegistType() {
        return registType;
    }

    public void setRegistType(Integer registType) {
        this.registType = registType;
    }

    public Integer getIsPrintCheckSheet() {
        return isPrintCheckSheet;
    }

    public void setIsPrintCheckSheet(Integer isPrintCheckSheet) {
        this.isPrintCheckSheet = isPrintCheckSheet;
    }

    public String getCorporateName() {
        return corporateName;
    }

    public void setCorporateName(String corporateName) {
        this.corporateName = corporateName;
    }

    public BigDecimal getTruckTareWeight() {
        return truckTareWeight;
    }

    public void setTruckTareWeight(BigDecimal truckTareWeight) {
        this.truckTareWeight = truckTareWeight;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getProductAliasName() {
        return productAliasName;
    }

    public void setProductAliasName(String productAliasName) {
        this.productAliasName = productAliasName;
    }

    public Long getMarketId() {
        return marketId;
    }

    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }
}
