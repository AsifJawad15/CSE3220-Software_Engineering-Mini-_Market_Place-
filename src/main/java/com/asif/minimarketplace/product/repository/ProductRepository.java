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

    /** Product detail — eagerly loads category, seller and tags to avoid LazyInitializationException. */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller LEFT JOIN FETCH p.tags WHERE p.id = :id")
    Optional<Product> findByIdWithDetails(@Param("id") Long id);

    /** Product browsing — fetches category and tags eagerly for product list display. */
    @EntityGraph(attributePaths = {"category", "tags"})
    Page<Product> findByActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "tags"})
    Page<Product> findByActiveTrueAndCategoryId(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "tags"})
    Page<Product> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);

    /** Seller product list — fetches category and tags eagerly. */
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.tags WHERE p.seller.id = :sellerId")
    List<Product> findBySellerId(@Param("sellerId") Long sellerId);

    long countBySellerId(Long sellerId);
    long countByActiveTrue();

    /** Admin product list — fetches category, seller, and tags eagerly. */
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller LEFT JOIN FETCH p.tags")
    List<Product> findAllWithDetails();
}
