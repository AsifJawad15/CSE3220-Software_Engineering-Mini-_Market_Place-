package com.asif.minimarketplace.buyer.entity;

import com.asif.minimarketplace.common.entity.BaseEntity;
import com.asif.minimarketplace.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "buyer_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "default_address_id")
    private Long defaultAddressId;
}


