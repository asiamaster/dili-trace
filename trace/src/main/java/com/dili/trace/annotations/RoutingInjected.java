package com.dili.trace.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * description
 *
 * @author wangguofeng
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RoutingInjected {
}