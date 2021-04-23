package com.dili.trace.dto;

import java.util.List;

import com.dili.ss.domain.BaseDomain;
import com.dili.ss.dto.IBaseDomain;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.SeparateSalesRecord;
import com.dili.trace.domain.QualityTraceTradeBill;

public class QualityTraceTradeBillOutDto extends BaseDomain {

    private RegisterBill registerBill;


    private List<SeparateSalesRecord> separateSalesRecords;


    private QualityTraceTradeBill qualityTraceTradeBill;

    public RegisterBill getRegisterBill() {
        return registerBill;
    }

    public void setRegisterBill(RegisterBill registerBill) {
        this.registerBill = registerBill;
    }

    public List<SeparateSalesRecord> getSeparateSalesRecords() {
        return separateSalesRecords;
    }

    public void setSeparateSalesRecords(List<SeparateSalesRecord> separateSalesRecords) {
        this.separateSalesRecords = separateSalesRecords;
    }

    public QualityTraceTradeBill getQualityTraceTradeBill() {
        return qualityTraceTradeBill;
    }

    public void setQualityTraceTradeBill(QualityTraceTradeBill qualityTraceTradeBill) {
        this.qualityTraceTradeBill = qualityTraceTradeBill;
    }
}
