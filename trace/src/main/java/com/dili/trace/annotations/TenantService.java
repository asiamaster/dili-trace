package com.dili.trace.annotations;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * description
 *
 * @author jinliang 2019/01/11 11:30
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface TenantService {
    String value() default "0";
}