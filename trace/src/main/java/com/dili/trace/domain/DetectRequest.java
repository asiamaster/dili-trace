package com.dili.trace.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.dili.ss.domain.BaseDomain;
import com.dili.trace.enums.DetectResultEnum;
import com.dili.trace.enums.DetectTypeEnum;
import com.dili.trace.glossary.SampleSourceEnum;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 检测请求创建时间
 */
@Table(name = "`detect_request`")
public class DetectRequest extends BaseDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private Long id;

    /**
     * 报备单ID
     */
    @Column(name = "`bill_id`")
    private Long billId;

    /**
     * 创建人ID
     */
    @Column(name = "`creator_id`")
    private Long creatorId;

    /**
     * 创建人姓名
     */
    @Column(name = "`creator_name`")
    private String creatorName;

    /**
     * 被指定检测员ID
     */
    @Column(name = "`designated_id`")
    private Long designatedId;

    /**
     * 被指定检测员姓名
     */
    @Column(name = "`designated_name`")
    private String designatedName;

    /**
     * 检测员ID
     */
    @Column(name = "`detector_id`")
    private Long detectorId;

    /**
     * 检测员姓名
     */
    @Column(name = "`detector_name`")
    private String detectorName;

    /**
     * 检测类型
     * {@link com.dili.trace.enums.DetectTypeEnum}
     */
    @Column(name = "`detect_type`")
    private Integer detectType;
    /**
     * 采样类型
     * {@link com.dili.trace.glossary.SampleSourceEnum}
     */
    @Column(name = "`detect_source`")
    private Integer detectSource;

    /**
     * 检测类型
     * {@link com.dili.trace.enums.DetectResultEnum}
     */
    @Column(name = "`detect_result`")
    private Integer detectResult;

    /**
     * 检测请求创建时间
     */
    @Column(name = "`created`")
    @JSONField(format ="yyyy-MM-dd HH:mm:ss" )
    private Date created;

    /**
     * 检测请求修改时间
     */
    @Column(name = "`modified`")
    @JSONField(format ="yyyy-MM-dd HH:mm:ss" )
    private Date modified;

    /**
     * 检测请求预约时间
     */
    @Column(name = "`detect_reservation_time`")
    @JSONField(format ="yyyy-MM-dd HH:mm:ss" )
    private Date detectReservationTime;


    /**
     * 检测指定时间
     */
    @Column(name = "`scheduled_detect_time`")
    @JSONField(format ="yyyy-MM-dd HH:mm:ss" )
    private Date scheduledDetectTime;

    /**
     * 检测时间
     */
    @Column(name = "`detect_time`")
    @JSONField(format ="yyyy-MM-dd HH:mm:ss" )
    private Date detectTime;

    /**
     * 检测费用
     */
    @Column(name = "`detect_fee`")
    private BigDecimal detectFee;

    /**
     * 接单时间
     */
    @Column(name = "`confirm_time`")
    @JSONField(format ="yyyy-MM-dd HH:mm:ss" )
    private Date confirmTime;

    /**
     * 采样时间
     */
    @Column(name = "`sample_time`")
    @JSONField(format ="yyyy-MM-dd HH:mm:ss" )
    private Date sampleTime;

    /**
     * 检测编号
     */
    @Column(name = "`detect_code`")
    private String detectCode;

    public Date getDetectTime() {
        return detectTime;
    }

    public void setDetectTime(Date detectTime) {
        this.detectTime = detectTime;
    }

    public BigDecimal getDetectFee() {
        return detectFee;
    }

    public void setDetectFee(BigDecimal detectFee) {
        this.detectFee = detectFee;
    }

    @Transient
    public String getDetectSourceName() {
        return SampleSourceEnum.name(this.detectSource);
    }

    @Transient
    public String getDetectTypeName() {
        return DetectTypeEnum.name(this.detectType);
    }

    public Integer getDetectSource() {
        return detectSource;
    }

    public void setDetectSource(Integer detectSource) {
        this.detectSource = detectSource;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public Long getDesignatedId() {
        return designatedId;
    }

    public void setDesignatedId(Long designatedId) {
        this.designatedId = designatedId;
    }

    public String getDesignatedName() {
        return designatedName;
    }

    public void setDesignatedName(String designatedName) {
        this.designatedName = designatedName;
    }

    public Integer getDetectType() {
        return detectType;
    }

    public void setDetectType(Integer detectType) {
        this.detectType = detectType;
    }

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


    public Long getDetectorId() {
        return detectorId;
    }

    public void setDetectorId(Long detectorId) {
        this.detectorId = detectorId;
    }

    public String getDetectorName() {
        return detectorName;
    }

    public void setDetectorName(String detectorName) {
        this.detectorName = detectorName;
    }

    @Transient
    public String getDetectResultName() {
        return DetectResultEnum.name(this.detectResult);
    }

    public Integer getDetectResult() {
        return detectResult;
    }

    public void setDetectResult(Integer detectResult) {
        this.detectResult = detectResult;
    }

    public Date getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(Date confirmTime) {
        this.confirmTime = confirmTime;
    }

    public Date getSampleTime() {
        return sampleTime;
    }

    public void setSampleTime(Date sampleTime) {
        this.sampleTime = sampleTime;
    }

    public Date getDetectReservationTime() {
        return detectReservationTime;
    }

    public void setDetectReservationTime(Date detectReservationTime) {
        this.detectReservationTime = detectReservationTime;
    }

    public String getDetectCode() {
        return detectCode;
    }

    public void setDetectCode(String detectCode) {
        this.detectCode = detectCode;
    }

    public Date getScheduledDetectTime() {
        return scheduledDetectTime;
    }

    public void setScheduledDetectTime(Date scheduledDetectTime) {
        this.scheduledDetectTime = scheduledDetectTime;
    }
}
