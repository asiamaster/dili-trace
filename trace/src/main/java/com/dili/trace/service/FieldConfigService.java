package com.dili.trace.service;

import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.trace.dao.FieldConfigMapper;
import com.dili.trace.domain.FieldConfig;
import com.dili.trace.enums.FieldConfigModuleTypeEnum;
import one.util.streamex.StreamEx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 字段配置信息
 */
@Service
public class FieldConfigService extends TraceBaseService<FieldConfig, Long> {
    @Autowired
    FieldConfigMapper fieldConfigMapper;

    /**
     * 查询字段配置信息
     *
     * @param marketId
     * @param moduleTypeEnum
     * @return
     */
    public Optional<FieldConfig> findByMarketIdAndModuleType(Long marketId, FieldConfigModuleTypeEnum moduleTypeEnum) {
        if (marketId == null || moduleTypeEnum == null) {
            return Optional.empty();
        }
        FieldConfig q = new FieldConfig();
        q.setMarketId(marketId);
        q.setModuleType(moduleTypeEnum.getCode());
        return StreamEx.of(this.listByExample(q)).findFirst();
    }

    /**
     * 保存或查询数据
     *
     * @param marketId
     * @param moduleType
     * @return
     */
    public FieldConfig saveOrFind(Long marketId, Integer moduleType) {
        if (marketId == null || moduleType == null) {
            throw new TraceBizException("参数错误");
        }
        FieldConfig domain = new FieldConfig();
        domain.setMarketId(marketId);
        domain.setModuleType(moduleType);
        this.fieldConfigMapper.insertOrIgnoreFieldConfig(domain);
        return StreamEx.of(this.listByExample(domain)).findFirst().orElseThrow(() -> {
            return new TraceBizException("创建字段配置错误");
        });
    }
}
