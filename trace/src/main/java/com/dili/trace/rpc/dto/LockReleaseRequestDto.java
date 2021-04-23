package com.dili.trace.rpc.dto;

import java.util.List;

/**
 * 锁定库存和释放库存请求dto
 */
public class LockReleaseRequestDto {

    //市场ID
    private Long firmId;
    //市场名称
    private String firmName;
    //锁定和释放项列表
    private List<LockReleaseItem> items;

    public LockReleaseRequestDto() {}

    public LockReleaseRequestDto(Long firmId, String firmName, List<LockReleaseItem> items) {
        this.firmId = firmId;
        this.firmName = firmName;
        this.items = items;
    }

    public Long getFirmId() {
        return firmId;
    }

    public void setFirmId(Long firmId) {
        this.firmId = firmId;
    }

    public String getFirmName() {
        return firmName;
    }

    public void setFirmName(String firmName) {
        this.firmName = firmName;
    }

    public List<LockReleaseItem> getItems() {
        return items;
    }

    public void setItems(List<LockReleaseItem> items) {
        this.items = items;
    }
}
