package com.dili.trace.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.dili.ss.domain.BaseDomain;
import com.dili.ss.dto.IBaseDomain;
import com.dili.ss.metadata.FieldEditor;
import com.dili.ss.metadata.annotation.EditMode;
import com.dili.ss.metadata.annotation.FieldDef;
import com.dili.trace.enums.VocationTypeEnum;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 由MyBatis Generator工具自动生成
 * <p>
 * This file was generated on 2019-07-26 09:20:35.
 */
@Table(name = "`user`")
public class UserInfo extends BaseDomain {
    /**
     * ID
     *
     * @return
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private Long id;


    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    @Column(name = "`name`")
    private String name;


    /**
     * 手机号（帐号）
     */
    @ApiModelProperty(value = "手机号（帐号）")
    @Column(name = "`phone`")
    private String phone;

    /**
     * 理货区号
     */
    @ApiModelProperty(value = "理货区号")
    @Column(name = "`tally_area_nos`")
    private String tallyAreaNos;

    /**
     * 身份证号
     */
    @ApiModelProperty(value = "身份证号")
    @Column(name = "`card_no`")
    private String cardNo;

    /**
     * 地址
     */
    @ApiModelProperty(value = "地址")
    @Column(name = "`addr`")
    private String addr;

    /**
     * 身份证照正面URL
     */
    @ApiModelProperty(value = "身份证照正面URL")
    @Column(name = "`card_no_front_url`")
    private String cardNoFrontUrl;

    /**
     * 身份证照反面URL
     */
    @ApiModelProperty(value = "身份证照反面URL")
    @Column(name = "`card_no_back_url`")
    private String cardNoBackUrl;

    /**
     * 营业执照URL
     */
    @ApiModelProperty(value = "营业执照URL")
    @Column(name = "`business_license_url`")
    private String businessLicenseUrl;

    /**
     * 销售城市ID
     */
    @ApiModelProperty(value = "销售城市ID")
    @Column(name = "`sales_city_id`")
    private Long salesCityId;
    /**
     * 销售城市名称
     */
    @ApiModelProperty(value = "销售城市名称")
    @Column(name = "`sales_city_name`")
    private String salesCityName;
    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    @Column(name = "`state`")
    private Integer state;

    /**
     * 密码
     */
//    @ApiModelProperty(value = "密码")
//    @Column(name = "`password`")
//    @JSONField(serialize = false)
//    private String password;

    /**
     * 删除状态1:正常 0：删除
     *
     * @return
     */
    @Column(name = "`yn`")
    private Integer yn;

    /**
     * 版本
     *
     * @return
     */
//    @Column(name = "`version`")
//    private Integer version;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @Column(name = "`created`")
    private Date created;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    @Column(name = "`modified`")
    private Date modified;


    /**
     * 0：未删除，非0:已经删除
     */
    @Column(name = "`is_delete`")
    private Long isDelete;

    /**
     * 法人
     */
    @ApiModelProperty(value = "法人姓名")
    @Column(name = "`legal_person`")

    private String legalPerson;

    /**
     * 统一信用代码
     */
    @ApiModelProperty(value = "统一信用代码")
    @Column(name = "`license`")
    private String license;

    /**
     * 用户类型 {@link com.dili.trace.glossary.UserTypeEnum}
     */
    @Column(name = "`user_type`")
    private Integer userType;
    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID")
    @Column(name = "`user_id`")
    private Long userId;
    /**
     * 所属市场
     */
    @ApiModelProperty(value = "所属市场")
    @Column(name = "`market_id`")
    private Long marketId;

    /**
     * 所属市场名称
     */
    @ApiModelProperty(value = "所属市场名称")
    @Column(name = "`market_name`")
    private String marketName;


    /**
     * 生产许可证URL
     */
    @ApiModelProperty(value = "生产许可证URL")
    @Column(name = "`manufacturing_license_url`")
    private String manufacturingLicenseUrl;


    /**
     * 经营许可证URL
     */
    @ApiModelProperty(value = "经营许可证URL")
    @Column(name = "`operation_license_url`")
    private String operationLicenseUrl;

    /**
     * 二维码状态
     * {@link com.dili.trace.glossary.UserQrStatusEnum}
     */
    @ApiModelProperty(value = "二维码状态")
    @Column(name = "`qr_status`")
    private Integer qrStatus;

    /**
     * 前一次二维码状态
     * {@link com.dili.trace.glossary.UserQrStatusEnum}
     */
    @ApiModelProperty(value = "")
    @Column(name = "`pre_qr_status`")
    private Integer preQrStatus;


    /**
     * 审核状态
     */
    @ApiModelProperty(value = "审核状态")
    @Column(name = "`validate_state`")
    private Integer validateState;


    /**
     * 经营类型
     *
     * @return
     */
    @Column(name = "`vocation_type`")
    private Integer vocationType;


    /**
     * 业务分类id
     *
     * @return
     */
    @Column(name = "business_category_ids")
    private String businessCategoryIds;


    /**
     * 业务分类描述
     *
     * @return
     */
    @Column(name = "business_categories")
    private String businessCategories;


//    @Transient
//    default String getVocationTypeName() {
//        return VocationTypeEnum.getNameFromCode(this.getVocationType());
//    }

    /**
     * 来源
     *
     * @return
     */
    @Column(name = "`source`")
    private Integer source;

    /**
     * 微信openid
     *
     * @return
     */
    @Column(name = "`open_id`")
    private String openId;


    /**
     * 微信弹窗绑定确认时间
     *
     * @return
     */
    @Column(name = "`confirm_date`")
    private Date confirmDate;


    /**
     * 用户上报标志位
     *
     * @return
     */
    @Column(name = "`is_push`")
    private Integer isPush;


    /**
     * 用户活跃标志位
     *
     * @return
     */
    @Column(name = "`is_active`")
    private Integer isActive;

    /**
     * 经营户第三方编码
     *
     * @return
     */
    @Column(name = "`third_party_code`")
    private String thirdPartyCode;

    /**
     * 最后的同步时间
     */
    @Column(name = "`last_sync_time`")
    private Date lastSyncTime;
    /**
     * 最后是否同步成功(1:成功,0:失败)
     */
    @Column(name = "`last_sync_success`")
    private Integer lastSyncSuccess;
    /**
     * 最后的二维码变更内容
     */
    @Column(name = "`qr_content`")
    private String qrContent;

    /**
     * 最后的二维码变更历史ID
     */
    @Column(name = "`qr_history_id`")
    private Long qrHistoryId;

    public String getQrContent() {
        return qrContent;
    }

    public void setQrContent(String qrContent) {
        this.qrContent = qrContent;
    }

    public Long getQrHistoryId() {
        return qrHistoryId;
    }

    public void setQrHistoryId(Long qrHistoryId) {
        this.qrHistoryId = qrHistoryId;
    }

    /**
     * 经营户扩展信息
     *
     * @return
     */
    @Transient
    private UserExt userExt;
    @Transient
    private String plates;

    public Date getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(Date lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getCardNoFrontUrl() {
        return cardNoFrontUrl;
    }

    public void setCardNoFrontUrl(String cardNoFrontUrl) {
        this.cardNoFrontUrl = cardNoFrontUrl;
    }

    public String getCardNoBackUrl() {
        return cardNoBackUrl;
    }

    public void setCardNoBackUrl(String cardNoBackUrl) {
        this.cardNoBackUrl = cardNoBackUrl;
    }

    public String getBusinessLicenseUrl() {
        return businessLicenseUrl;
    }

    public void setBusinessLicenseUrl(String businessLicenseUrl) {
        this.businessLicenseUrl = businessLicenseUrl;
    }

    public Long getSalesCityId() {
        return salesCityId;
    }

    public void setSalesCityId(Long salesCityId) {
        this.salesCityId = salesCityId;
    }

    public String getSalesCityName() {
        return salesCityName;
    }

    public void setSalesCityName(String salesCityName) {
        this.salesCityName = salesCityName;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }

    public Integer getYn() {
        return yn;
    }

    public void setYn(Integer yn) {
        this.yn = yn;
    }

//    public Integer getVersion() {
//        return version;
//    }
//
//    public void setVersion(Integer version) {
//        this.version = version;
//    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Long getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Long isDelete) {
        this.isDelete = isDelete;
    }

    public String getLegalPerson() {
        return legalPerson;
    }

    public void setLegalPerson(String legalPerson) {
        this.legalPerson = legalPerson;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Long getMarketId() {
        return marketId;
    }

    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public String getManufacturingLicenseUrl() {
        return manufacturingLicenseUrl;
    }

    public void setManufacturingLicenseUrl(String manufacturingLicenseUrl) {
        this.manufacturingLicenseUrl = manufacturingLicenseUrl;
    }

    public String getOperationLicenseUrl() {
        return operationLicenseUrl;
    }

    public void setOperationLicenseUrl(String operationLicenseUrl) {
        this.operationLicenseUrl = operationLicenseUrl;
    }

    public Integer getQrStatus() {
        return qrStatus;
    }

    public void setQrStatus(Integer qrStatus) {
        this.qrStatus = qrStatus;
    }

    public Integer getPreQrStatus() {
        return preQrStatus;
    }

    public void setPreQrStatus(Integer preQrStatus) {
        this.preQrStatus = preQrStatus;
    }

    public Integer getValidateState() {
        return validateState;
    }

    public void setValidateState(Integer validateState) {
        this.validateState = validateState;
    }

    public Integer getVocationType() {
        return vocationType;
    }

    public void setVocationType(Integer vocationType) {
        this.vocationType = vocationType;
    }

    public String getBusinessCategoryIds() {
        return businessCategoryIds;
    }

    public void setBusinessCategoryIds(String businessCategoryIds) {
        this.businessCategoryIds = businessCategoryIds;
    }

    public String getBusinessCategories() {
        return businessCategories;
    }

    public void setBusinessCategories(String businessCategories) {
        this.businessCategories = businessCategories;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public Date getConfirmDate() {
        return confirmDate;
    }

    public void setConfirmDate(Date confirmDate) {
        this.confirmDate = confirmDate;
    }

    public Integer getIsPush() {
        return isPush;
    }

    public void setIsPush(Integer isPush) {
        this.isPush = isPush;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public String getThirdPartyCode() {
        return thirdPartyCode;
    }

    public void setThirdPartyCode(String thirdPartyCode) {
        this.thirdPartyCode = thirdPartyCode;
    }

    public UserExt getUserExt() {
        return userExt;
    }

    public void setUserExt(UserExt userExt) {
        this.userExt = userExt;
    }

    public String getPlates() {
        return plates;
    }

    public void setPlates(String plates) {
        this.plates = plates;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getLastSyncSuccess() {
        return lastSyncSuccess;
    }

    public void setLastSyncSuccess(Integer lastSyncSuccess) {
        this.lastSyncSuccess = lastSyncSuccess;
    }
}