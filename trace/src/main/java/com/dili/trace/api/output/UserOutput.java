package com.dili.trace.api.output;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dili.customer.sdk.domain.Attachment;

/**
 * 业户输出实体
 */
public class UserOutput {

    /**
     * 主键
     */
    private Long userId;

    /**
     * 业户状态 {@link com.dili.trace.enums.ValidateStateEnum}
     */
    private Integer validateState;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 摊位号
     */
    private String tallyAreaNos;

    /**
     * 姓名
     */
    private String userName;

    private String storeName;

    /**
     * 还不知道
     */
    private String numbers;

    /**
     * 二维码状态 {@link com.dili.trace.glossary.UserQrStatusEnum}
     */
    private Integer qrStatus;

    /**
     * 还不知道
     */
    private Integer cnt;

    /**
     * 市场名称
     */
    private String marketName;

    private Long marketId;


    /**
     * 营业执照
     */
    private String businessLicenseUrl;

    /**
     * 用户类型 {@ling com.dili.trace.glossary.UserTypeEnum}
     */
    private Integer userType;

    /**
     * 组织类型,个人/企业
     */
    private String organizationType;

    /**
     * 营业执照
     */
    private Attachment businessLicenseAttachment;

    public Long getMarketId() {
        return marketId;
    }

    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }

    public Attachment getBusinessLicenseAttachment() {
        return businessLicenseAttachment;
    }

    public void setBusinessLicenseAttachment(Attachment businessLicenseAttachment) {
        this.businessLicenseAttachment = businessLicenseAttachment;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getBusinessLicenseUrl() {
        return businessLicenseUrl;
    }

    public void setBusinessLicenseUrl(String businessLicenseUrl) {
        this.businessLicenseUrl = businessLicenseUrl;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public Integer getQrStatus() {
        return qrStatus;
    }

    public void setQrStatus(Integer qrStatus) {
        this.qrStatus = qrStatus;
    }

    public String getNumbers() {
        return numbers;
    }

    public void setNumbers(String numbers) {
        this.numbers = numbers;
    }


    public Integer getValidateState() {
        return validateState;
    }

    public void setValidateState(Integer validateState) {
        this.validateState = validateState;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTallyAreaNos() {
        return tallyAreaNos;
    }

    public void setTallyAreaNos(String tallyAreaNos) {
        this.tallyAreaNos = tallyAreaNos;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    /**
     * @return Integer return the cnt
     */
    public Integer getCnt() {
        return cnt;
    }

    /**
     * @param cnt the cnt to set
     */
    public void setCnt(Integer cnt) {
        this.cnt = cnt;
    }

    public String getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(String organizationType) {
        this.organizationType = organizationType;
    }
}
