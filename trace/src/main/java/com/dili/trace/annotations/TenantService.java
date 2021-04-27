package com.dili.trace.annotations;


import com.dili.trace.dynamic.TenantType;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * description
 *
 * @author wangguofeng
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface TenantService {
    String tanent();
}

