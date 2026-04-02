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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private BuyerProfileService buyerProfileService;

    @Mock
    private SellerProfileService sellerProfileService;

    @InjectMocks
    private AuthService authService;

    private RegisterBuyerRequest buyerRequest;
    private RegisterSellerRequest sellerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        buyerRequest = new RegisterBuyerRequest();
        buyerRequest.setFullName("Test Buyer");
        buyerRequest.setEmail("buyer@test.com");
        buyerRequest.setPassword("password");
        buyerRequest.setConfirmPassword("password");

        sellerRequest = new RegisterSellerRequest();
        sellerRequest.setFullName("Test Seller");
        sellerRequest.setEmail("seller@test.com");
        sellerRequest.setPassword("password");
        sellerRequest.setConfirmPassword("password");
        sellerRequest.setShopName("Test Shop");

        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
    }

    @Test
    void registerBuyer_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = authService.registerBuyer(buyerRequest);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(buyerProfileService, times(1)).createProfile(any(User.class));
    }

    @Test
    void registerBuyer_EmailExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            authService.registerBuyer(buyerRequest);
        });

        assertEquals("Validation failed for 'email': An account with this email already exists", exception.getMessage());
    }

    @Test
    void registerBuyer_PasswordsDoNotMatch() {
        buyerRequest.setConfirmPassword("wrongPassword");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            authService.registerBuyer(buyerRequest);
        });

        assertEquals("Validation failed for 'confirmPassword': Passwords do not match", exception.getMessage());
    }

    @Test
    void registerSeller_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = authService.registerSeller(sellerRequest);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(sellerProfileService, times(1)).createProfile(any(User.class), anyString());
    }

    @Test
    void registerSeller_EmailExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            authService.registerSeller(sellerRequest);
        });

        assertEquals("Validation failed for 'email': An account with this email already exists", exception.getMessage());
    }

    @Test
    void registerSeller_PasswordsDoNotMatch() {
        sellerRequest.setConfirmPassword("wrongPassword");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            authService.registerSeller(sellerRequest);
        });

        assertEquals("Validation failed for 'confirmPassword': Passwords do not match", exception.getMessage());
    }
}
