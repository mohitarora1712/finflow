package com.lpu.service;

import com.lpu.dto.LoginRequest;
import com.lpu.dto.SignupRequest;
import com.lpu.entity.Role;
import com.lpu.entity.User;
import com.lpu.repository.RoleRepository;
import com.lpu.repository.UserRepository;
import com.lpu.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           BCryptPasswordEncoder encoder,
                           JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void signup(SignupRequest request) {

        if(userRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email already exists");

        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setRoles(Set.of(role));

        userRepository.save(user);
    }

    @Override
    public String login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if(!encoder.matches(request.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid credentials");

        String role = user.getRoles().iterator().next().getName();
        return jwtUtil.generateToken(user.getEmail(), role);
    }
}