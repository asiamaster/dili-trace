package com.dili.trace.dao;

import com.dili.ss.base.MyMapper;
import com.dili.trace.domain.FieldConfig;

public interface FieldConfigMapper extends MyMapper<FieldConfig> {
    public int insertOrIgnoreFieldConfig(FieldConfig config);
}
