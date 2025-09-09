package com.project.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LoginAuditServiceTest {

    @TempDir Path tempDir;
    private LoginAuditService auditService;
    private File auditFile;

    @BeforeEach
    void setUp() {
        auditFile = tempDir.resolve("user.json").toFile();
        auditService = new LoginAuditService(auditFile.getAbsolutePath());
    }

    @Test
    void saveLoginLog_shouldCreateFileAndAppendLogs() throws Exception {
        Map<String, Object> log1 = new HashMap<>();
        log1.put("username", "alice");
        log1.put("status", "success");

        Map<String, Object> log2 = new HashMap<>();
        log2.put("username", "bob");
        log2.put("status", "failure");

        auditService.saveLoginLog(log1);
        auditService.saveLoginLog(log2);

        assertTrue(auditFile.exists());

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> logs =
                mapper.readValue(auditFile, mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

        assertEquals(2, logs.size());
        assertEquals("alice", logs.get(0).get("username"));
        assertEquals("bob", logs.get(1).get("username"));
    }

    @Test
    void saveLoginLog_shouldHandleEmptyFileGracefully() {
        Map<String, Object> log = new HashMap<>();
        log.put("username", "charlie");
        log.put("status", "success");

        auditService.saveLoginLog(log);

        assertTrue(auditFile.exists());
    }
}
