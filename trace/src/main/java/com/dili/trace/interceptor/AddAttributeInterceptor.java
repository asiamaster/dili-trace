package com.dili.trace.interceptor;

import com.dili.common.config.BuildConfiguration;
import com.dili.trace.service.GlobalVarService;
import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author xuliang
 */
public class AddAttributeInterceptor extends HandlerInterceptorAdapter {
    private static final Logger logger = LoggerFactory.getLogger(AddAttributeInterceptor.class);

    @Autowired
    private Environment environment;
    @Autowired
    private BuildConfiguration buildConfiguration;
    @Autowired
    GlobalVarService globalVarService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        request.setAttribute("imageViewPathPrefix", this.globalVarService.getDfsImageViewPathPrefix());
        request.setAttribute("baseWebPath", this.globalVarService.getBaseWebPath());
        request.setAttribute("v", this.getVersion());
        return true;
    }

    private String getVersion() {

        List<String> activeProfiles = StreamEx.of(this.environment.getActiveProfiles()).nonNull().toList();
        if (activeProfiles.contains("dev") || activeProfiles.contains("test")) {
            this.buildConfiguration.setDate(String.valueOf(System.currentTimeMillis()));
        } else {
            if ("${pom.date}".equalsIgnoreCase(this.buildConfiguration.getDate())) {
                this.buildConfiguration.setDate(String.valueOf(System.currentTimeMillis()));
            }
        }
        return this.buildConfiguration.getDate();
    }

}
