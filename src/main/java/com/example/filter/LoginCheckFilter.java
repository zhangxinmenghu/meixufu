package com.example.filter;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.example.common.BaseContext;
import com.example.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//检查是否登录
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径比较，支持通配符
    public static  final AntPathMatcher PATH_MATCHER = new AntPathMatcher();


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //获取本次请求的url
        String requestURI = request.getRequestURI();
        log.info(requestURI);
        //定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                 "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",//发送验证码的请求路径
                "/user/login"//用户登录的路径
        };
        //判断本次请求是否需要处理
        boolean check = check(urls, requestURI);
        if (check){
            //放行
            filterChain.doFilter(request,response);
            return;
        }else {
            //判断是否登录
            // 已登录放行
            if (request.getSession().getAttribute("employee") != null){
                log.info("用户已登录,用户ID为：{}",request.getSession().getAttribute("employee"));

                Long empId = (Long) request.getSession().getAttribute("employee");
                BaseContext.setCurrentId(empId);
                filterChain.doFilter(request,response);
                return;
            }else {
                //判断是否登录
                // 已登录放行
                if (request.getSession().getAttribute("user") != null){
                    log.info("用户已登录,用户ID为：{}",request.getSession().getAttribute("user"));

                    //通过ThreadLocal获取Session中的id
                    Long userId = (Long) request.getSession().getAttribute("user");
                    BaseContext.setCurrentId(userId);
                    filterChain.doFilter(request,response);
                    return;

            }else {
                //未登录，通过输出流向客户端响应数据
                log.info("用户未登录");
                response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
                return;
            }
        }
    }
  }
    //检查本次请求是否需要放行
    private boolean check(String[] urls, String requestURI) {
        for (String url : urls){
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match){
                log.info("本次请求{}不需要处理",url);
                return true;
            }
        }
        return false;
    }
}
