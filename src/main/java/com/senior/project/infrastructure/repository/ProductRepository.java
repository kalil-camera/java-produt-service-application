package com.senior.project.infrastructure.repository;

import com.senior.project.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Product entity following Spring Data JPA best practices.
 * 
 * Demonstrates:
 * - Custom query methods with @Query
 * - Pagination and sorting support
 * - Named parameters for security (prevents SQL injection)
 * - Domain-specific queries
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySku(String sku);

    /**
     * Find all active products with pagination
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE'")
    Page<Product> findAllActive(Pageable pageable);

    /**
     * Find products by category with filtering
     */
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.status = 'ACTIVE'")
    Page<Product> findByCategory(@Param("category") String category, Pageable pageable);

    /**
     * Find products with low stock
     */
    @Query("SELECT p FROM Product p WHERE p.quantity < :threshold AND p.status = 'ACTIVE'")
    Page<Product> findLowStockProducts(@Param("threshold") Integer threshold, Pageable pageable);

    /**
     * Search products by name or description
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND p.status = 'ACTIVE'")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);
}
