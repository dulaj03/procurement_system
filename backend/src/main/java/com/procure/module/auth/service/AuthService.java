package com.procure.module.auth.service;

import com.procure.common.exception.BusinessException;
import com.procure.module.auth.dto.AuthDtos;
import com.procure.module.user.entity.Role;
import com.procure.module.user.entity.User;
import com.procure.module.user.repository.RoleRepository;
import com.procure.module.user.repository.UserRepository;
import com.procure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findActiveByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Account not found or inactive", HttpStatus.UNAUTHORIZED));

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(accessToken, refreshToken, user);
    }

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw BusinessException.conflict("Email is already registered: " + request.getEmail());
        }

        Role defaultRole = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseThrow(() -> new BusinessException("Default role not found. Please seed the database."));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .employeeCode(request.getEmployeeCode())
                .roles(Set.of(defaultRole))
                .status(User.UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return buildAuthResponse(accessToken, refreshToken, user);
    }

    public AuthDtos.AuthResponse refreshToken(AuthDtos.RefreshTokenRequest request) {
        String email = jwtService.extractUsername(request.getRefreshToken());
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!jwtService.isTokenValid(request.getRefreshToken(), userDetails)) {
            throw new BusinessException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> BusinessException.notFound("User"));

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        return buildAuthResponse(newAccessToken, newRefreshToken, user);
    }

    private AuthDtos.AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        AuthDtos.AuthResponse.UserInfo userInfo = new AuthDtos.AuthResponse.UserInfo();
        userInfo.setId(user.getId().toString());
        userInfo.setFirstName(user.getFirstName());
        userInfo.setLastName(user.getLastName());
        userInfo.setEmail(user.getEmail());
        userInfo.setRoles(user.getRoles().stream().map(Role::getName).toList());

        AuthDtos.AuthResponse response = new AuthDtos.AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtExpirationMs / 1000);
        response.setUser(userInfo);
        return response;
    }
}
