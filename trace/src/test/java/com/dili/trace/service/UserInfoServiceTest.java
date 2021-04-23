package com.dili.trace.service;

import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.domain.UserInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.Optional;

@EnableDiscoveryClient
public class UserInfoServiceTest extends AutoWiredBaseTest {
    @Autowired
    UserInfoService userInfoService;

    @Test
    public void saveUserInfo() {
        Long userId = 1L;
        Long marketId = 8L;
        Optional<UserInfo> userOpt = this.userInfoService.saveUserInfo(userId, marketId);
        Assertions.assertTrue(userOpt.isPresent());
        UserInfo userInfo = userOpt.get();
        Assertions.assertNotNull(userInfo);
        Assertions.assertEquals(userInfo.getLastSyncSuccess(), YesOrNoEnum.NO.getCode());


    }
}
