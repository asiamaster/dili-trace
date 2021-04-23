package com.dili.trace.dto.ret;

import com.dili.trace.domain.DefaultFieldDetail;
import com.dili.trace.domain.FieldConfigDetail;

public class FieldConfigDetailRetDto extends FieldConfigDetail {
    private DefaultFieldDetail defaultFieldDetail;

    public DefaultFieldDetail getDefaultFieldDetail() {
        return defaultFieldDetail;
    }

    public void setDefaultFieldDetail(DefaultFieldDetail defaultFieldDetail) {
        this.defaultFieldDetail = defaultFieldDetail;
    }
}
