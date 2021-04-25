package com.dili.trace.tenant.service.impl;

import com.dili.trace.annotations.TenantService;
import com.dili.trace.tenant.service.TenantAfterBillCreationService;
import org.springframework.stereotype.Service;

/**
 * 多租业务实现(用于不同配置实现业务)
 */
@Service
@TenantService("abc")
public class TenantAfterBillCreationServiceImpl extends TenantAfterBillCreationService {

    @Override
    public void afterCreate() {

    }

}
