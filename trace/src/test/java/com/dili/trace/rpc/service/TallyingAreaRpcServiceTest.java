package com.dili.trace.rpc.service;

import com.dili.customer.sdk.domain.TallyingArea;
import com.dili.customer.sdk.domain.dto.CustomerSimpleExtendDto;
import com.dili.ss.domain.PageOutput;
import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.rpc.dto.CustomerQueryDto;
import one.util.streamex.StreamEx;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.List;
import java.util.Map;
import java.util.Set;

@EnableDiscoveryClient
public class TallyingAreaRpcServiceTest extends AutoWiredBaseTest {
    @Autowired
    CustomerRpcService customerRpcService;
    @Autowired
    TallyingAreaRpcService tallyingAreaRpcService;

    @Test
    public void findTallyingAreaByMarketIdAndCustomerIdList() {
        CustomerQueryDto q = new CustomerQueryDto();
        q.setMarketId(8L);
        q.setKeyword("啊");
        q.setPage(1);
        q.setRows(20);
        PageOutput<List<CustomerSimpleExtendDto>> pg = this.customerRpcService.queryCustomerByCustomerQueryDto(q);
        Assertions.assertNotNull(pg);

        List<Long> customerIdSet = StreamEx.of(pg.getData()).map(CustomerSimpleExtendDto::getId).toList();
        Assertions.assertNotNull(customerIdSet);

        Map<Long, List<TallyingArea>> map = this.tallyingAreaRpcService.findTallyingAreaByMarketIdAndCustomerIdList(q.getMarketId(), customerIdSet);
        Assertions.assertNotNull(map);

    }

}
