package com.qbit.framework.core.web.filter.content;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

/**
 * 请求流可重复读
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ContentCachingRequestFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 获取 Content-Type 判断是否为文件流类请求
        String contentType = request.getContentType();
        boolean isMultipart = contentType != null &&
                (contentType.startsWith("multipart/") ||
                        contentType.startsWith("application/octet-stream"));

        // 对文件流或已包装过的请求，直接放行
        if (isMultipart || WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class) != null) {
            filterChain.doFilter(request, response);
            return;
        }
        byte[] bytes = request.getInputStream().readAllBytes();
        // 包装请求，使其支持重复读取
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request, bytes);
        filterChain.doFilter(wrappedRequest, response);
    }
}

