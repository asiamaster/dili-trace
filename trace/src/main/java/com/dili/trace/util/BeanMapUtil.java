package com.dili.trace.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.cglib.beans.BeanMap;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean and Map convert util
 */
public class BeanMapUtil {

    /**
     * 将对象装换为map
     * 
     * @param bean
     * @return
     */
    public static <T> Map<String, Object> beanToMap(T bean) {
        Map<String, Object> map = Maps.newHashMap();
        if (bean != null) {
            BeanMap beanMap = BeanMap.create(bean);
            for (Object key : beanMap.keySet()) {
                map.put(key + "", beanMap.get(key));
            }
        }
        return map;
    }

    public static <T> T trimBean(T bean) {
        Map<String, Object> map = Maps.newHashMap();
        if (bean != null) {
            BeanMap beanMap = BeanMap.create(bean);
            for (Object key : beanMap.keySet()) {
                Object value = beanMap.get(key);
                if (value != null) {
                    if (value instanceof String) {
                        String v = (String) value;
                        if (v.trim().length() != 0) {
                            map.put(key + "", v.trim());
                        }else{
                            map.put(key + "", null);
                        }
                    } else {
                        map.put(key + "", value);
                    }

                } else {
                    map.put(key + "", value);
                }

            }
        }
        try {
            org.apache.commons.beanutils.BeanUtils.copyProperties(bean, map);
        } catch (IllegalAccessException | InvocationTargetException e) {
        }
        return bean;
    }

    /**
     * 将map装换为javabean对象
     * 
     * @param map
     * @param bean
     * @return
     */
    public static <T> T mapToBean(Map<String, Object> map, T bean) {
        BeanMap beanMap = BeanMap.create(bean);
        beanMap.putAll(map);
        return bean;
    }

    /**
     * 将List<T>转换为List<Map<String, Object>>
     * 
     * @param objList
     * @return
     */
    public static <T> List<Map<String, Object>> objectsToMaps(List<T> objList) {
        List<Map<String, Object>> list = Lists.newArrayList();
        if (objList != null && objList.size() > 0) {
            Map<String, Object> map = null;
            T bean = null;
            for (int i = 0, size = objList.size(); i < size; i++) {
                bean = objList.get(i);
                map = beanToMap(bean);
                list.add(map);
            }
        }
        return list;
    }

    /**
     * 将List<Map<String,Object>>转换为List<T>
     * 
     * @param maps
     * @param clazz
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <T> List<T> mapsToObjects(List<Map<String, Object>> maps, Class<T> clazz)
            throws InstantiationException, IllegalAccessException {
        List<T> list = Lists.newArrayList();
        if (maps != null && maps.size() > 0) {
            Map<String, Object> map = null;
            T bean = null;
            for (int i = 0, size = maps.size(); i < size; i++) {
                map = maps.get(i);
                bean = clazz.newInstance();
                mapToBean(map, bean);
                list.add(bean);
            }
        }
        return list;
    }

    /**
     * 将List<Map<String,String>>转换为Map<String,String>
     * 
     * @param list
     * @param key
     * @param value
     * @return
     */
    public static Map<String, String> listToMap(List<Map<String, String>> list, String key, String value) {
        Map<String, String> map = new HashMap<>();
        for (Map<String, String> temp : list) {
            map.put(temp.get(key), temp.get(value));
        }
        return map;
    }
}
