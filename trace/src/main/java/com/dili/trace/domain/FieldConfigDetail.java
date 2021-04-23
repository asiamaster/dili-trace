package com.dili.trace.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.dili.ss.domain.BaseDomain;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "field_config_detail")
public class FieldConfigDetail extends BaseDomain {
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 配置ID
     */
    @Column(name = "field_config_id")
    private Long fieldConfigId;
    /**
     * field id
     */
    @Column(name = "default_id")
    private Long defaultId;

    /**
     * 是否显示
     */
    @Column(name = "displayed")
    private Integer displayed;
    /**
     * 是否必填
     */
    @Column(name = "required")
    private Integer required;
    /**
     * 默认值
     */
    @Column(name = "default_value")
    private String defaultValue;

    /**
     * 是否可用
     */
    @Column(name = "is_valid")
    private Integer isValid;

    /**
     * 创建时间
     */
    @Column(name = "created")
    private LocalDateTime created;

    /**
     * 更新时间
     */
    @Column(name = "modified")
    private LocalDateTime modified;

    /**
     * 显示field条件值
     */
    @Column(name = "available_values")
    @JSONField(serialize = false)
    private String availableValues;


    public String getAvailableValues() {
        return availableValues;
    }

    public void setAvailableValues(String availableValues) {
        this.availableValues = availableValues;
    }
    @Transient
    public List<Object> getAvailableValueList() {
        try {
            List<Object>list=JSON.parseArray(StringUtils.trimToNull(this.availableValues));
            if(list==null){
                return new ArrayList<>();
            }
            return list;
        }catch (Exception e){
            return new ArrayList<>();
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getFieldConfigId() {
        return fieldConfigId;
    }

    public void setFieldConfigId(Long fieldConfigId) {
        this.fieldConfigId = fieldConfigId;
    }


    public Integer getDisplayed() {
        return displayed;
    }

    public void setDisplayed(Integer displayed) {
        this.displayed = displayed;
    }

    public Integer getRequired() {
        return required;
    }

    public void setRequired(Integer required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }

    public Long getDefaultId() {
        return defaultId;
    }

    public void setDefaultId(Long defaultId) {
        this.defaultId = defaultId;
    }

    public Integer getIsValid() {
        return isValid;
    }

    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }
}
