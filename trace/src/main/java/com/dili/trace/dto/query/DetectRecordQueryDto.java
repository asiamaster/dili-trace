package com.dili.trace.dto.query;

import com.dili.ss.domain.annotation.Operator;
import com.dili.trace.domain.DetectRecord;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import java.util.List;

public class DetectRecordQueryDto extends DetectRecord {
    @ApiModelProperty(value = "IN ID")
    @Column(name = "`detect_request_id`")
    @Operator(Operator.IN)
    private List<Long> detectRequestIdList;

    public List<Long> getDetectRequestIdList() {
        return detectRequestIdList;
    }

    public void setDetectRequestIdList(List<Long> detectRequestIdList) {
        this.detectRequestIdList = detectRequestIdList;
    }
}
