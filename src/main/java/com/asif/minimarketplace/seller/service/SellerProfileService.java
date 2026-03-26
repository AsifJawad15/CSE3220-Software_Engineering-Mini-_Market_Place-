package com.asif.minimarketplace.seller.service;

import com.asif.minimarketplace.common.exception.NotFoundException;
import com.asif.minimarketplace.seller.dto.UpdateSellerProfileRequest;
import com.asif.minimarketplace.seller.entity.ApprovalStatus;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import com.asif.minimarketplace.seller.repository.SellerProfileRepository;
import com.asif.minimarketplace.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerProfileService {

    private final SellerProfileRepository sellerProfileRepository;

    @Transactional
    public SellerProfile createProfile(User user, String shopName) {
        SellerProfile profile = SellerProfile.builder()
                .user(user)
                .shopName(shopName)
                .build();
        SellerProfile saved = sellerProfileRepository.save(profile);
        log.info("Created seller profile for user: {}", user.getEmail());
        return saved;
    }

    public SellerProfile getProfileByUserId(Long userId) {
        return sellerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Seller profile not found"));
    }

    public SellerProfile findById(Long id) {
        return sellerProfileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Seller profile", id));
    }

    @Transactional
    public SellerProfile updateProfile(Long userId, UpdateSellerProfileRequest request) {
        SellerProfile profile = getProfileByUserId(userId);
        if (request.getShopName() != null && !request.getShopName().isBlank()) {
            profile.setShopName(request.getShopName());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        return sellerProfileRepository.save(profile);
    }

    // ── Admin methods ──────────────────────────────────────────────────────

    public List<SellerProfile> findAll() {
        return sellerProfileRepository.findAllWithUser();
    }

    public List<SellerProfile> findByStatus(ApprovalStatus status) {
        return sellerProfileRepository.findByApprovalStatus(status);
    }

    public long countByStatus(ApprovalStatus status) {
        return sellerProfileRepository.countByApprovalStatus(status);
    }

    @Transactional
    public SellerProfile approve(Long sellerId) {
        SellerProfile profile = findById(sellerId);
        profile.setApprovalStatus(ApprovalStatus.APPROVED);
        log.info("Seller approved: {}", profile.getShopName());
        return sellerProfileRepository.save(profile);
    }

    @Transactional
    public SellerProfile reject(Long sellerId) {
        SellerProfile profile = findById(sellerId);
        profile.setApprovalStatus(ApprovalStatus.REJECTED);
        log.info("Seller rejected: {}", profile.getShopName());
        return sellerProfileRepository.save(profile);
    }
}


