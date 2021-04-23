package com.dili.trace.service;

import com.dili.trace.domain.DefaultFieldDetail;
import com.dili.trace.enums.FieldConfigModuleTypeEnum;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字段配置详情
 */
@Service
public class DefaultFieldDetailService extends TraceBaseService<DefaultFieldDetail, Long> {
    /**
     * 查询详情
     *
     * @param moduleTypeEnum
     * @return
     */
    public List<DefaultFieldDetail> findByModuleType(FieldConfigModuleTypeEnum moduleTypeEnum) {
        if (moduleTypeEnum == null) {
            return Lists.newArrayList();
        }
        DefaultFieldDetail q = new DefaultFieldDetail();
        q.setModuleType(moduleTypeEnum.getCode());
        return this.listByExample(q);
    }

}
