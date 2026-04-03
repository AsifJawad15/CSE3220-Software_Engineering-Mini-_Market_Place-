package com.asif.minimarketplace.buyer.entity;

import com.asif.minimarketplace.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_profile_id", nullable = false)
    private BuyerProfile buyerProfile;

    @Column(name = "label", length = 50)
    private String label;

    @Column(name = "line1", nullable = false, length = 200)
    private String line1;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "postal", length = 20)
    private String postal;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean defaultAddress = false;
}
