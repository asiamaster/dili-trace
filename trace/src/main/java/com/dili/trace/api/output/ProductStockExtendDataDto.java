package com.dili.trace.api.output;

import java.math.BigDecimal;

public class ProductStockExtendDataDto {

    /**
     * 检测不合格重量
     */
    private BigDecimal detectFailedWeight=BigDecimal.ZERO;
    /**
     * 购买中重量
     */
    private BigDecimal buyingWeight=BigDecimal.ZERO;
    /**
     * 下架重量
     */
    private BigDecimal pushDownWeight=BigDecimal.ZERO;

    public BigDecimal getDetectFailedWeight() {
        return detectFailedWeight;
    }

    public void setDetectFailedWeight(BigDecimal detectFailedWeight) {
        this.detectFailedWeight = detectFailedWeight;
    }

    public BigDecimal getBuyingWeight() {
        return buyingWeight;
    }

    public void setBuyingWeight(BigDecimal buyingWeight) {
        this.buyingWeight = buyingWeight;
    }

    public BigDecimal getPushDownWeight() {
        return pushDownWeight;
    }

    public void setPushDownWeight(BigDecimal pushDownWeight) {
        this.pushDownWeight = pushDownWeight;
    }
}
