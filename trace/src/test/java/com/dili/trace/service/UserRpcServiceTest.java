package com.dili.trace.service;

import com.dili.trace.AutoWiredBaseTest;
import com.dili.uap.sdk.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.List;

@EnableDiscoveryClient
public class UserRpcServiceTest extends AutoWiredBaseTest {
    @Autowired
    UserRpcService userRpcService;

    @Test
    public void findDetectDepartmentUsers() {
        List<User> userList = this.userRpcService.findDetectDepartmentUsers("一号", 8L);
        Assertions.assertNotNull(userList);
    }
}
