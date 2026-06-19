package com.senior.project.application.service;

import com.senior.project.application.dto.CreateProductRequestDto;
import com.senior.project.application.dto.ProductResponseDto;
import com.senior.project.application.dto.UpdateProductRequestDto;
import com.senior.project.application.mapper.ProductMapper;
import com.senior.project.domain.entity.Product;
import com.senior.project.domain.exception.ProductNotFoundException;
import com.senior.project.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Product service with business logic and transaction management.
 * 
 * Demonstrates:
 * - @Transactional for ACID compliance
 * - Dependency injection
 * - Separation of concerns
 * - Logging for observability
 * - Security context usage
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Create a new product
     */
    @Transactional
    public ProductResponseDto createProduct(CreateProductRequestDto requestDto) {
        log.info("Creating new product with SKU: {}", requestDto.getSku());

        // Check if SKU already exists
        if (productRepository.findBySku(requestDto.getSku()).isPresent()) {
            log.warn("Attempt to create product with duplicate SKU: {}", requestDto.getSku());
            throw new IllegalArgumentException("Product with SKU already exists: " + requestDto.getSku());
        }

        Product product = productMapper.toEntity(requestDto);
        product.setCreatedBy(getCurrentUser());
        product.setUpdatedBy(getCurrentUser());

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return productMapper.toResponseDto(savedProduct);
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(UUID id) {
        log.debug("Fetching product with ID: {}", id);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Product not found with ID: {}", id);
                return new ProductNotFoundException(id);
            });

        return productMapper.toResponseDto(product);
    }

    /**
     * Get product by SKU
     */
    @Transactional(readOnly = true)
    public ProductResponseDto getProductBySku(String sku) {
        log.debug("Fetching product with SKU: {}", sku);

        Product product = productRepository.findBySku(sku)
            .orElseThrow(() -> {
                log.warn("Product not found with SKU: {}", sku);
                return new ProductNotFoundException(sku);
            });

        return productMapper.toResponseDto(product);
    }

    /**
     * Get all active products with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllActiveProducts(Pageable pageable) {
        log.debug("Fetching all active products with pagination: {}", pageable);
        
        return productRepository.findAllActive(pageable)
            .map(productMapper::toResponseDto);
    }

    /**
     * Search products by term
     */
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchProducts(String searchTerm, Pageable pageable) {
        log.debug("Searching products with term: {}", searchTerm);
        
        return productRepository.searchProducts(searchTerm, pageable)
            .map(productMapper::toResponseDto);
    }

    /**
     * Get products by category
     */
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByCategory(String category, Pageable pageable) {
        log.debug("Fetching products for category: {}", category);
        
        return productRepository.findByCategory(category, pageable)
            .map(productMapper::toResponseDto);
    }

    /**
     * Find low stock products
     */
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getLowStockProducts(Integer threshold, Pageable pageable) {
        log.debug("Fetching low stock products with threshold: {}", threshold);
        
        return productRepository.findLowStockProducts(threshold, pageable)
            .map(productMapper::toResponseDto);
    }

    /**
     * Update product
     */
    @Transactional
    public ProductResponseDto updateProduct(UUID id, UpdateProductRequestDto requestDto) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Product not found for update with ID: {}", id);
                return new ProductNotFoundException(id);
            });

        productMapper.updateEntityFromDto(requestDto, product);
        product.setUpdatedBy(getCurrentUser());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", id);

        return productMapper.toResponseDto(updatedProduct);
    }

    /**
     * Delete product (soft delete via status change)
     */
    @Transactional
    public void deleteProduct(UUID id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Product not found for deletion with ID: {}", id);
                return new ProductNotFoundException(id);
            });

        product.setStatus(Product.ProductStatus.DISCONTINUED);
        product.setUpdatedBy(getCurrentUser());
        productRepository.save(product);

        log.info("Product deleted successfully (marked as discontinued) with ID: {}", id);
    }

    /**
     * Helper method to get current user from security context
     */
    private String getCurrentUser() {
        try {
            SecurityContext context = SecurityContextHolder.getContext();
            Authentication authentication = context.getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.warn("Unable to get current user from security context", e);
        }
        return "system";
    }
}
