package com.dili.trace.service;

import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.enums.FieldConfigModuleTypeEnum;
import com.dili.trace.enums.ImageCertTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

@EnableDiscoveryClient
public class EnumServiceTest extends AutoWiredBaseTest {
    @Autowired
    EnumService enumService;

    @Test
    public void listImageCertType() {
        List<ImageCertTypeEnum> list = this.enumService.listImageCertType(8L, FieldConfigModuleTypeEnum.REGISTER);
        Assertions.assertNotNull(list);

    }

    @Test
    public void selectAvailableValueList() {
        List<ImageCertTypeEnum> list = ReflectionTestUtils.invokeMethod(this.enumService, "selectAvailableValueList", new Object[]{8L, FieldConfigModuleTypeEnum.REGISTER, "truckType"});
        Assertions.assertNotNull(list);

    }

}
