package com.dili.trace.routing;

import com.dili.trace.annotations.TenantService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanCreationException;

import java.util.HashMap;
import java.util.Map;


/**
 * 代理类生成
 *
 * @author wangguofeng
 */
public class RoutingBeanProxyFactory {
    public static Object createProxy(Class targetClass, Map<String, Object> beans) {
        ProxyFactory proxyFactory = new ProxyFactory();
        if (targetClass.isInterface()) {
            proxyFactory.setInterfaces(targetClass);
        } else {
            proxyFactory.setTargetClass(targetClass);
        }
        proxyFactory.addAdvice(new VersionRoutingMethodInterceptor(beans));
        return proxyFactory.getProxy();
    }

    static class VersionRoutingMethodInterceptor implements MethodInterceptor {
        Map<String, Object> tenantBeans = new HashMap<>();

        public VersionRoutingMethodInterceptor(Map<String, Object> beans) {
            //拿到实现类上注解中配置的租户编码
            for (Map.Entry<String, Object> entry : beans.entrySet()) {
                Object obj = entry.getValue();
                String tenantCode = null;
                if (obj.getClass().isAnnotationPresent(TenantService.class)) {
                    tenantCode = obj.getClass().getAnnotation(TenantService.class).tanent();
                }
                if (tenantBeans.get(tenantCode) != null) {
                    throw new BeanCreationException(tenantBeans.get(tenantCode).getClass().getName() + " 和 " + obj.getClass().getName() + "有相同Tanent");
                }
                tenantBeans.put(tenantCode, obj);
            }
        }


        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            return invocation.getMethod().invoke(getTargetBean(), invocation.getArguments());
        }

        public Object getTargetBean() {
            Object obj = this.tenantBeans.get(getCurrentTeant());
            if (obj != null) {
                return obj;
            }
            return this.tenantBeans.get(null);
        }

        //设置租户ID
        private String getCurrentTeant() {

            if (RoutingContextHolder.get() == null) {
                return null;
            }
            return RoutingContextHolder.get();
        }
    }


}