package com.asif.minimarketplace.seller.entity;

import com.asif.minimarketplace.common.entity.BaseEntity;
import com.asif.minimarketplace.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seller_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "shop_name", nullable = false, length = 100)
    private String shopName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
}

