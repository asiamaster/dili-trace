package com.dili.trace.service;

import com.dili.trace.AutoWiredBaseTest;
import com.dili.uap.sdk.domain.Department;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.Optional;

@EnableDiscoveryClient
public class DepartmentRpcServiceTest extends AutoWiredBaseTest {
    @Autowired
    DepartmentRpcService departmentRpcService;

    @Test
    public void findDetectDepartment() {
        Optional<Department> opt = this.departmentRpcService.findDetectDepartment(8L);
        Assertions.assertNotNull(opt);
    }

}
