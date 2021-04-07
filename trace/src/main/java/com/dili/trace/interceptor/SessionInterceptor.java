package com.dili.trace.interceptor;

import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.common.exception.TraceBizException;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.redis.service.RedisUtil;
import com.dili.ss.util.DateUtils;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.service.SyncUserInfoService;
import com.dili.trace.service.UapRpcService;
import com.dili.uap.sdk.config.ManageConfig;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.service.AuthService;
import com.dili.uap.sdk.service.redis.UserRedis;
import com.dili.uap.sdk.service.redis.UserUrlRedis;
import com.dili.uap.sdk.session.PermissionContext;
import com.dili.uap.sdk.util.WebContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.Optional;

public class SessionInterceptor extends HandlerInterceptorAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SessionInterceptor.class);

    @Autowired
    private LoginSessionContext sessionContext;
    @Autowired
    UapRpcService uapRpcService;
    @Autowired
    CustomerRpcService customerRpcService;
    @Autowired
    UserRedis userRedis;
    @Autowired
    UserUrlRedis userUrlRedis;
    @Autowired
    SyncUserInfoService syncUserInfoService;
    @Resource
    RedisUtil redisUtil;
    @Autowired
    AuthService authService;
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * 用户过期时间-分钟
     */
    private Integer userEffectMin = 10;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) throws Exception {
        WebContent.resetLocal();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        AppAccess access = findAnnotation((HandlerMethod) handler,
                AppAccess.class);

        try {
            if (access == null) {
                return this.write401(response, "没有权限访问");
            }
            WebContent.put("req", request);
            WebContent.put("resp", response);
            Optional<SessionData> currentSessionData = Optional.empty();
            if (access.role() == Role.ANY) {
                currentSessionData = this.loginAsAny(request);
            } else if (access.role() == Role.Client) {
                currentSessionData = this.loginAsClient(request);
            } else if (access.role() == Role.Manager) {
                currentSessionData = this.loginAsManager(request);
            } else if (access.role() == Role.NONE) {
                logger.info("无需权限即可访问。比如检测机器");
                return true;
            } else {
                return this.writeError(response, "权限配置错误");
            }
            if (!currentSessionData.isPresent()) {
                return this.write401(response, "没有权限访问");
            }
            this.sessionContext.setSessionData(currentSessionData.get(), access);

        } catch (TraceBizException e) {
            return this.writeError(response, e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return this.writeError(response, "服务端出错");
        }

        this.sessionContext.getSessionData().setRole(access.role());

        return true;
    }

    private boolean writeError(HttpServletResponse resp, String msg) {
        try {
            BaseOutput out = BaseOutput.failure(msg);
            byte[] bytes = mapper.writeValueAsBytes(out);
            resp.getOutputStream().write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    private boolean write401(HttpServletResponse resp, String msg) {
        try {
            BaseOutput out = BaseOutput.failure("401", msg);
            byte[] bytes = mapper.writeValueAsBytes(out);
            resp.getOutputStream().write(bytes);
            resp.flushBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    private Optional<SessionData> loginAsManager(HttpServletRequest req) {

        String accessToken = req.getHeader("UAP_accessToken");
        String refreshToken = req.getHeader("UAP_refreshToken");
        if (StringUtils.isBlank(accessToken) && StringUtils.isBlank(refreshToken)) {
            return Optional.empty();
        }

        UserTicket ut = authService.getUserTicket(accessToken, refreshToken);
        if (ut == null) {
            return Optional.empty();
        }
        SessionData sessionData = SessionData.fromUserTicket(ut);
        return Optional.ofNullable(sessionData);
    }

    private Optional<SessionData> loginAsClient(HttpServletRequest req) {
        Optional<SessionData> data = this.customerRpcService.getCurrentCustomer();
        this.sync(data);
        return data;
    }

    private Optional<SessionData> loginAsAny(HttpServletRequest req) {
        String accessToken = req.getHeader("UAP_accessToken");
        String refreshToken = req.getHeader("UAP_refreshToken");
        String userId = req.getHeader("userId");
        if (StringUtils.isNotBlank(accessToken) || StringUtils.isNotBlank(refreshToken)) {
            return this.loginAsManager(req);
        } else if (StringUtils.isNotBlank(userId)) {
            return this.loginAsClient(req);
        } else {
            return Optional.empty();
        }
    }


    private void sync(Optional<SessionData> sessionData) {
        sessionData.ifPresent(sd -> {
            this.syncUserInfoService.saveAndSyncUserInfo(sd.getUserId(), sd.getMarketId());
        });
    }

    private <T extends Annotation> T findAnnotation(HandlerMethod handler, Class<T> annotationType) {
        T annotation = handler.getMethodAnnotation(annotationType);
        if (annotation != null)
            return annotation;
        return handler.getBeanType().getAnnotation(annotationType);
    }

}
