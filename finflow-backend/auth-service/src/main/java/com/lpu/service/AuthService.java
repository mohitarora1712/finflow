package com.lpu.service;

import com.lpu.dto.LoginRequest;
import com.lpu.dto.SignupRequest;

public interface AuthService {

    void signup(SignupRequest request);
    String login(LoginRequest request);
}