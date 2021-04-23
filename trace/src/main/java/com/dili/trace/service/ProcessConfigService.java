package com.dili.trace.service;

import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.trace.domain.ProcessConfig;
import one.util.streamex.StreamEx;
import org.springframework.stereotype.Service;

/**
 * 流程配置参数
 */
@Service
public class ProcessConfigService extends BaseServiceImpl<ProcessConfig, Long> {

    /**
     * 查询 配置
     *
     * @param marketId
     * @return
     */
    public ProcessConfig findByMarketId(Long marketId) {

        ProcessConfig processConfig = new ProcessConfig();
        processConfig.setIsAutoVerifyPassed(YesOrNoEnum.NO.getCode());
        processConfig.setCanDoCheckInWithoutWeight(YesOrNoEnum.YES.getCode());
        processConfig.setIsManullyCheckIn(YesOrNoEnum.YES.getCode());
        processConfig.setMarketId(marketId);

        ProcessConfig pcq = new ProcessConfig();
        pcq.setMarketId(marketId);
        ProcessConfig item = StreamEx.of(this.listByExample(pcq)).findFirst().orElse(processConfig);

        return item;
    }


}
