package com.dili.trace.service;

import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.domain.UserQrHistory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.math.BigDecimal;
import java.util.Optional;

@EnableDiscoveryClient
public class UserQrHistoryServiceTest extends AutoWiredBaseTest {
    @Autowired
    UserQrHistoryService userQrHistoryService;
    @Autowired
    UserInfoService userInfoService;

    @Test
    public void createUserQrHistoryForVerifyBill() {
        Long billId = super.baseCreateRegisterBill(8L, 1L, BigDecimal.valueOf(100));
        Optional<UserQrHistory> opt = this.userQrHistoryService.createUserQrHistoryForVerifyBill(billId);
        Assertions.assertTrue(opt.isPresent());
        UserQrHistory qh = opt.get();
        System.out.println(qh);

        UserInfo userInfo = this.userInfoService.get(qh.getUserInfoId());
        Assertions.assertNotNull(userInfo);
        Assertions.assertEquals(userInfo.getQrHistoryId(), qh.getId());

        Assertions.assertEquals(userInfo.getQrStatus(), qh.getQrStatus());

    }

}
