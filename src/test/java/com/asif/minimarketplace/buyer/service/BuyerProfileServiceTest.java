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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuyerProfileServiceTest {

    @Mock
    private BuyerProfileRepository buyerProfileRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private BuyerProfileService buyerProfileService;

    private User user;
    private BuyerProfile profile;
    private Address address;
    private AddressRequest addressRequest;
    private UpdateBuyerProfileRequest updateRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");

        profile = new BuyerProfile();
        profile.setId(10L);
        profile.setUser(user);
        profile.setPhone("1234567890");

        address = new Address();
        address.setId(100L);
        address.setBuyerProfile(profile);
        address.setDefaultAddress(true);
        address.setLabel("Home");

        addressRequest = new AddressRequest();
        addressRequest.setLabel("Work");
        addressRequest.setCity("Metropolis");
        addressRequest.setMakeDefault(false);

        updateRequest = new UpdateBuyerProfileRequest();
        updateRequest.setPhone("0987654321");
    }

    @Test
    void createProfile_Success() {
        when(buyerProfileRepository.save(any(BuyerProfile.class))).thenReturn(profile);

        BuyerProfile result = buyerProfileService.createProfile(user);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        verify(buyerProfileRepository).save(any(BuyerProfile.class));
    }

    @Test
    void getProfileByUserId_Success() {
        when(buyerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        BuyerProfile result = buyerProfileService.getProfileByUserId(1L);

        assertNotNull(result);
        assertEquals(profile.getId(), result.getId());
    }

    @Test
    void getProfileByUserId_NotFound() {
        when(buyerProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> buyerProfileService.getProfileByUserId(1L));
    }

    @Test
    void updateProfile_Success() {
        when(buyerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(buyerProfileRepository.save(any(BuyerProfile.class))).thenReturn(profile);

        BuyerProfile result = buyerProfileService.updateProfile(1L, updateRequest);

        assertNotNull(result);
        assertEquals(updateRequest.getPhone(), profile.getPhone());
        verify(buyerProfileRepository).save(profile);
    }

    @Test
    void getAddresses_Success() {
        when(buyerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(addressRepository.findByBuyerProfileId(10L)).thenReturn(Collections.singletonList(address));

        List<Address> results = buyerProfileService.getAddresses(1L);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("Home", results.get(0).getLabel());
    }

    @Test
    void addAddress_FirstAddressBecomesDefault() {
        addressRequest.setMakeDefault(false);

        when(buyerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(addressRepository.countByBuyerProfileId(10L)).thenReturn(0L); // No existing addresses
        when(addressRepository.save(any(Address.class))).thenAnswer(i -> {
            Address a = i.getArgument(0);
            a.setId(200L);
            return a;
        });
        when(buyerProfileRepository.save(any(BuyerProfile.class))).thenReturn(profile);

        Address result = buyerProfileService.addAddress(1L, addressRequest);

        assertTrue(result.isDefaultAddress()); // First address must be default
        assertEquals("Work", result.getLabel());
        assertEquals(200L, profile.getDefaultAddressId());
        verify(addressRepository).save(any(Address.class));
        verify(buyerProfileRepository).save(profile);
    }

    @Test
    void addAddress_MakeDefaultClearsOldDefault() {
        addressRequest.setMakeDefault(true);
        
        Address oldDefault = new Address();
        oldDefault.setId(50L);
        oldDefault.setDefaultAddress(true);

        when(buyerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(addressRepository.countByBuyerProfileId(10L)).thenReturn(1L); // Existing addresses
        when(addressRepository.findByBuyerProfileIdAndDefaultAddressTrue(10L)).thenReturn(Optional.of(oldDefault));
        when(addressRepository.save(any(Address.class))).thenAnswer(i -> {
            Address a = i.getArgument(0);
            if (a.getId() == null) a.setId(200L);
            return a;
        });
        when(buyerProfileRepository.save(any(BuyerProfile.class))).thenReturn(profile);

        Address result = buyerProfileService.addAddress(1L, addressRequest);

        assertTrue(result.isDefaultAddress());
        assertFalse(oldDefault.isDefaultAddress()); // Old default was cleared
        assertEquals(200L, profile.getDefaultAddressId());
    }

    @Test
    void updateAddress_Success() {
        when(buyerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(addressRepository.findById(100L)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        address.setDefaultAddress(false);
        addressRequest.setMakeDefault(true);
        when(buyerProfileRepository.save(any(BuyerProfile.class))).thenReturn(profile);

        Address result = buyerProfileService.updateAddress(1L, 100L, addressRequest);

        assertNotNull(result);
        assertEquals("Work", result.getLabel());
        assertTrue(result.isDefaultAddress());
        verify(addressRepository).save(address);
    }

    @Test
    void updateAddress_AccessDenied() {
        BuyerProfile otherProfile = new BuyerProfile();
        otherProfile.setId(20L);
        Address otherAddress = new Address();
        otherAddress.setId(100L);
        otherAddress.setBuyerProfile(otherProfile);

        when(buyerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(addressRepository.findById(100L)).thenReturn(Optional.of(otherAddress));

        assertThrows(AccessDeniedException.class, () -> buyerProfileService.updateAddress(1L, 100L, addressRequest));
    }

    @Test
    void deleteAddress_Success() {
        when(buyerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(addressRepository.findById(100L)).thenReturn(Optional.of(address));
        
        buyerProfileService.deleteAddress(1L, 100L);

        verify(addressRepository).delete(address);
        assertNull(profile.getDefaultAddressId());
        verify(buyerProfileRepository).save(profile); // Since it was the default address
    }

    @Test
    void setDefaultAddress_Success() {
        address.setDefaultAddress(false);
        when(buyerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(addressRepository.findById(100L)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(buyerProfileRepository.save(any(BuyerProfile.class))).thenReturn(profile);

        buyerProfileService.setDefaultAddress(1L, 100L);

        assertTrue(address.isDefaultAddress());
        assertEquals(100L, profile.getDefaultAddressId());
        verify(addressRepository).save(address);
        verify(buyerProfileRepository).save(profile);
    }
}
