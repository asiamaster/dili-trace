package com.dili.trace.dto;

import com.dili.ss.domain.annotation.Like;
import com.dili.ss.domain.annotation.Operator;
import com.dili.trace.domain.UserHistory;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.util.Date;

/**
 * 由MyBatis Generator工具自动生成
 * <p>
 * This file was generated on 2019-07-26 09:20:35.
 */
public class UserHistoryListDto extends UserHistory {
    @Column(name = "`created`")
    @Operator(Operator.GREAT_EQUAL_THAN)
    private Date createdStart;

    @Column(name = "`created`")
    @Operator(Operator.LITTLE_EQUAL_THAN)
    private Date createdEnd;


    @Column(name = "`modified`")
    @Operator(Operator.GREAT_EQUAL_THAN)
    private Date modifiedStart;

    @Column(name = "`modified`")
    @Operator(Operator.LITTLE_EQUAL_THAN)
    private Date modifiedEnd;

    /**
     * 昵称模糊查询
     *
     * @return
     */
    @Column(name = "tally_area_nos")
    @Like
    private String likeTallyAreaNos;

    @Transient
    private Boolean hasBusinessLicense;

    @Column(name = "user_plates")
    @Like
    private String likeUserPlates;

    @Transient
    private Long offSet;

    public Date getCreatedStart() {
        return createdStart;
    }

    public void setCreatedStart(Date createdStart) {
        this.createdStart = createdStart;
    }

    public Date getCreatedEnd() {
        return createdEnd;
    }

    public void setCreatedEnd(Date createdEnd) {
        this.createdEnd = createdEnd;
    }

    public Date getModifiedStart() {
        return modifiedStart;
    }

    public void setModifiedStart(Date modifiedStart) {
        this.modifiedStart = modifiedStart;
    }

    public Date getModifiedEnd() {
        return modifiedEnd;
    }

    public void setModifiedEnd(Date modifiedEnd) {
        this.modifiedEnd = modifiedEnd;
    }

    public String getLikeTallyAreaNos() {
        return likeTallyAreaNos;
    }

    public void setLikeTallyAreaNos(String likeTallyAreaNos) {
        this.likeTallyAreaNos = likeTallyAreaNos;
    }

    public Boolean getHasBusinessLicense() {
        return hasBusinessLicense;
    }

    public void setHasBusinessLicense(Boolean hasBusinessLicense) {
        this.hasBusinessLicense = hasBusinessLicense;
    }

    public String getLikeUserPlates() {
        return likeUserPlates;
    }

    public void setLikeUserPlates(String likeUserPlates) {
        this.likeUserPlates = likeUserPlates;
    }

    public Long getOffSet() {
        return offSet;
    }

    public void setOffSet(Long offSet) {
        this.offSet = offSet;
    }
}