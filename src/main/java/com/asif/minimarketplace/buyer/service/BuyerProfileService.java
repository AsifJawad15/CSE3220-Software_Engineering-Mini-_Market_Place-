package com.asif.minimarketplace.buyer.service;

import com.asif.minimarketplace.buyer.dto.AddressRequest;
import com.asif.minimarketplace.buyer.dto.UpdateBuyerProfileRequest;
import com.asif.minimarketplace.buyer.entity.Address;
import com.asif.minimarketplace.buyer.entity.BuyerProfile;
import com.asif.minimarketplace.buyer.repository.AddressRepository;
import com.asif.minimarketplace.buyer.repository.BuyerProfileRepository;
import com.asif.minimarketplace.common.exception.AccessDeniedException;
import com.asif.minimarketplace.common.exception.NotFoundException;
import com.asif.minimarketplace.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuyerProfileService {

    private final BuyerProfileRepository buyerProfileRepository;
    private final AddressRepository addressRepository;

    @Transactional
    public BuyerProfile createProfile(User user) {
        BuyerProfile profile = BuyerProfile.builder()
                .user(user)
                .build();
        BuyerProfile saved = buyerProfileRepository.save(profile);
        log.info("Created buyer profile for user: {}", user.getEmail());
        return saved;
    }

    public BuyerProfile getProfileByUserId(Long userId) {
        return buyerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Buyer profile not found"));
    }

    @Transactional
    public BuyerProfile updateProfile(Long userId, UpdateBuyerProfileRequest request) {
        BuyerProfile profile = getProfileByUserId(userId);
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        return buyerProfileRepository.save(profile);
    }

    // ── Addresses ──────────────────────────────────────────────────────────

    public List<Address> getAddresses(Long userId) {
        BuyerProfile profile = getProfileByUserId(userId);
        return addressRepository.findByBuyerProfileId(profile.getId());
    }

    @Transactional
    public Address addAddress(Long userId, AddressRequest request) {
        BuyerProfile profile = getProfileByUserId(userId);

        boolean firstAddress = addressRepository.countByBuyerProfileId(profile.getId()) == 0;
        boolean setAsDefault = request.isMakeDefault() || firstAddress;

        if (setAsDefault) {
            clearDefaultAddress(profile.getId());
        }

        Address address = Address.builder()
                .buyerProfile(profile)
                .label(request.getLabel())
                .line1(request.getLine1())
                .city(request.getCity())
                .postal(request.getPostal())
                .country(request.getCountry())
                .phone(request.getPhone())
                .defaultAddress(setAsDefault)
                .build();

        Address saved = addressRepository.save(address);

        if (saved.isDefaultAddress()) {
            profile.setDefaultAddressId(saved.getId());
            buyerProfileRepository.save(profile);
        }
        return saved;
    }

    @Transactional
    public Address updateAddress(Long userId, Long addressId, AddressRequest request) {
        BuyerProfile profile = getProfileByUserId(userId);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address not found"));

        if (!address.getBuyerProfile().getId().equals(profile.getId())) {
            throw new AccessDeniedException("You do not own this address");
        }

        if (request.isMakeDefault() && !address.isDefaultAddress()) {
            clearDefaultAddress(profile.getId());
            profile.setDefaultAddressId(addressId);
            buyerProfileRepository.save(profile);
        }

        address.setLabel(request.getLabel());
        address.setLine1(request.getLine1());
        address.setCity(request.getCity());
        address.setPostal(request.getPostal());
        address.setCountry(request.getCountry());
        address.setPhone(request.getPhone());
        address.setDefaultAddress(request.isMakeDefault());

        return addressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        BuyerProfile profile = getProfileByUserId(userId);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address not found"));

        if (!address.getBuyerProfile().getId().equals(profile.getId())) {
            throw new AccessDeniedException("You do not own this address");
        }

        boolean wasDefault = address.isDefaultAddress();
        addressRepository.delete(address);

        if (wasDefault) {
            profile.setDefaultAddressId(null);
            buyerProfileRepository.save(profile);
        }
    }

    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        BuyerProfile profile = getProfileByUserId(userId);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address not found"));

        if (!address.getBuyerProfile().getId().equals(profile.getId())) {
            throw new AccessDeniedException("You do not own this address");
        }

        clearDefaultAddress(profile.getId());
        address.setDefaultAddress(true);
        addressRepository.save(address);

        profile.setDefaultAddressId(addressId);
        buyerProfileRepository.save(profile);
    }

    private void clearDefaultAddress(Long profileId) {
        addressRepository.findByBuyerProfileIdAndDefaultAddressTrue(profileId)
                .ifPresent(addr -> {
                    addr.setDefaultAddress(false);
                    addressRepository.save(addr);
                });
    }
}

