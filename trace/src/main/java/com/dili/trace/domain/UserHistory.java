package com.dili.trace.domain;

import com.dili.ss.domain.BaseDomain;
import com.dili.ss.dto.IBaseDomain;
import com.dili.ss.metadata.FieldEditor;
import com.dili.ss.metadata.annotation.EditMode;
import com.dili.ss.metadata.annotation.FieldDef;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import javax.persistence.*;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * 由MyBatis Generator工具自动生成
 * <p>
 * This file was generated on 2019-07-26 09:20:35.
 */
@Table(name = "`user_history`")
public class UserHistory extends BaseDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private Long id;

    @ApiModelProperty(value = "用户ID")
    @Column(name = "`user_id`")
    private Long userId;


    @ApiModelProperty(value = "名称")
    @Column(name = "`name`")
    private String name;


    @ApiModelProperty(value = "手机号（帐号）")
    @Column(name = "`phone`")
    private String phone;

    @ApiModelProperty(value = "理货区号")
    @Column(name = "`tally_area_nos`")
    private String tallyAreaNos;


    @ApiModelProperty(value = "身份证号")
    @Column(name = "`card_no`")
    private String cardNo;


    @ApiModelProperty(value = "地址")
    @Column(name = "`addr`")
    private String addr;

    @ApiModelProperty(value = "销售城市ID")
    @Column(name = "`sales_city_id`")
    private Long salesCityId;


    @ApiModelProperty(value = "销售城市名称")
    @Column(name = "`sales_city_name`")
    private String salesCityName;


    @ApiModelProperty(value = "状态")
    @Column(name = "`state`")
    private Integer state;


    @Column(name = "`yn`")
    private Integer yn;

    @ApiModelProperty(value = "创建时间")
    @Column(name = "`created`")
    private Date created;


    @ApiModelProperty(value = "修改时间")
    @Column(name = "`modified`")
    private Date modified;


    @ApiModelProperty(value = "车牌号")
    @Column(name = "`user_plates`")
    private String userPlates;

    @ApiModelProperty(value = "车牌数量")
    @Column(name = "`plate_amount`")
    private Integer plateAmount;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Integer getYn() {
        return yn;
    }

    public void setYn(Integer yn) {
        this.yn = yn;
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

    public String getUserPlates() {
        return userPlates;
    }

    public void setUserPlates(String userPlates) {
        this.userPlates = userPlates;
    }

    public Integer getPlateAmount() {
        return plateAmount;
    }

    public void setPlateAmount(Integer plateAmount) {
        this.plateAmount = plateAmount;
    }
}