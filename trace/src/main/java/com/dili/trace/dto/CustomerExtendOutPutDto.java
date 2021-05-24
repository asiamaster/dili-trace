package com.dili.trace.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dili.customer.sdk.domain.Attachment;
import com.dili.customer.sdk.domain.dto.AttachmentGroupInfo;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.customer.sdk.enums.CustomerEnum;
import one.util.streamex.StreamEx;

import javax.persistence.Transient;
import java.util.List;

/**
 * Description:
 *
 * @author Lily.Huang
 * @date 2020/12/29
 */
public class CustomerExtendOutPutDto {
    /**
     * 类型
     */
    private Integer clientType;
    /**
     * 用户id
     */
    private Long id;
    /**
     * 名字
     */
    private String name;

    /**
     * 组织类型,个人/企业
     * {@link com.dili.customer.sdk.enums.CustomerEnum.OrganizationType}
     */
    private String organizationType;
    /**
     * 市场主键
     */
    private Long marketId;
    /**
     * 市场名称
     */
    private String marketName;
    /**
     * 园区卡号
     */
    private String tradePrintingCard;

    /**
     * 手机
     */
    private String phone;

    /**
     * 车辆信息
     */
    private List<VehicleInfoDto> vehicleInfoList;


    /**
     * 营业执照
     */
    @JSONField(serialzeFeatures = {SerializerFeature.DisableCircularReferenceDetect})
    private Attachment businessLicenseAttachment;

    public Attachment getBusinessLicenseAttachment() {
        return businessLicenseAttachment;
    }

    public void setBusinessLicenseAttachment(Attachment businessLicenseAttachment) {
        this.businessLicenseAttachment = businessLicenseAttachment;
    }


    public Integer getClientType() {
        return clientType;
    }

    public void setClientType(Integer clientType) {
        this.clientType = clientType;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<VehicleInfoDto> getVehicleInfoList() {
        return vehicleInfoList;
    }

    public void setVehicleInfoList(List<VehicleInfoDto> vehicleInfoList) {
        this.vehicleInfoList = vehicleInfoList;
    }

    public Long getMarketId() {
        return marketId;
    }

    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }

    public CustomerExtendOutPutDto() {
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public String getTradePrintingCard() {
        return tradePrintingCard;
    }

    public void setTradePrintingCard(String tradePrintingCard) {
        this.tradePrintingCard = tradePrintingCard;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(String organizationType) {
        this.organizationType = organizationType;
    }

    @Transient
    public String getUserName() {
        return this.name;
    }
}
