package com.finflow.document.interceptor;

import com.finflow.document.context.IdentityContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class IdentityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String email = request.getHeader("X-User-Email");
        String role = request.getHeader("X-User-Role");

        IdentityContext.set(email, role);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        IdentityContext.clear();
    }
}