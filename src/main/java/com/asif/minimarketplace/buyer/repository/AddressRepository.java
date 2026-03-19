package com.asif.minimarketplace.buyer.repository;

import com.asif.minimarketplace.buyer.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByBuyerProfileId(Long buyerProfileId);
    Optional<Address> findByBuyerProfileIdAndDefaultAddressTrue(Long buyerProfileId);
    long countByBuyerProfileId(Long buyerProfileId);
}
