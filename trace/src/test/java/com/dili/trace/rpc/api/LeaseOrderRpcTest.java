package com.dili.trace.rpc.api;

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
public class LeaseOrderRpcTest extends AutoWiredBaseTest {
    @Autowired
    LeaseOrderRpc leaseOrderRpc;

    @Test
    public void findLease() {
        AssetsParamsDto paramsDto = new AssetsParamsDto();
        paramsDto.setCustomerId(40628L);
        paramsDto.setMarketId(8L);
        BaseOutput<List<AssetsResultDto>> out = this.leaseOrderRpc.findLease(paramsDto);
        Assertions.assertNotNull(out);
    }
}
