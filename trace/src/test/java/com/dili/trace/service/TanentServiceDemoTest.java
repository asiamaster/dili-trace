package com.dili.trace.service;

import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.annotations.RoutingInjected;
import com.dili.trace.dynamic.TanentInterfaceDemo;
import com.dili.trace.dynamic.TanentServiceDemo;
import com.dili.trace.routing.RoutingContextHolder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;

@EnableDiscoveryClient
public class TanentServiceDemoTest extends AutoWiredBaseTest {
    @Autowired
    ApplicationContext applicationContext;
    @RoutingInjected
    TanentServiceDemo tanentServiceDemo;

    @RoutingInjected
    TanentInterfaceDemo tanentInterfaceDemo;

    @BeforeAll
    public static void init() {

//        RoutingContextHolder.put("a");
    }

    @Test
    public void demo() {
        String str = this.tanentServiceDemo.demo();
        Assertions.assertEquals(str, "demo-0");
        Assertions.assertThrows(NullPointerException.class, () -> {
            this.tanentInterfaceDemo.demo();
        });
    }

    @Test
    public void demoa() {
        RoutingContextHolder.put("a");
        String str = this.tanentServiceDemo.demo();
        Assertions.assertEquals("demo-a", str);

        String str2 = this.tanentInterfaceDemo.demo();
        Assertions.assertEquals("demo-a", str2);
    }

    @Test
    public void demob() {
        RoutingContextHolder.put("b");
        String str = this.tanentServiceDemo.demo();
        Assertions.assertEquals("demo-b", str);


        String str2 = this.tanentInterfaceDemo.demo();
        Assertions.assertEquals("demo-b", str2);
    }


}
