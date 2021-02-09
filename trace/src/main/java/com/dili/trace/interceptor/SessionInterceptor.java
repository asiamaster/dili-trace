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
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.redis.UserRedis;
import com.dili.uap.sdk.redis.UserUrlRedis;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * 同步时间
     */
    private String syncUserTimeKey = "syncUserTime_";
    /**
     * 用户过期时间-分钟
     */
    private Integer userEffectMin = 10;

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
//        System.out.println(req.getHeaderNames());
        String userToken = req.getHeader("UAP_Token");
        if (StringUtils.isBlank(userToken)) {
            return Optional.empty();
        }

        UserTicket ut = this.userRedis.getTokenUser(userToken);
        if (ut == null) {
            return Optional.empty();
        }
//        String url = "app_auth";
//        if (!this.userUrlRedis.checkUserMenuUrlRight(ut.getId(), url)) {
//            return Optional.empty();
//        }
        SessionData sessionData = SessionData.fromUserTicket(ut);
        //asyncRpcUser(Optional.ofNullable(sessionData));
        return Optional.ofNullable(sessionData);
    }

    private Optional<SessionData> loginAsClient(HttpServletRequest req) {
        Optional<SessionData> data = this.customerRpcService.getCurrentCustomer();
        //asyncRpcUser(data);
        this.sync(data);
        return data;
    }

    private Optional<SessionData> loginAsAny(HttpServletRequest req) {
        String userToken = req.getHeader("UAP_Token");
        String userId = req.getHeader("userId");
        if (StringUtils.isNotBlank(userToken)) {
            return this.loginAsManager(req);
        } else if (StringUtils.isNotBlank(userId)) {
            return this.loginAsClient(req);
        } else {
            return Optional.empty();
        }
    }

    /**
     * 同步用户信息
     *
     * @param customer
     */
    public void asyncRpcUser(Optional<SessionData> customer) {
        if (null == customer) {
            return;
        }
        try {
            //取用户id
            SessionData sessionData = customer.get();
            if (null == sessionData) {
                return;
            }
            Long userId = sessionData.getUserId();
            if (null == userId) {
                return;
            }
            String key_user = syncUserTimeKey + userId;
            //没有对应用户key，则同步该用户信息并写入到redis，key:user_id,val:过期时间
            if (!redisUtil.exists(key_user)) {
                syncUserInfoAdd(userId, key_user);
            } else {
                syncUserInfoUpdate(userId, key_user);
            }
        } catch (Exception e) {
            logger.error("===>>同步用户失败");
            logger.error(e.getMessage());
        }
    }

    /**
     * 新增用戶对应redis同步时间
     *
     * @param userId
     * @param key_user
     */
    private void syncUserInfoAdd(Long userId, String key_user) {
        doSyncUserRpc(userId);
        Date newMinutes = DateUtils.addMinutes(DateUtils.getCurrentDate(), userEffectMin);
        redisUtil.set(key_user, newMinutes);
    }

    /**
     * 更新用戶对应redis同步时间
     *
     * @param userId
     * @param key_user
     */
    private void syncUserInfoUpdate(Long userId, String key_user) {
        //redis中同步过期时间
        Date syncUserTime = (Date) redisUtil.get(key_user);
        //当前时间
        Date currentDate = DateUtils.getCurrentDate();
        //redis中没有过期时间,重新设置
        if (null == syncUserTime) {
            syncUserInfoAdd(userId, key_user);
        } else {
            //当前时间在同步过期时间之后，则调用同步方法同步用户
            if (currentDate.after(syncUserTime)) {
                doSyncUserRpc(userId);
                //秒转分
                Date newSyncDate = DateUtils.addMinutes(syncUserTime, userEffectMin);
                //同步过期时间+10分钟仍然在当前时间之前，则取当前时间+10分钟作新的同步过期时间
                if (currentDate.after(newSyncDate)) {
                    newSyncDate = DateUtils.addMinutes(DateUtils.getCurrentDate(), userEffectMin);
                }
                redisUtil.set(key_user, newSyncDate);
            }
        }

    }

    /**
     * 同步用户
     */
    public void doSyncUserRpc(Long userId) {
//        try {
//            syncRpcService.syncRpcUserByUserId(userId);
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        }
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
