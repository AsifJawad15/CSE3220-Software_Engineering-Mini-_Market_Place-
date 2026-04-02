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

    @Test
    void updateProfile_Success() {
        UpdateSellerProfileRequest updateRequest = new UpdateSellerProfileRequest();
        updateRequest.setShopName("New Shop Name");
        updateRequest.setPhone("1234567890");

        when(sellerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(sellerProfileRepository.save(any(SellerProfile.class))).thenReturn(profile);

        SellerProfile result = sellerProfileService.updateProfile(1L, updateRequest);

        assertNotNull(result);
        assertEquals("New Shop Name", profile.getShopName());
        assertEquals("1234567890", profile.getPhone());
        verify(sellerProfileRepository).save(profile);
    }

    @Test
    void approveSeller_ChangesStatusToApproved() {
        when(sellerProfileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(sellerProfileRepository.save(any(SellerProfile.class))).thenReturn(profile);

        SellerProfile result = sellerProfileService.approve(10L);

        assertEquals(ApprovalStatus.APPROVED, result.getApprovalStatus());
        verify(sellerProfileRepository).save(profile);
    }

    @Test
    void rejectSeller_ChangesStatusToRejected() {
        when(sellerProfileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(sellerProfileRepository.save(any(SellerProfile.class))).thenReturn(profile);

        SellerProfile result = sellerProfileService.reject(10L);

        assertEquals(ApprovalStatus.REJECTED, result.getApprovalStatus());
        verify(sellerProfileRepository).save(profile);
    }
}