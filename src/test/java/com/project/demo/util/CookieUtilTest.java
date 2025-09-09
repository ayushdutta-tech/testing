package com.project.demo.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CookieUtil.
 */
class CookieUtilTest {

    @Test
    void testAddRefreshTokenCookie() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        CookieUtil.addRefreshTokenCookie(response, "sample-token");

        verify(response).addCookie(argThat(cookie ->
                CookieUtil.REFRESH_TOKEN_COOKIE.equals(cookie.getName()) &&
                        "sample-token".equals(cookie.getValue()) &&
                        cookie.isHttpOnly() &&
                        !cookie.getSecure() &&
                        "/".equals(cookie.getPath()) &&
                        cookie.getMaxAge() == CookieUtil.REFRESH_TOKEN_MAX_AGE
        ));
    }

    @Test
    void testClearRefreshTokenCookie() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        CookieUtil.clearRefreshTokenCookie(response);

        verify(response).addCookie(argThat(cookie ->
                CookieUtil.REFRESH_TOKEN_COOKIE.equals(cookie.getName()) &&
                        cookie.getValue() == null &&
                        cookie.getMaxAge() == 0 &&
                        "/".equals(cookie.getPath())
        ));
    }

    @Test
    void testGetRefreshTokenFromCookies_present() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] cookies = { new Cookie(CookieUtil.REFRESH_TOKEN_COOKIE, "token123") };
        when(request.getCookies()).thenReturn(cookies);

        Optional<String> token = CookieUtil.getRefreshTokenFromCookies(request);

        assertTrue(token.isPresent());
        assertEquals("token123", token.get());
    }

    @Test
    void testGetRefreshTokenFromCookies_absent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] cookies = { new Cookie("otherCookie", "value") };
        when(request.getCookies()).thenReturn(cookies);

        Optional<String> token = CookieUtil.getRefreshTokenFromCookies(request);

        assertFalse(token.isPresent());
    }

    @Test
    void testGetRefreshTokenFromCookies_nullCookies() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        Optional<String> token = CookieUtil.getRefreshTokenFromCookies(request);

        assertFalse(token.isPresent());
    }
}
