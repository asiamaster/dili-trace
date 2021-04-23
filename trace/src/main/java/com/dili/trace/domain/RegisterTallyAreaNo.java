package com.dili.trace.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.dili.ss.domain.BaseDomain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "`register_tallyarea_no`")
public class RegisterTallyAreaNo extends BaseDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @JSONField(serialize = false)
    private Long id;


    @Column(name = "`bill_id`")
    private Long billId;

    /**
     * 类型
     */
    @Column(name = "`bill_type`")
    private Integer billType;

    /**
     * 摊位号
     */
    @Column(name = "`tallyarea_no`")
    private String tallyareaNo;

    @Column(name = "`created`")
    private LocalDateTime created;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Integer getBillType() {
        return billType;
    }

    public void setBillType(Integer billType) {
        this.billType = billType;
    }

    public String getTallyareaNo() {
        return tallyareaNo;
    }

    public void setTallyareaNo(String tallyareaNo) {
        this.tallyareaNo = tallyareaNo;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }
}
