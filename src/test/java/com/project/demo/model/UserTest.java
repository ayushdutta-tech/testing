package com.project.demo.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity.
 */
class UserTest {

    @Test
    void testGettersAndSetters() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setPassword("secret123");

        assertEquals(1L, user.getId());
        assertEquals("testUser", user.getUsername());
        assertEquals("secret123", user.getPassword());
    }

    @Test
    void testAuthoritiesShouldContainRoleUser() {
        User user = new User();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
    }

    @Test
    void testAccountStatusFlags() {
        User user = new User();

        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }

    @Test
    void testPasswordShouldBeIgnoredInJson() throws JsonProcessingException {
        User user = new User();
        user.setId(10L);
        user.setUsername("jsonUser");
        user.setPassword("hiddenPass");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(user);

        assertTrue(json.contains("jsonUser"), "JSON should contain username");
        assertFalse(json.contains("hiddenPass"), "JSON should not contain password");
    }
}
