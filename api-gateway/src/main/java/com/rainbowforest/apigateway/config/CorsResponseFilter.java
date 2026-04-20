package com.rainbowforest.apigateway.config;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import java.util.List;
import com.netflix.util.Pair;

@Component
public class CorsResponseFilter extends ZuulFilter {
    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    } // Chạy sau khi nhận data từ Backend

    @Override
    public int filterOrder() {
        return 999;
    } // Chốt hạ cuối cùng

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        List<Pair<String, String>> headers = ctx.getZuulResponseHeaders();

        // 🔥 XÓA TẤT CẢ các header CORS thừa từ Backend gửi lên
        if (headers != null) {
            headers.removeIf(p -> p.first().equalsIgnoreCase("Access-Control-Allow-Origin"));
            headers.removeIf(p -> p.first().equalsIgnoreCase("Access-Control-Allow-Credentials"));
            headers.removeIf(p -> p.first().equalsIgnoreCase("Access-Control-Allow-Methods"));
            headers.removeIf(p -> p.first().equalsIgnoreCase("Access-Control-Allow-Headers"));
        }
        return null;
    }
}