package com.dili.trace.rpc.service;

import com.dili.ss.domain.BaseOutput;
import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.rpc.dto.AssetsParamsDto;
import com.dili.trace.rpc.dto.AssetsResultDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.List;

@EnableDiscoveryClient
public class LeaseOrderRpcServiceTest extends AutoWiredBaseTest {
    @Autowired
    LeaseOrderRpcService leaseOrderRpcService;

    @Test
    public void findLease() {
        AssetsParamsDto paramsDto = new AssetsParamsDto();
        paramsDto.setCustomerId(40556L);
        paramsDto.setMarketId(11L);
        paramsDto.setBizType(1);


        List<AssetsResultDto> list = this.leaseOrderRpcService.findLease(paramsDto);
        Assertions.assertNotNull(list);
    }
}
