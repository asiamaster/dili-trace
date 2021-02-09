package com.dili.common.entity;

import com.dili.trace.service.MarketService;
import com.dili.uap.sdk.domain.Firm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 系统通用常量
 */
@Component
public class ExecutionConstants {

    //系统code
    public final static String SYSTEM_CODE = "trace";

}
