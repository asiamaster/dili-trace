package com.dili.trace.rpc.dto;

/**
 * 锁定库存和释放库存项实体类
 */
public class LockReleaseItem {

    //数量
    private Float amount;
    //库存ID
    private Long stockId;
    //是否锁定 1 是 0 否
    private Integer frozen;

    public LockReleaseItem() {}

    public LockReleaseItem(Float amount, Long stockId, Integer frozen) {
        this.amount = amount;
        this.stockId = stockId;
        this.frozen = frozen;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public Integer getFrozen() {
        return frozen;
    }

    public void setFrozen(Integer frozen) {
        this.frozen = frozen;
    }
}
