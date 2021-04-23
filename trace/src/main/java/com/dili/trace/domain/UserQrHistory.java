package com.dili.trace.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.dili.ss.domain.BaseDomain;

import io.swagger.annotations.ApiModelProperty;

/**
 * 由MyBatis Generator工具自动生成
 * <p>
 * This file was generated on 2019-07-26 09:20:35.
 */
@Table(name = "`user_qr_history`")
public class UserQrHistory extends BaseDomain {
    /**
     * 二维码条目主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private Long id;

    /**
     * 业户主键
     */
    @Column(name = "`user_info_id`")
    private Long userInfoId;


    /**
     * 二维码条目类型 {@link com.dili.trace.glossary.QrItemTypeEnum}
     */
    @Column(name = "`qr_status`")
    private Integer qrStatus;

    /**
     * 二维码内容
     */
    @Column(name = "`content`")
    private String content;

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
     * qr事件ID
     */
    @ApiModelProperty(value = "qr事件ID")
    @Column(name = "`qr_history_event_id`")
    private Long qrHistoryEventId;

    /**
     * qr事件类型
     */
    @Column(name = "`qr_history_event_type`")
    private Integer qrHistoryEventType;

    /**
     * 交易单ID
     */
//    @ApiModelProperty(value = "交易单ID")
//    @Column(name = "`trade_request_id`")
//    private Long tradeRequestId;

    /**
     * 查验状态
     */
    @ApiModelProperty(value = "查验状态")
    @Column(name = "`verify_status`")
    private Integer verifyStatus;




    /**
     * 是否有效
     */
    @ApiModelProperty(value = "是否有效")
    @Column(name = "`is_valid`")
    private Integer isValid;

//    public Long getTradeRequestId() {
//        return tradeRequestId;
//    }
//
//    public void setTradeRequestId(Long tradeRequestId) {
//        this.tradeRequestId = tradeRequestId;
//    }

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
     * @return Integer return the qrStatus
     */
    public Integer getQrStatus() {
        return qrStatus;
    }

    /**
     * @param qrStatus the qrStatus to set
     */
    public void setQrStatus(Integer qrStatus) {
        this.qrStatus = qrStatus;
    }

    /**
     * @return String return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }


    /**
     * @return Long return the billId
     */
//    public Long getBillId() {
//        return billId;
//    }
//
//    /**
//     * @param billId the billId to set
//     */
//    public void setBillId(Long billId) {
//        this.billId = billId;
//    }

    /**
     * @return Integer return the verifyStatus
     */
    public Integer getVerifyStatus() {
        return verifyStatus;
    }

    /**
     * @param verifyStatus the verifyStatus to set
     */
    public void setVerifyStatus(Integer verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    /**
     * @return Integer return the isValid
     */
    public Integer getIsValid() {
        return isValid;
    }

    /**
     * @param isValid the isValid to set
     */
    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }

    public Long getQrHistoryEventId() {
        return qrHistoryEventId;
    }

    public void setQrHistoryEventId(Long qrHistoryEventId) {
        this.qrHistoryEventId = qrHistoryEventId;
    }

    public Integer getQrHistoryEventType() {
        return qrHistoryEventType;
    }

    public void setQrHistoryEventType(Integer qrHistoryEventType) {
        this.qrHistoryEventType = qrHistoryEventType;
    }

    public Long getUserInfoId() {
        return userInfoId;
    }

    public void setUserInfoId(Long userInfoId) {
        this.userInfoId = userInfoId;
    }
}