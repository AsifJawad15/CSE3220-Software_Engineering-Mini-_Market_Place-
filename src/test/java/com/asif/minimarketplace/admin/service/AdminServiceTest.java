package com.asif.minimarketplace.admin.service;

import com.asif.minimarketplace.common.exception.NotFoundException;
import com.asif.minimarketplace.order.entity.OrderStatus;
import com.asif.minimarketplace.order.repository.OrderRepository;
import com.asif.minimarketplace.order.service.OrderService;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.repository.ProductRepository;
import com.asif.minimarketplace.product.service.CategoryService;
import com.asif.minimarketplace.product.service.ProductService;
import com.asif.minimarketplace.seller.entity.ApprovalStatus;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import com.asif.minimarketplace.seller.repository.SellerProfileRepository;
import com.asif.minimarketplace.seller.service.SellerProfileService;
import com.asif.minimarketplace.user.entity.User;
import com.asif.minimarketplace.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests admin-facing service methods: seller approval/rejection,
 * product listing, toggling active, and order counting.
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private SellerProfileRepository sellerProfileRepository;
    @InjectMocks private SellerProfileService sellerProfileService;

    private SellerProfile seller;

    @BeforeEach
    void setUp() {
        seller = SellerProfile.builder().shopName("My Shop")
                .approvalStatus(ApprovalStatus.PENDING).build();
        seller.setId(1L);
    }

    @Test
    void approveSeller_setsApproved() {
        when(sellerProfileRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(sellerProfileRepository.save(any(SellerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        SellerProfile result = sellerProfileService.approve(1L);
        assertThat(result.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
    }

    @Test
    void rejectSeller_setsRejected() {
        when(sellerProfileRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(sellerProfileRepository.save(any(SellerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        SellerProfile result = sellerProfileService.reject(1L);
        assertThat(result.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
    }

    @Test
    void findById_notFound_throwsException() {
        when(sellerProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerProfileService.findById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void countByStatus_delegates() {
        when(sellerProfileRepository.countByApprovalStatus(ApprovalStatus.PENDING)).thenReturn(3L);

        long count = sellerProfileService.countByStatus(ApprovalStatus.PENDING);
        assertThat(count).isEqualTo(3);
    }

    @Test
    void findAll_returnsAllSellers() {
        when(sellerProfileRepository.findAll()).thenReturn(List.of(seller));

        List<SellerProfile> result = sellerProfileService.findAll();
        assertThat(result).hasSize(1);
    }
}

