package com.lpu.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dev")
public class DevPasswordController {

    private final BCryptPasswordEncoder encoder;

    public DevPasswordController(BCryptPasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @GetMapping("/hash/{password}")
    public String hash(@PathVariable String password) {
        return encoder.encode(password);
    }
}
