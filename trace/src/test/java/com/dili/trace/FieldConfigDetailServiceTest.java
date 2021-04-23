package com.dili.trace;

import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.trace.domain.FieldConfig;
import com.dili.trace.domain.FieldConfigDetail;
import com.dili.trace.service.FieldConfigDetailService;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.List;

@EnableDiscoveryClient
public class FieldConfigDetailServiceTest extends AutoWiredBaseTest {
    @Autowired
    FieldConfigDetailService fieldConfigDetailService;

    @Test
    public void saveOrUpdateFieldConfig() {

        FieldConfig fieldConfigInput = new FieldConfig();
        fieldConfigInput.setMarketId(8L);
        fieldConfigInput.setModuleType(1);


        FieldConfigDetail fcd = new FieldConfigDetail();
        fcd.setDefaultId(1L);
        fcd.setDisplayed(YesOrNoEnum.YES.getCode());
        fcd.setRequired(YesOrNoEnum.YES.getCode());

        List<FieldConfigDetail> fieldConfigDetailListInput = Lists.newArrayList(fcd);

        int v = this.fieldConfigDetailService.saveOrUpdateFieldConfig(fieldConfigInput, fieldConfigDetailListInput);
        Assertions.assertTrue(v != 0);
    }
}
