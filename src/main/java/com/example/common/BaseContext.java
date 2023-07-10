package com.example.common;


import org.springframework.stereotype.Component;

//基于threadlocal封装工具类，用户保存和获取当前登录用户id
@Component
public class BaseContext {
    private static ThreadLocal<Long>  threadLocal = new ThreadLocal<>();
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
