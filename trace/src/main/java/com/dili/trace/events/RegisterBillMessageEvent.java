package com.dili.trace.events;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RegisterBillMessageEvent {
    /**
     * 新增
     */
    add("btn_add"),
    /**
     * 修改
     */
    edit("edit-btn"),
    /**
     * 进场审核
     */
    COPY("copy-btn"),
    DETAIL("detail-btn"),
    upload_origincertifiy("upload-origincertifiy-btn"),
    upload_handleresult("upload-handleresult-btn"),

    undo("undo-btn"),
    audit("audit-btn"),
    upload_detectreport("upload-detectreport-btn"),
    remove_reportAndcertifiy("remove-reportAndcertifiy-btn"),
    audit_withoutDetect("audit-withoutDetect-btn"),

    auto("auto-btn"),
    sampling("sampling-btn"),

    review("review-btn"),
    export("export"),

    batch_audit("batch-audit-btn"),
    batch_auto("batch-auto-btn"),
    batch_sampling("batch-sampling-btn"),
    batch_undo("batch-undo-btn"),
    createsheet("createsheet-btn"),
    /**
     * 修改图片
     */
    updateImage("update-img-btn"),


    /**
     * 进门
     */
    checkin("btn_checkin");

    private RegisterBillMessageEvent(String code) {
        this.code = code;
    }

    private String code;

    @JsonValue
    public String getCode() {
        return code;
    }

}
