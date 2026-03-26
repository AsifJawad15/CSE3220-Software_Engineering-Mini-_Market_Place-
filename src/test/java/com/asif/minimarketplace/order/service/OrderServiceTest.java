package com.asif.minimarketplace.order.service;

import com.asif.minimarketplace.buyer.entity.BuyerProfile;
import com.asif.minimarketplace.buyer.service.BuyerProfileService;
import com.asif.minimarketplace.common.exception.AccessDeniedException;
import com.asif.minimarketplace.common.exception.NotFoundException;
import com.asif.minimarketplace.order.entity.Order;
import com.asif.minimarketplace.order.entity.OrderItem;
import com.asif.minimarketplace.order.entity.OrderStatus;
import com.asif.minimarketplace.order.repository.OrderItemRepository;
import com.asif.minimarketplace.order.repository.OrderRepository;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.service.InventoryService;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import com.asif.minimarketplace.seller.service.SellerProfileService;
import com.asif.minimarketplace.user.entity.RoleName;
import com.asif.minimarketplace.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private BuyerProfileService buyerProfileService;
    @Mock private SellerProfileService sellerProfileService;
    @Mock private InventoryService inventoryService;

    @InjectMocks private OrderService orderService;

    private User buyerUser;
    private BuyerProfile buyerProfile;
    private SellerProfile sellerProfile;
    private Product product;
    private Order order;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        buyerUser = User.builder().fullName("Buyer").email("buyer@test.com")
                .password("enc").role(RoleName.BUYER).enabled(true).build();
        buyerUser.setId(1L);

        buyerProfile = BuyerProfile.builder().user(buyerUser).build();
        buyerProfile.setId(1L);

        sellerProfile = new SellerProfile();
        sellerProfile.setId(10L);

        product = Product.builder().name("Widget").price(BigDecimal.TEN)
                .stockQuantity(50).active(true).seller(sellerProfile).build();
        product.setId(1L);

        orderItem = OrderItem.builder().product(product).sellerProfile(sellerProfile)
                .quantity(2).priceAtPurchase(BigDecimal.TEN).build();
        orderItem.setId(1L);

        order = Order.builder().buyerProfile(buyerProfile).status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(20.00)).shippingAddress("Test Address")
                .items(new ArrayList<>(List.of(orderItem))).build();
        order.setId(100L);
        orderItem.setOrder(order);
    }

    @Test
    void getBuyerOrders_returnsList() {
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(orderRepository.findByBuyerProfileIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(order));

        List<Order> result = orderService.getBuyerOrders(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
    }

    @Test
    void getBuyerOrderDetail_ownerMatch_returnsOrder() {
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(orderRepository.findByIdWithItems(100L)).thenReturn(Optional.of(order));

        Order result = orderService.getBuyerOrderDetail(1L, 100L);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20.00));
    }

    @Test
    void getBuyerOrderDetail_notOwner_throwsAccessDenied() {
        BuyerProfile other = BuyerProfile.builder().user(buyerUser).build();
        other.setId(99L);
        when(buyerProfileService.getProfileByUserId(2L)).thenReturn(other);
        when(orderRepository.findByIdWithItems(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getBuyerOrderDetail(2L, 100L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void cancelOrder_pending_success() {
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(orderRepository.findByIdWithItems(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.cancelOrder(1L, 100L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(inventoryService).increaseStock(1L, 2);
    }

    @Test
    void cancelOrder_notPending_throwsIllegalState() {
        order.setStatus(OrderStatus.SHIPPED);
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(orderRepository.findByIdWithItems(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, 100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only PENDING");
    }

    @Test
    void advanceOrderStatus_pending_becomesConfirmed() {
        when(orderRepository.findByIdWithItems(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.advanceOrderStatus(100L, 10L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void advanceOrderStatus_sellerNotInOrder_throwsAccessDenied() {
        when(orderRepository.findByIdWithItems(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.advanceOrderStatus(100L, 999L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void countByStatus_delegates() {
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(5L);
        long count = orderService.countByStatus(OrderStatus.PENDING);
        assertThat(count).isEqualTo(5);
    }
}

