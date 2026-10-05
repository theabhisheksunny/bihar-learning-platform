package com.biharlearning.service;

import com.biharlearning.entity.AppUser;
import com.biharlearning.exception.BusinessException;
import com.biharlearning.repository.AppUserRepository;
import com.biharlearning.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository userRepo, PasswordEncoder encoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public String login(String email, String password) {
        AppUser user = userRepo.findByEmail(email)
                .orElseThrow(() -> new BusinessException("UNAUTHORIZED", "Invalid credentials", HttpStatus.UNAUTHORIZED));
        if (user.getPasswordHash() == null || !encoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException("UNAUTHORIZED", "Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        return jwtService.generate(user.getEmail(), user.getRole());
    }
}
