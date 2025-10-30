package com.kyedev.spring_security_jwt.service;

import com.kyedev.spring_security_jwt.dto.request.LoginRequest;
import com.kyedev.spring_security_jwt.dto.request.RegisterRequest;
import com.kyedev.spring_security_jwt.dto.response.AuthResponse;
import com.kyedev.spring_security_jwt.entity.RefreshToken;
import com.kyedev.spring_security_jwt.entity.Role;
import com.kyedev.spring_security_jwt.entity.User;
import com.kyedev.spring_security_jwt.exceptions.BadRequestException;
import com.kyedev.spring_security_jwt.exceptions.TokenRefreshException;
import com.kyedev.spring_security_jwt.repository.RoleRepository;
import com.kyedev.spring_security_jwt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;


    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if you use already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already in use");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        // Get default role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new BadRequestException("Role is not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(roles)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        User savedUser = userRepository.save(user);

        // Generate tokens custom claims
        Map<String, Object> extraClaims = new HashMap<>();
        String accessToken = jwtService.generateAccessToken(savedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwtService.getAccessTokenExpiration())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles().stream()
                        .map(Role::getName)
                        .toList()
                )
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {

        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Username not found"));

        // Generate tokens with custom claims
        Map<String, Object> extraClaims = buildExtraClaims(user);
        String accessToken = jwtService.generateAccessToken(extraClaims, user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwtService.getAccessTokenExpiration())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        return refreshTokenService.findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    Map<String, Object> extraClaims = buildExtraClaims(user);
                    String accessToken = jwtService.generateAccessToken(extraClaims, user);

                    return AuthResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshTokenStr)
                            .expiresIn(jwtService.getAccessTokenExpiration())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .roles(user.getRoles().stream().map(Role::getName).toList())
                            .build();
                })
                .orElseThrow(()  -> new TokenRefreshException(refreshTokenStr, "Refresh token invalid"));
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }

    // Build custom JWT claims (metadata)
    private Map<String, Object> buildExtraClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        return claims;
    }

}
