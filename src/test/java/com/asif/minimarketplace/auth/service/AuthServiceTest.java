package com.asif.minimarketplace.auth.service;

import com.asif.minimarketplace.auth.dto.RegisterBuyerRequest;
import com.asif.minimarketplace.auth.dto.RegisterSellerRequest;
import com.asif.minimarketplace.buyer.service.BuyerProfileService;
import com.asif.minimarketplace.common.exception.ValidationException;
import com.asif.minimarketplace.seller.service.SellerProfileService;
import com.asif.minimarketplace.user.entity.RoleName;
import com.asif.minimarketplace.user.entity.User;
import com.asif.minimarketplace.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private BuyerProfileService buyerProfileService;
    @Mock private SellerProfileService sellerProfileService;

    @InjectMocks private AuthService authService;

    private RegisterBuyerRequest buyerRequest;
    private RegisterSellerRequest sellerRequest;

    @BeforeEach
    void setUp() {
        buyerRequest = new RegisterBuyerRequest();
        buyerRequest.setFullName("John Doe");
        buyerRequest.setEmail("john@example.com");
        buyerRequest.setPassword("password123");
        buyerRequest.setConfirmPassword("password123");

        sellerRequest = new RegisterSellerRequest();
        sellerRequest.setFullName("Jane Seller");
        sellerRequest.setEmail("jane@example.com");
        sellerRequest.setPassword("password123");
        sellerRequest.setConfirmPassword("password123");
        sellerRequest.setShopName("Jane's Shop");
    }

    @Test
    void registerBuyer_success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        User saved = User.builder().fullName("John Doe").email("john@example.com")
                .password("encodedPassword").role(RoleName.BUYER).enabled(true).build();
        saved.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = authService.registerBuyer(buyerRequest);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(RoleName.BUYER);
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(buyerProfileService).createProfile(saved);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerBuyer_duplicateEmail_throwsValidation() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.registerBuyer(buyerRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void registerBuyer_passwordMismatch_throwsValidation() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        buyerRequest.setConfirmPassword("differentPassword");

        assertThatThrownBy(() -> authService.registerBuyer(buyerRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Passwords do not match");
    }

    @Test
    void registerSeller_success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        User saved = User.builder().fullName("Jane Seller").email("jane@example.com")
                .password("encodedPassword").role(RoleName.SELLER).enabled(true).build();
        saved.setId(2L);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = authService.registerSeller(sellerRequest);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(RoleName.SELLER);
        verify(sellerProfileService).createProfile(saved, "Jane's Shop");
    }

    @Test
    void registerSeller_duplicateEmail_throwsValidation() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.registerSeller(sellerRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");
    }
}

