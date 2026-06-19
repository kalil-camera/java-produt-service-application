package com.senior.project.presentation.controller;

import com.senior.project.application.dto.CreateProductRequestDto;
import com.senior.project.application.dto.ProductResponseDto;
import com.senior.project.application.dto.UpdateProductRequestDto;
import com.senior.project.application.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for Product resource.
 * 
 * Demonstrates:
 * - RESTful API design principles
 * - Proper HTTP status codes
 * - Request validation
 * - Pagination and sorting
 * - Exception handling via global exception handler
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * POST /api/v1/products - Create a new product
     */
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestBody CreateProductRequestDto requestDto) {
        log.info("POST /api/v1/products - Creating new product");
        
        ProductResponseDto response = productService.createProduct(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/products/{id} - Get product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable UUID id) {
        log.info("GET /api/v1/products/{} - Fetching product", id);
        
        ProductResponseDto response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/products/search?q={term} - Search products
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDto>> searchProducts(
            @RequestParam(name = "q") String searchTerm,
            @PageableDefault(size = 20, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        log.info("GET /api/v1/products/search - Searching with term: {}", searchTerm);
        
        Page<ProductResponseDto> response = productService.searchProducts(searchTerm, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/products - Get all active products
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(
            @PageableDefault(size = 20, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        log.info("GET /api/v1/products - Fetching all active products");
        
        Page<ProductResponseDto> response = productService.getAllActiveProducts(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/products/category/{category} - Get products by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ProductResponseDto>> getProductsByCategory(
            @PathVariable String category,
            @PageableDefault(size = 20, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        log.info("GET /api/v1/products/category/{} - Fetching products", category);
        
        Page<ProductResponseDto> response = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/products/low-stock - Get products with low stock
     */
    @GetMapping("/low-stock")
    public ResponseEntity<Page<ProductResponseDto>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold,
            @PageableDefault(size = 20, page = 0, sort = "quantity", direction = Sort.Direction.ASC) 
            Pageable pageable) {
        log.info("GET /api/v1/products/low-stock - Fetching low stock products with threshold: {}", threshold);
        
        Page<ProductResponseDto> response = productService.getLowStockProducts(threshold, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/products/{id} - Update product
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequestDto requestDto) {
        log.info("PUT /api/v1/products/{} - Updating product", id);
        
        ProductResponseDto response = productService.updateProduct(id, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/products/{id} - Delete product
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        log.info("DELETE /api/v1/products/{} - Deleting product", id);
        
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/products/sku/{sku} - Get product by SKU
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponseDto> getProductBySku(@PathVariable String sku) {
        log.info("GET /api/v1/products/sku/{} - Fetching product by SKU", sku);
        
        ProductResponseDto response = productService.getProductBySku(sku);
        return ResponseEntity.ok(response);
    }
}
