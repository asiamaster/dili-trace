package com.dili.trace.interceptor;

import com.dili.trace.routing.RoutingContextHolder;
import com.dili.trace.service.ProcessConfigService;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.service.AuthService;
import com.dili.uap.sdk.util.WebContent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * @author wangguofeng
 */
public class TanentInterceptor extends HandlerInterceptorAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TanentInterceptor.class);

    @Autowired
    private ProcessConfigService processConfigService;
    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler)
            throws Exception {
        if (WebContent.getRequest() == null) {
            WebContent.put(req);
            WebContent.put(resp);
        }
        this.findMarketId(req).ifPresent(marketId -> {
            logger.info("marketId={}", marketId);
            RoutingContextHolder.put(String.valueOf(marketId));
        });

        return true;
    }

    private Optional<Long> findMarketId(HttpServletRequest req) {

        String marketIdStr = req.getHeader("marketId");

        String accessToken = req.getHeader("UAP_accessToken");
        String refreshToken = req.getHeader("UAP_refreshToken");
        if (StringUtils.isNotBlank(marketIdStr)) {
            try {
                return Optional.ofNullable(Long.parseLong(marketIdStr));
            } catch (Exception e) {
                //ignore exception
            }
        } else if (StringUtils.isNotBlank(accessToken) && StringUtils.isNotBlank(refreshToken)) {
            UserTicket ut = authService.getUserTicket(accessToken, refreshToken);
            if (ut != null) {
                return Optional.ofNullable(ut.getFirmId());
            }
        }
        return Optional.empty();

    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler,
                                @Nullable Exception ex) throws Exception {
        WebContent.resetLocal();
    }
}
