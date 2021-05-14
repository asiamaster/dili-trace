package com.dili.trace.domain;

import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import io.swagger.annotations.ApiModelProperty;

/**
 * 由MyBatis Generator工具自动生成
 * <p>
 * This file was generated on 2019-07-31 14:56:14.
 */
public class TraceCustomer {


    private Long id;

    private String code;

    @ApiModelProperty(value = "客户姓名")
    private String name;

    @ApiModelProperty(value = "客户身份证")
    private String idNo;
    @ApiModelProperty(value = "客户地址")
    private String address;

    @ApiModelProperty(value = "业户手机号")
    private String phone;

    @ApiModelProperty(value = "印刷卡号(客户账号)")
    private String cardNo;
    private String marketName;

    private Long marketId;

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public Long getMarketId() {
        return marketId;
    }

    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }

    @ApiModelProperty(value = "作废状态")
//    @FieldDef(label="0:作废，1：活跃")
    private Integer active;

    public static TraceCustomer convert(CustomerExtendDto dto) {
        TraceCustomer c = new TraceCustomer();
        c.setAddress(dto.getCertificateAddr());
        c.setId(dto.getId());
        c.setIdNo(dto.getCertificateNumber());
        c.setName(dto.getName());
        c.setPhone(dto.getContactsPhone());
        return c;

    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }
}