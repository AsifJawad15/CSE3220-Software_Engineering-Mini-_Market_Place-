package com.asif.minimarketplace.order.service;

import com.asif.minimarketplace.buyer.entity.Address;
import com.asif.minimarketplace.buyer.entity.BuyerProfile;
import com.asif.minimarketplace.buyer.service.BuyerProfileService;
import com.asif.minimarketplace.cart.entity.Cart;
import com.asif.minimarketplace.cart.entity.CartItem;
import com.asif.minimarketplace.cart.service.CartService;
import com.asif.minimarketplace.common.exception.InsufficientStockException;
import com.asif.minimarketplace.order.entity.Order;
import com.asif.minimarketplace.order.entity.OrderItem;
import com.asif.minimarketplace.order.entity.OrderStatus;
import com.asif.minimarketplace.order.repository.OrderRepository;
import com.asif.minimarketplace.payment.PaymentMethod;
import com.asif.minimarketplace.payment.PaymentResult;
import com.asif.minimarketplace.payment.strategy.PaymentStrategy;
import com.asif.minimarketplace.payment.strategy.PaymentStrategyFactory;
import com.asif.minimarketplace.product.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final BuyerProfileService buyerProfileService;
    private final CartService cartService;
    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;

    /**
     * Full checkout transaction:
     * 1. Validate stock for every cart item
     * 2. Process payment via Strategy pattern (COD / bKash / Nagad)
     * 3. Create Order + OrderItems
     * 4. Deduct stock
     * 5. Clear cart
     */
    @Transactional
    public Order checkout(Long userId, Long addressId, PaymentMethod paymentMethod) {
        BuyerProfile profile = buyerProfileService.getProfileByUserId(userId);
        Cart cart = cartService.getOrCreateCart(userId);

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // 1. Validate all stock upfront
        for (CartItem item : cart.getItems()) {
            inventoryService.validateStock(item.getProduct().getId(), item.getQuantity());
        }

        // Build shipping address string
        String shippingAddress = buildShippingAddress(profile, addressId);

        // 2. Calculate total
        BigDecimal total = cartService.calculateTotal(cart);

        // 3. Process payment via Strategy pattern
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(paymentMethod);
        PaymentResult paymentResult = strategy.pay(total, "ORD-" + System.currentTimeMillis());

        if (!paymentResult.isSuccess()) {
            throw new IllegalStateException("Payment failed: " + paymentResult.getMessage());
        }

        // 4. Create Order
        Order order = Order.builder()
                .buyerProfile(profile)
                .status(OrderStatus.PENDING)
                .paymentMethod(paymentMethod)
                .transactionId(paymentResult.getTransactionId())
                .totalAmount(total)
                .shippingAddress(shippingAddress)
                .items(new ArrayList<>())
                .build();

        // 5. Create OrderItems
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .sellerProfile(cartItem.getProduct().getSeller())
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(cartItem.getUnitPriceSnapshot())
                    .build();
            order.getItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // 6. Deduct stock
        for (CartItem item : cart.getItems()) {
            inventoryService.decreaseStock(item.getProduct().getId(), item.getQuantity());
        }

        // 7. Clear cart
        cartService.clearCart(cart);

        log.info("Order #{} placed by buyer {} via {} - total: {}",
                savedOrder.getId(), profile.getUser().getEmail(), paymentMethod, total);
        return savedOrder;
    }

    private String buildShippingAddress(BuyerProfile profile, Long addressId) {
        if (addressId != null) {
            try {
                List<Address> addresses = buyerProfileService.getAddresses(profile.getUser().getId());
                return addresses.stream()
                        .filter(a -> a.getId().equals(addressId))
                        .findFirst()
                        .map(a -> a.getLine1() + ", " + a.getCity() + ", " + a.getPostal() + ", " + a.getCountry())
                        .orElse("Address not found");
            } catch (Exception e) {
                return "Default Address";
            }
        }
        return "Default Address";
    }
}

