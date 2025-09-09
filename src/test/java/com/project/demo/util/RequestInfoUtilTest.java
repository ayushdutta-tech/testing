package com.project.demo.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RequestInfoUtil.
 */
class RequestInfoUtilTest {

    @Test
    void testGetClientIpAddress_withXForwardedFor() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.10, proxy1, proxy2");

        String ip = RequestInfoUtil.getClientIpAddress(request);

        assertEquals("192.168.1.10", ip);
    }

    @Test
    void testGetClientIpAddress_withXRealIp() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("10.0.0.5");

        String ip = RequestInfoUtil.getClientIpAddress(request);

        assertEquals("10.0.0.5", ip);
    }

    @Test
    void testGetClientIpAddress_withRemoteAddr() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        String ip = RequestInfoUtil.getClientIpAddress(request);

        assertEquals("127.0.0.1", ip);
    }

    @Test
    void testExtractLoginInfo_withCustomHeaders() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.42");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (X11; Linux x86_64) Chrome/90.0.0.0");
        when(request.getHeader("X-Browser")).thenReturn("CustomBrowser");
        when(request.getHeader("X-OS")).thenReturn("Linux");
        when(request.getHeader("X-Device")).thenReturn("PC");

        Map<String, Object> loginInfo = RequestInfoUtil.extractLoginInfo("alice", request);

        assertEquals("alice", loginInfo.get("username"));
        assertEquals("203.0.113.42", loginInfo.get("ip"));
        assertEquals("CustomBrowser", loginInfo.get("browser"));
        assertEquals("Linux", loginInfo.get("os"));
        assertEquals("PC", loginInfo.get("device"));
        assertNotNull(loginInfo.get("timestamp"));
    }

    @Test
    void testExtractLoginInfo_withUserAgentFallback() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 Firefox/89.0");

        Map<String, Object> loginInfo = RequestInfoUtil.extractLoginInfo("bob", request);

        assertEquals("bob", loginInfo.get("username"));
        assertEquals("127.0.0.1", loginInfo.get("ip"));
        assertEquals("Firefox", loginInfo.get("browser")); // parsed from User-Agent
        assertNull(loginInfo.get("os")); // since not provided
        assertNull(loginInfo.get("device"));
    }

    @Test
    void testExtractLoginInfo_withUnknownUserAgent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("SomeRandomAgentWithoutKnownBrowser");

        Map<String, Object> loginInfo = RequestInfoUtil.extractLoginInfo("charlie", request);

        assertEquals("charlie", loginInfo.get("username"));
        assertTrue(((String) loginInfo.get("browser")).startsWith("SomeRandomAgent"));
    }
}
