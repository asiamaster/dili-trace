package com.dili.trace.api.client;

import com.dili.trace.AutoWiredBaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
public class ClientDetectRequestApiTest extends AutoWiredBaseTest {
    @Autowired
    ClientDetectRequestApi clientDetectRequestApi;

    @Test
    public void getDetectRequestDetail() {
        String str = this.clientDetectRequestApi.getDetectRequestDetail(448L);
        System.out.println(str);
    }
}
