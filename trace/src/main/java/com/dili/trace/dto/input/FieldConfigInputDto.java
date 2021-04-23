package com.dili.trace.dto.input;

import com.dili.trace.domain.FieldConfigDetail;

import java.util.List;

public class FieldConfigInputDto {
    private Long marketId;
    private Integer moduleType;
    private List<FieldConfigDetail> fieldConfigDetailList;

    public Long getMarketId() {
        return marketId;
    }

    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }

    public Integer getModuleType() {
        return moduleType;
    }

    public void setModuleType(Integer moduleType) {
        this.moduleType = moduleType;
    }

    public List<FieldConfigDetail> getFieldConfigDetailList() {
        return fieldConfigDetailList;
    }

    public void setFieldConfigDetailList(List<FieldConfigDetail> fieldConfigDetailList) {
        this.fieldConfigDetailList = fieldConfigDetailList;
    }
}
