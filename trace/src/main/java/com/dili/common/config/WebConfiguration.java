package com.dili.common.config;

import com.dili.common.entity.LoginSessionContext;
import com.dili.trace.interceptor.AddAttributeInterceptor;
import com.dili.trace.interceptor.SessionInterceptor;
import com.dili.trace.interceptor.TanentInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public LoginSessionContext sessionContext() {
        return new LoginSessionContext();
    }

    @Bean
    public SessionInterceptor sessionInterceptor() {
        return new SessionInterceptor();
    }

    @Bean
    public AddAttributeInterceptor addAttributeInterceptor() {
        return new AddAttributeInterceptor();
    }

    @Bean
    public TanentInterceptor tanentInterceptor() {
        return new TanentInterceptor();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tanentInterceptor()).addPathPatterns("/**");
        registry.addInterceptor(sessionInterceptor()).addPathPatterns("/api/**");
        registry.addInterceptor(addAttributeInterceptor()).addPathPatterns("/**");

    }

}
