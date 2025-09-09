package com.project.demo.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class RequestInfoUtil {

    public static String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    public static Map<String, Object> extractLoginInfo(String username, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String browser = request.getHeader("X-Browser");
        String os = request.getHeader("X-OS");
        String device = request.getHeader("X-Device");

        Map<String, Object> loginInfo = new LinkedHashMap<>();
        loginInfo.put("username", username);
        loginInfo.put("ip", ipAddress);
        loginInfo.put("browser", browser != null ? browser : parseUserAgent(userAgent));
        loginInfo.put("os", os);
        loginInfo.put("device", device);
        loginInfo.put("timestamp", new Date().toString());

        return loginInfo;
    }

    private static String parseUserAgent(String userAgent) {
        if (userAgent == null) return "Unknown";

        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("Edge")) return "Edge";

        return userAgent.length() > 50 ? userAgent.substring(0, 50) + "..." : userAgent;
    }
}
