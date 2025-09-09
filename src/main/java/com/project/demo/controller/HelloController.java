package com.project.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public String hello(Principal principal) {
        String username = (principal != null) ? principal.getName() : "anonymous";
        return "Hello, " + username + "! This is a protected resource.";
    }
}
