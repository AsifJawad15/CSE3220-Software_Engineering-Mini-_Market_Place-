package com.asif.minimarketplace.product.repository;

import com.asif.minimarketplace.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Product detail — eagerly loads category and seller to avoid LazyInitializationException. */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller WHERE p.id = :id")
    Optional<Product> findByIdWithDetails(@Param("id") Long id);

    /** Product browsing — fetches category eagerly for product list display. */
    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByActiveTrueAndCategoryId(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);

    /** Seller product list — fetches category eagerly. */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.seller.id = :sellerId")
    List<Product> findBySellerId(@Param("sellerId") Long sellerId);

    long countBySellerId(Long sellerId);
    long countByActiveTrue();

    /** Admin product list — fetches category and seller eagerly. */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller")
    List<Product> findAllWithDetails();
}
