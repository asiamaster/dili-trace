package com.dili.trace.rpc.service;

import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.customer.sdk.domain.query.CustomerQueryInput;
import com.dili.ss.domain.PageOutput;
import com.dili.trace.AutoWiredBaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.List;

@EnableDiscoveryClient
public class CustomerRpcServiceTest extends AutoWiredBaseTest {
    @Autowired
    CustomerRpcService CustomerRpcService;

    @Test
    public void listSeller() {

        CustomerQueryInput query = new CustomerQueryInput();
        query.setPage(1);
        query.setRows(50);
        query.setKeyword("啊");
        Long marketId=8L;

        PageOutput<List<CustomerExtendDto>> out = this.customerRpcService.listSeller(query, marketId);
        Assertions.assertNotNull(out);
    }
}
