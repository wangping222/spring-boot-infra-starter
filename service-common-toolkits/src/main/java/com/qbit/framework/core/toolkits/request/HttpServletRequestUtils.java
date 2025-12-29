package com.qbit.framework.core.toolkits.request;


import com.qbit.framework.core.toolkits.AssertUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 从上下文中获取 http servlet request
 * @author Qbit Framework
 *
 **/
@Slf4j
public final class HttpServletRequestUtils {

    private static final String NOT_CURRENTLY_IN_WEB_SERVLET_CONTEXT = "not currently in web servlet context";

    private HttpServletRequestUtils() {
        throw new AssertionError();
    }

    @NotNull
    public static HttpServletRequest requireContextRequest() {
        HttpServletRequest result = getContextRequestOfNullable();
        AssertUtils.notNull(result, NOT_CURRENTLY_IN_WEB_SERVLET_CONTEXT);
        return result;
    }

    @Nullable
    public static HttpServletRequest getContextRequestOfNullable() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    public static <T> T requireRequestAttribute(@NotBlank String name) {
        return requireRequestAttribute(name, requireContextRequest());
    }

    public static <T> T requireRequestAttribute(@NotBlank String name, @NotNull HttpServletRequest request) {
        T result = getRequestAttribute(name, request);
        AssertUtils.notNull(result, () -> String.format("attribute = %s must not null", name));
        return result;
    }

    @Nullable
    public static <T> T getRequestAttribute(@NotBlank String name) {
        return getRequestAttribute(name, requireContextRequest());
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T getRequestAttribute(String name, HttpServletRequest request) {
        return (T) request.getAttribute(name);
    }

    @NotNull
    public static HttpServletResponse requireContextResponse() {
        HttpServletResponse result = getContextResponseOfNullable();
        AssertUtils.notNull(result, NOT_CURRENTLY_IN_WEB_SERVLET_CONTEXT);
        return result;
    }

    @Nullable
    public static HttpServletResponse getContextResponseOfNullable() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getResponse();
    }


    public static void writeJsonText(HttpServletResponse response, String data) {
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try {
            response.getWriter().write(data);
            response.getWriter().flush();
        } catch (IOException e) {
            log.error("writeJsonText error", e);
        }
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = null;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if ("127.0.0.1".equals(ipAddress)) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        log.error(e.getMessage());
                    }
                    ipAddress = inet.getHostAddress();
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) {
                // "***.***.***.***".length()
                // = 15
                if (ipAddress.indexOf(",") >= 1) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "";
        }
        return ipAddress;
    }


    public static boolean isValidIp(String ip) {
        if (ip == null) {
            return false;
        }
        // 去除前后空格
        ip = ip.trim();
        // 判断是否是 IPv4 地址
        if (isIpv4(ip)) {
            return true;
        }
        // 判断是否是 IPv6 地址
        return isIpv6(ip);
    }

    public static boolean isIpv4(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            // 检查是否为空
            if (part.isEmpty()) {
                return false;
            }
            // 检查是否有前导零
            if (part.length() > 1 && part.startsWith("0")) {
                return false;
            }
            // 尝试转换为整数并检查范围
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }


    public static boolean isIpv6(String ip) {
        String[] parts = ip.split(":");
        if (parts.length != 8) {
            return false;
        }
        for (String part : parts) {
            if (part.isEmpty() || part.length() > 4) {
                return false;
            }
            try {
                Integer.parseInt(part, 16);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }


    public static HttpServletRequest getCurrentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest();
        }
        return null;
    }

    /**
     * 是否英文语言
     *
     * @return true 英文
     */
    public static boolean isEn() {
        HttpServletRequest httpServletRequest = getCurrentRequest();
        String lang = httpServletRequest.getHeader("Lang");
        return "en_US".equalsIgnoreCase(lang);
    }

    /**
     * 获取当前请求的所有headers
     *
     * @return Map<String, String>
     */
    public static Map<String, Object> getHeaders() {
        HttpServletRequest request = getCurrentRequest();
        Map<String, Object> headersMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headersMap.put(headerName, headerValue);
        }
        return headersMap;
    }

    /**
     * 获取当前请求的所有参数 ?user=123
     *
     * @return Map<String, String>
     */
    public static Map<String, Object> getQueryParameters() {
        HttpServletRequest request = getCurrentRequest();
        Map<String, Object> queryParamsMap = new HashMap<>();
        String queryString = request.getQueryString();
        if (queryString != null) {
            String[] params = queryString.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length > 1) {
                    queryParamsMap.put(keyValue[0], keyValue[1]);
                } else {
                    queryParamsMap.put(keyValue[0], "");
                    // 如果参数值为空
                }
            }
        }
        return queryParamsMap;
    }

}