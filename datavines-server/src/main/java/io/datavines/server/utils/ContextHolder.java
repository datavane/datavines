package io.datavines.server.utils;

import io.datavines.server.DataVinesConstants;
import io.datavines.server.coordinator.repository.entity.User;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ContextHolder {

    private static final ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<>();

    public static Long getUserId(){
        Object obj = getContext().get(DataVinesConstants.LOGIN_USER);
        User user = null == obj ? null : (User)obj;
        return null == user ? null : user.getId();
    }

    /**
     * 移除上下文信息
     */
    public static void removeAll(){
        Map<String, Object> context = getContext();
        if(null != context){
            context.clear();
        }
    }

    /**
     * 移除上下文信息
     */
    public static void removeByKey(String key){
        Map<String, Object> context = getContext();
        if(null != context){
            context.remove(key);
        }
    }

    /**
     * 获取当前线程上线文
     * @return
     */
    public static Map<String, Object> getContext(){
        Map<String, Object> currentContext = threadLocal.get();
        if(null == currentContext){
            Map<String, Object> concurrentHashMap = new ConcurrentHashMap<>();
            threadLocal.set(concurrentHashMap);
            return concurrentHashMap;
        }
        return currentContext;
    }

    /**
     * 设置参数
     * @param key
     * @param value
     */
    public static void setParam(String key, Object value){
        Map<String, Object> context = getContext();
        context.put(key, value);
    }

    /**
     * 获取参数
     * @param key
     * @return
     */
    public static Object getParam(String key){
        Map<String, Object> context = getContext();
        return context.get(key);
    }

    /**
     * 获取用户信息
     * @return
     */
    public static User getUser(){
        Object obj = getContext().get(DataVinesConstants.LOGIN_USER);
        return null == obj ? null : (User)obj;
    }



}
