package com.asif.minimarketplace.seller.service;

import com.asif.minimarketplace.common.exception.NotFoundException;
import com.asif.minimarketplace.seller.dto.UpdateSellerProfileRequest;
import com.asif.minimarketplace.seller.entity.ApprovalStatus;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import com.asif.minimarketplace.seller.repository.SellerProfileRepository;
import com.asif.minimarketplace.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerProfileServiceTest {

    @Mock
    private SellerProfileRepository sellerProfileRepository;

    @InjectMocks
    private SellerProfileService sellerProfileService;

    private User user;
    private SellerProfile profile;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("seller@test.com");

        profile = new SellerProfile();
        profile.setId(10L);
        profile.setUser(user);
        profile.setShopName("Initial Shop");
        profile.setApprovalStatus(ApprovalStatus.PENDING);
    }

    @Test
    void createProfile_Success() {
        when(sellerProfileRepository.save(any(SellerProfile.class))).thenReturn(profile);

        SellerProfile result = sellerProfileService.createProfile(user, "Initial Shop");

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals("Initial Shop", result.getShopName());
        verify(sellerProfileRepository).save(any(SellerProfile.class));
    }

    @Test
    void getProfileByUserId_Success() {
        when(sellerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        SellerProfile result = sellerProfileService.getProfileByUserId(1L);

        assertNotNull(result);
        assertEquals(profile.getId(), result.getId());
    }

    @Test
    void getProfileByUserId_NotFound() {
        when(sellerProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> sellerProfileService.getProfileByUserId(1L));
    }
}