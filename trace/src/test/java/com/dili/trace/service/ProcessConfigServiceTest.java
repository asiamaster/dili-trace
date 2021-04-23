package com.dili.trace.service;

import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.domain.ProcessConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
public class ProcessConfigServiceTest extends AutoWiredBaseTest {
    @SpyBean
    ProcessConfigService processConfigService;

    @Test
    public void findByMarketId() {
        ProcessConfig processConfig = new ProcessConfig();
        processConfig.setCanDoCheckInWithoutWeight(YesOrNoEnum.NO.getCode());
        processConfig.setIsManullyCheckIn(YesOrNoEnum.NO.getCode());
        processConfig.setIsAutoVerifyPassed(YesOrNoEnum.NO.getCode());
        processConfig.setMarketId(8L);
        Mockito.doReturn(processConfig).when(this.processConfigService).findByMarketId(Mockito.anyLong());

        ProcessConfig pc = this.processConfigService.findByMarketId(1L);
        Assertions.assertNotNull(pc);
    }
}
