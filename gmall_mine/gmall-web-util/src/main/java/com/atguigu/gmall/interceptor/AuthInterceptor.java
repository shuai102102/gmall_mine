package com.atguigu.gmall.interceptor;

import com.atguigu.gmall.annotation.LoginRequire;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpClientUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    // 重写拦截器preHandle方法
    //参数：request，response，Handle
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("mvc的方法在被请求之前，用户权限拦截器");

//        StringBuffer requestURL = request.getRequestURL();
        //判断被请求的方法是否需要拦截器
        HandlerMethod mh = (HandlerMethod) handler;
        LoginRequire methodAnnotation = mh.getMethodAnnotation(LoginRequire.class);

        if(methodAnnotation == null){
            //无需拦截器，直接过
            System.out.println("无需验证，直接过");

            return true;
        }

        String token = "";
        String oldToken = CookieUtil.getCookieValue(request,"userToken",true);
        String newToken = request.getParameter("newToken");
        //取出是否需要认证
        boolean neededSuccess = methodAnnotation.isNeededSuccess();
        //oldToken无 newToken无 从未登录
        //oldToken无 newToken有 新登录
        //oldToken有 newToken有 新登录
        //oldToken有 newToken无 以前登录

        if(StringUtils.isBlank(oldToken) && StringUtils.isBlank(newToken) && neededSuccess){
            //重定向到登录页面，带上请求地址
            response.sendRedirect("http://passport.gmall.com:8085/goToLogin?originUrl=" + request.getRequestURL());
            return false;
        }

        if((StringUtils.isBlank(oldToken) && StringUtils.isNotBlank(newToken)) || (StringUtils.isNotBlank(oldToken) && StringUtils.isNotBlank(newToken))){
            //新登录，放cookie，验证
            token = newToken;
            CookieUtil.setCookie(request,response,"userToken",newToken,60*30,true);
        }

        if(StringUtils.isNotBlank(oldToken) && StringUtils.isBlank(newToken)){
            //以前登录，验证
            token = oldToken;
        }

        String success = "";
        if(StringUtils.isNotBlank(token)){
            //验证一下token
           success =  HttpClientUtil.doGet("Http://passport.gmall.com:8085/verify?token="+token + "&currentIp=" + getIp(request));
        }

        if("success".equals(success)){
            //将用户信息放入request
            Map atguigu0228 = JwtUtil.decode("atguigu0228",token,getIp(request));
            request.setAttribute("userId",atguigu0228.get("userId").toString());
            request.setAttribute("nickName",atguigu0228.get("nickName").toString());
        }

        //购物车清单既需要验证，又需要不成功
        if(neededSuccess && !"success".equals(success)){
            CookieUtil.deleteCookie(request,response,"userToken");
            response.sendRedirect("http://passport.gmall.com:8085/goToLogin?originUrl="+request.getRequestURL());
            return false;
        }
        return true;
    }

    private String getIp(HttpServletRequest request) {

        String ip = "";
        ip = request.getHeader("x-forworded-for");

        if(StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();
        }

        if(StringUtils.isBlank(ip)){
            ip = "127.0.0.1";
        }
        return  ip;
    }
}
