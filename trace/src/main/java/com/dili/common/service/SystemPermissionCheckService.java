package com.dili.common.service;

import com.dili.uap.sdk.redis.UserUrlRedis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName SystemPermissionCheckService
 * @Description: 系统权限校验业务类
 * @Author Tab.Xie
 * @Date 2020/10/16
 * @Version V1.0
 **/
@Service
public class SystemPermissionCheckService {
    @Autowired
    private UserUrlRedis userUrlRedis;

    /**
     * 微信小程序菜单权限前缀
     */
    private static final String WECHAT_MENUS_PREFIX = "wechat-";

    /**
     * TODO
     *
     * @param： userId:待校验用户id
     * url: 待检验url地址
     * @return：boolean
     * @author：Tab.Xie
     * @date：2020/10/16 13:43
     */
    public boolean checkUrl(Long userId, String url) {
        return userUrlRedis.checkUserMenuUrlRight(userId, url);
    }

    /**
     * 获取用户菜单权限
     * @param userId
     * @return
     */
    public Set<String> getWeChatUserMenus(Long userId) {
        Object object = userUrlRedis.getUserMenus(userId);
        Set<String> userMenus = castSet(object, String.class);
        if (!CollectionUtils.isEmpty(userMenus)) {
            return userMenus.stream().filter(m -> m.startsWith(WECHAT_MENUS_PREFIX)).collect(Collectors.toSet());
        } else {
            return new HashSet<>();
        }
    }

    /**
     * SB
     * @param <T>
     * @param obj
     * @param clazz
     * @return 
     */
    public static <T> Set<T> castSet(Object obj, Class<T> clazz)
    {
        Set<T> result = new HashSet<T>();
        if(obj instanceof Set<?>)
        {
            for (Object o : (Set<?>) obj)
            {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }

}