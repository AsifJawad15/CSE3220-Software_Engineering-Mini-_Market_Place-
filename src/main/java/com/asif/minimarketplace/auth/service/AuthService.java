package com.asif.minimarketplace.auth.service;
import com.asif.minimarketplace.auth.dto.RegisterBuyerRequest;
import com.asif.minimarketplace.auth.dto.RegisterSellerRequest;
import com.asif.minimarketplace.common.exception.ValidationException;
import com.asif.minimarketplace.user.entity.RoleName;
import com.asif.minimarketplace.user.entity.User;
import com.asif.minimarketplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Transactional
    public User registerBuyer(RegisterBuyerRequest request) {
        validateRegistration(request.getEmail(), request.getPassword(), request.getConfirmPassword());
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(RoleName.BUYER)
                .enabled(true)
                .build();
        User saved = userRepository.save(user);
        log.info("Registered new BUYER: {}", saved.getEmail());
        return saved;
    }
    @Transactional
    public User registerSeller(RegisterSellerRequest request) {
        validateRegistration(request.getEmail(), request.getPassword(), request.getConfirmPassword());
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(RoleName.SELLER)
                .enabled(true)
                .build();
        User saved = userRepository.save(user);
        log.info("Registered new SELLER: {}", saved.getEmail());
        return saved;
    }
    private void validateRegistration(String email, String password, String confirmPassword) {
        if (userRepository.existsByEmail(email.toLowerCase().trim())) {
            throw new ValidationException("email", "An account with this email already exists");
        }
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("confirmPassword", "Passwords do not match");
        }
    }
}