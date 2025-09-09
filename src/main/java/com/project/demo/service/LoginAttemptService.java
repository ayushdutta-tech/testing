package com.project.demo.service;

import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    // Threshold: 5 failures within 2 minutes -> block for 10 minutes
    private static final int  MAX_ATTEMPTS_IN_WINDOW = 5;
    private static final long WINDOW_MS              = 24 * 60 * 60 * 1000L;   // 2 minutes
    private static final long BLOCK_MS               = 24 * 60 * 60 * 1000L;  // 10 minutes

    // username -> deque of failure timestamps (only those within WINDOW_MS are kept)
    private final Map<String, Deque<Long>> failures = new ConcurrentHashMap<>();
    // username -> unblockAt (epoch millis) if currently blocked
    private final Map<String, Long> blockedUntil = new ConcurrentHashMap<>();


    public void loginSucceeded(String username) {
        failures.remove(username);
        blockedUntil.remove(username);
    }

    public void loginFailed(String username) {
        long now = System.currentTimeMillis();

        // If already blocked, keep it blocked
        Long until = blockedUntil.get(username);
        if (until != null && now < until) return;

        // Track failure within a sliding time window
        Deque<Long> q = failures.computeIfAbsent(username, k -> new ArrayDeque<>());
        synchronized (q) {
            pruneOld(q, now);
            q.addLast(now);

            if (q.size() >= MAX_ATTEMPTS_IN_WINDOW) {
                blockedUntil.put(username, now + BLOCK_MS);
                q.clear(); // optional: clear history after blocking
            }
        }
    }


    public boolean isBlocked(String username) {
        Long until = blockedUntil.get(username);
        if (until == null) return false;
        long now = System.currentTimeMillis();
        if (now >= until) {
            blockedUntil.remove(username);
            return false;
        }
        return true;
    }


    public long getRemainingBlockMillis(String username) {
        Long until = blockedUntil.get(username);
        if (until == null) return 0L;
        return Math.max(0L, until - System.currentTimeMillis());
    }


    public int getRemainingAttemptsInWindow(String username) {
        long now = System.currentTimeMillis();
        Deque<Long> q = failures.get(username);
        if (q == null) return MAX_ATTEMPTS_IN_WINDOW;
        synchronized (q) {
            pruneOld(q, now);
            return Math.max(0, MAX_ATTEMPTS_IN_WINDOW - q.size());
        }
    }

    private void pruneOld(Deque<Long> q, long now) {
        long cutoff = now - WINDOW_MS;
        while (!q.isEmpty() && q.peekFirst() < cutoff) {
            q.removeFirst();
        }
    }
}

