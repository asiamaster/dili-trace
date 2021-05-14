package com.dili.trace.rpc.api;

import com.dili.customer.sdk.domain.CharacterType;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.customer.sdk.domain.query.CustomerQueryInput;
import com.dili.customer.sdk.enums.CustomerEnum;
import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.ss.domain.PageOutput;
import com.dili.trace.AutoWiredBaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.List;

@EnableDiscoveryClient
public class CustomerRpcTest extends AutoWiredBaseTest {
    @Autowired
    CustomerRpc customerRpc;

    @Test
    public void listPage() {

        CustomerQueryInput q = new CustomerQueryInput();
        q.setKeyword("买七");
        q.setPage(1);
        q.setRows(10);
        CharacterType seller = new CharacterType();
        seller.setCharacterType(CustomerEnum.CharacterType.经营户.getCode());
        q.setCharacterType(seller);
        q.setMarketId(8L);
        PageOutput<List<CustomerExtendDto>> out = this.customerRpc.listPage(q);
        System.out.println(out);

    }
}
