package com.rainbowforest.apigateway.config;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletResponse;

@Component
public class FinalCorsFilter extends ZuulFilter {
    @Override
    public String filterType() {
        return "post";
    } // Chạy ở bước cuối cùng trước khi trả về React

    @Override
    public int filterOrder() {
        return 1000;
    } // Thứ tự ưu tiên cao nhất (chạy sau cùng)

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletResponse response = ctx.getResponse();

        // --- CHIÊU ĐỘC: DÙNG SET ĐỂ GHI ĐÈ, XÓA SẠCH DẤU * CỦA SERVICE CON ---
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");

        return null;
    }
}