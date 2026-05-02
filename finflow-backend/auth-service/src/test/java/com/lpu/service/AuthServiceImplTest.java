package com.lpu.service;

import com.lpu.dto.LoginRequest;
import com.lpu.dto.SignupRequest;
import com.lpu.entity.Role;
import com.lpu.entity.User;
import com.lpu.repository.RoleRepository;
import com.lpu.repository.UserRepository;
import com.lpu.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl service;

    @Test
    void signup_ShouldSaveUser() {
        SignupRequest req = new SignupRequest();
        req.setEmail("test@example.com");
        req.setPassword("password");

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role()));
        when(encoder.encode(req.getPassword())).thenReturn("encoded");

        service.signup(req);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_ShouldThrow_WhenEmailExists() {
        SignupRequest req = new SignupRequest();
        req.setEmail("test@example.com");

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.signup(req));
    }

    @Test
    void login_ShouldReturnToken() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("password");

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword("encoded");
        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(Set.of(role));

        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(encoder.matches(req.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(any(), any())).thenReturn("token");

        String result = service.login(req);

        assertEquals("token", result);
    }

    @Test
    void login_ShouldThrow_WhenPasswordWrong() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("wrong");

        User user = new User();
        user.setPassword("encoded");

        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(encoder.matches(req.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.login(req));
    }
}
