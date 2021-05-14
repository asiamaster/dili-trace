package com.dili.trace.rpc.api;

import com.dili.ss.domain.BaseOutput;
import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.rpc.dto.AccountGetListQueryDto;
import com.dili.trace.rpc.dto.AccountGetListResultDto;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.List;

@EnableDiscoveryClient
public class AccountRpcTest extends AutoWiredBaseTest {
    @Autowired
    AccountRpc accountRpc;

    @Test
    public void getList() {
        AccountGetListQueryDto q = new AccountGetListQueryDto();
        q.setFirmId(8L);
        q.setKeyword("8888");
        BaseOutput<List<AccountGetListResultDto>> list = this.accountRpc.getList(q);
        System.out.println(list);
    }

    @Test
    public void getList2() {
        AccountGetListQueryDto q = new AccountGetListQueryDto();
        q.setFirmId(8L);
        q.setCustomerIds(Lists.newArrayList(218L));
        BaseOutput<List<AccountGetListResultDto>> list = this.accountRpc.getList(q);
        System.out.println(list);
    }
}
