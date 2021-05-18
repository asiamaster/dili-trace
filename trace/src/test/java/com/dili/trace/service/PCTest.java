package com.dili.trace.service;

import com.dili.ss.util.SpringUtil;
import com.dili.uap.sdk.config.ManageConfig;
import com.dili.uap.sdk.session.PermissionContext;
import mockit.Mock;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

public class PCTest {
    @Test
    public void getAccessToken() {

        ReflectionTestUtils.setField(SpringUtil.class, "environment", new MockEnvironment());
        HttpServletResponse resp = new MockHttpServletResponse();
        HttpServletRequest req = new MockHttpServletRequest();
        PermissionContext permissionContext = new PermissionContext(req, resp, null, new ManageConfig(), "");
        String accessToken = permissionContext.getAccessToken();
        String refreshToken = permissionContext.getRefreshToken();
    }

    @Test
    public void optTest() {

        Optional<String> opt = Optional.of("aaa").map(s -> {
            String ss = null;
            System.out.println("ddd");
            return Optional.ofNullable(ss).orElse("fff");

        });
        System.out.println(opt.isPresent());


    }
}
