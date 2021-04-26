package com.dili.trace.dynamic.impl;

import com.dili.trace.annotations.TenantService;
import com.dili.trace.dynamic.TanentInterfaceDemo;
import com.dili.trace.dynamic.TanentServiceDemo;
import org.springframework.stereotype.Service;

/**
 * DEMO
 */
@Service
@TenantService("a")
public class TanentServiceDemoA extends TanentServiceDemo implements TanentInterfaceDemo {
    /**
     * DEMO
     */
    @Override
    public String demo() {
        return "demo-a";
    }
}
