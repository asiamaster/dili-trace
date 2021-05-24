package com.dili.trace.rpc.dto;

import com.dili.customer.sdk.domain.query.CustomerQueryInput;

public class CustomerQueryDto extends CustomerQueryInput {
    private Boolean queryBusinessCategory;
    private Boolean queryVehicleInfo;
    private Boolean queryTallyingArea;
    private Boolean queryAttachment;

    public Boolean getQueryBusinessCategory() {
        return queryBusinessCategory;
    }

    public void setQueryBusinessCategory(Boolean queryBusinessCategory) {
        this.queryBusinessCategory = queryBusinessCategory;
    }

    public Boolean getQueryVehicleInfo() {
        return queryVehicleInfo;
    }

    public void setQueryVehicleInfo(Boolean queryVehicleInfo) {
        this.queryVehicleInfo = queryVehicleInfo;
    }

    public Boolean getQueryTallyingArea() {
        return queryTallyingArea;
    }

    public void setQueryTallyingArea(Boolean queryTallyingArea) {
        this.queryTallyingArea = queryTallyingArea;
    }

    public Boolean getQueryAttachment() {
        return queryAttachment;
    }

    public void setQueryAttachment(Boolean queryAttachment) {
        this.queryAttachment = queryAttachment;
    }
}
