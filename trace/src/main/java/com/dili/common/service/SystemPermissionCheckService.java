package com.dili.common.service;

import com.dili.uap.sdk.service.redis.UserUrlRedis;
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