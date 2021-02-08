package com.dili.trace.api.input;

import com.dili.ss.domain.annotation.Like;
import com.dili.ss.domain.annotation.Operator;
import com.dili.trace.domain.UserInfo;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

public class UserQueryDto extends UserInfo {
    @Column(name = "`name`")
    @Like
    private String name;

    @Column(name = "`id`")
    @Operator(Operator.IN)
    private List<Long> userInfoIdList;


    @Column(name = "`tally_area_nos`")
    @Like
    private String tallyAreaNos;

    /**
     * 创建时间开始
     *
     * @return
     */
    @Column(name = "`created`")
    @Operator(Operator.GREAT_EQUAL_THAN)
    private Date createdStart;


    /**
     * 创建时间结束
     *
     * @return
     */
    @Column(name = "`created`")
    @Operator(Operator.LITTLE_EQUAL_THAN)
    private Date createdEnd;

    /**
     * 昵称模糊查询
     *
     * @return
     */
    @Column(name = "tally_area_nos")
    @Like
    private String likeTallyAreaNos;
    /**
     * 业户名称
     *
     * @return
     */
    @Column(name = "name")
    @Like
    private String likeName;

    /**
     * 业户id集合
     *
     * @return
     */
    @Column(name = "`id`")
    @Operator(Operator.IN)
    private List<Long> Ids;

    /**
     * 姓名/手机号/经营户卡号模糊查询
     *
     * @return
     */
    @Column(name = "name")
    @Like
    private String keyword;

    /**
     * 是否有营业执照
     *
     * @return
     */
    @Transient
    private Boolean hasBusinessLicense;

    public Boolean getHasBusinessLicense() {
        return hasBusinessLicense;
    }

    public void setHasBusinessLicense(Boolean hasBusinessLicense) {
        this.hasBusinessLicense = hasBusinessLicense;
    }

    public String getLikeName() {
        return likeName;
    }

    public void setLikeName(String likeName) {
        this.likeName = likeName;
    }

    public List<Long> getIds() {
        return Ids;
    }

    public void setIds(List<Long> ids) {
        Ids = ids;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTallyAreaNos() {
        return tallyAreaNos;
    }

    @Override
    public void setTallyAreaNos(String tallyAreaNos) {
        this.tallyAreaNos = tallyAreaNos;
    }

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

    public String getLikeTallyAreaNos() {
        return likeTallyAreaNos;
    }

    public void setLikeTallyAreaNos(String likeTallyAreaNos) {
        this.likeTallyAreaNos = likeTallyAreaNos;
    }

    public List<Long> getUserInfoIdList() {
        return userInfoIdList;
    }

    public void setUserInfoIdList(List<Long> userInfoIdList) {
        this.userInfoIdList = userInfoIdList;
    }
}
