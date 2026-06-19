package com.senior.project.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senior.project.application.dto.CreateProductRequestDto;
import com.senior.project.domain.entity.Product;
import com.senior.project.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProductController
 * 
 * Demonstrates:
 * - Spring Boot testing with MockMvc
 * - REST API integration testing
 * - Database interaction testing
 * - JSON serialization/deserialization
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private CreateProductRequestDto createRequest;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        createRequest = CreateProductRequestDto.builder()
            .sku("TEST-SKU-001")
            .name("Integration Test Product")
            .description("Product for integration testing")
            .price(BigDecimal.valueOf(99.99))
            .quantity(100)
            .category("Test")
            .build();
    }

    @Test
    void testCreateProduct_Success() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.sku").value("TEST-SKU-001"))
            .andExpect(jsonPath("$.name").value("Integration Test Product"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andReturn();

        // Verify product was saved in database
        assert productRepository.findBySku("TEST-SKU-001").isPresent();
    }

    @Test
    void testCreateProduct_ValidationError_MissingRequired() throws Exception {
        createRequest.setName(null);

        mockMvc.perform(post("/api/v1/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation Error"))
            .andExpect(jsonPath("$.fieldErrors").exists())
            .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void testCreateProduct_ValidationError_InvalidPrice() throws Exception {
        createRequest.setPrice(BigDecimal.ZERO);

        mockMvc.perform(post("/api/v1/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.price").exists());
    }

    @Test
    void testGetProductById_Success() throws Exception {
        // Create a product first
        Product product = Product.builder()
            .sku("TEST-SKU-002")
            .name("Test Product")
            .price(BigDecimal.valueOf(99.99))
            .quantity(100)
            .category("Test")
            .status(Product.ProductStatus.ACTIVE)
            .createdBy("test")
            .updatedBy("test")
            .build();
        Product savedProduct = productRepository.save(product);

        mockMvc.perform(get("/api/v1/products/{id}", savedProduct.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedProduct.getId().toString()))
            .andExpect(jsonPath("$.sku").value("TEST-SKU-002"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void testGetProductById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/products/550e8400-e29b-41d4-a716-446655440000"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testGetProductBySku_Success() throws Exception {
        // Create a product
        Product product = Product.builder()
            .sku("UNIQUE-SKU-001")
            .name("Test Product")
            .price(BigDecimal.valueOf(99.99))
            .quantity(100)
            .category("Test")
            .status(Product.ProductStatus.ACTIVE)
            .createdBy("test")
            .updatedBy("test")
            .build();
        productRepository.save(product);

        mockMvc.perform(get("/api/v1/products/sku/UNIQUE-SKU-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sku").value("UNIQUE-SKU-001"));
    }

    @Test
    void testGetAllProducts_Pagination() throws Exception {
        // Create multiple products
        for (int i = 0; i < 25; i++) {
            Product product = Product.builder()
                .sku("SKU-" + i)
                .name("Product " + i)
                .price(BigDecimal.valueOf(99.99))
                .quantity(100)
                .category("Test")
                .status(Product.ProductStatus.ACTIVE)
                .createdBy("test")
                .updatedBy("test")
                .build();
            productRepository.save(product);
        }

        mockMvc.perform(get("/api/v1/products?page=0&size=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(10))
            .andExpect(jsonPath("$.totalElements").value(25))
            .andExpect(jsonPath("$.totalPages").value(3))
            .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void testSearchProducts() throws Exception {
        // Create products
        Product product1 = Product.builder()
            .sku("SKU-SEARCH-1")
            .name("High Performance Laptop")
            .description("Laptop for development")
            .price(BigDecimal.valueOf(1299.99))
            .quantity(50)
            .category("Electronics")
            .status(Product.ProductStatus.ACTIVE)
            .createdBy("test")
            .updatedBy("test")
            .build();
        productRepository.save(product1);

        Product product2 = Product.builder()
            .sku("SKU-SEARCH-2")
            .name("Gaming Mouse")
            .description("High precision mouse")
            .price(BigDecimal.valueOf(59.99))
            .quantity(100)
            .category("Accessories")
            .status(Product.ProductStatus.ACTIVE)
            .createdBy("test")
            .updatedBy("test")
            .build();
        productRepository.save(product2);

        mockMvc.perform(get("/api/v1/products/search?q=laptop"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").greaterThanOrEqualTo(1))
            .andExpect(jsonPath("$.content[0].sku").value("SKU-SEARCH-1"));
    }

    @Test
    void testUpdateProduct_Success() throws Exception {
        // Create a product
        Product product = Product.builder()
            .sku("TEST-UPDATE")
            .name("Original Name")
            .price(BigDecimal.valueOf(99.99))
            .quantity(100)
            .category("Test")
            .status(Product.ProductStatus.ACTIVE)
            .createdBy("test")
            .updatedBy("test")
            .build();
        Product savedProduct = productRepository.save(product);

        String updateJson = objectMapper.writeValueAsString(
            com.senior.project.application.dto.UpdateProductRequestDto.builder()
                .name("Updated Name")
                .price(BigDecimal.valueOf(149.99))
                .build()
        );

        mockMvc.perform(put("/api/v1/products/{id}", savedProduct.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Name"))
            .andExpect(jsonPath("$.price").value(149.99));
    }

    @Test
    void testDeleteProduct_Success() throws Exception {
        // Create a product
        Product product = Product.builder()
            .sku("TEST-DELETE")
            .name("Product to Delete")
            .price(BigDecimal.valueOf(99.99))
            .quantity(100)
            .category("Test")
            .status(Product.ProductStatus.ACTIVE)
            .createdBy("test")
            .updatedBy("test")
            .build();
        Product savedProduct = productRepository.save(product);

        mockMvc.perform(delete("/api/v1/products/{id}", savedProduct.getId()))
            .andExpect(status().isNoContent());

        // Verify product status changed to DISCONTINUED
        Product deletedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        assert deletedProduct.getStatus() == Product.ProductStatus.DISCONTINUED;
    }

    @Test
    void testGetLowStockProducts() throws Exception {
        // Create low stock product
        Product lowStock = Product.builder()
            .sku("LOW-STOCK")
            .name("Low Stock Item")
            .price(BigDecimal.valueOf(99.99))
            .quantity(5)
            .category("Test")
            .status(Product.ProductStatus.ACTIVE)
            .createdBy("test")
            .updatedBy("test")
            .build();
        productRepository.save(lowStock);

        // Create normal stock product
        Product normalStock = Product.builder()
            .sku("NORMAL-STOCK")
            .name("Normal Stock Item")
            .price(BigDecimal.valueOf(99.99))
            .quantity(100)
            .category("Test")
            .status(Product.ProductStatus.ACTIVE)
            .createdBy("test")
            .updatedBy("test")
            .build();
        productRepository.save(normalStock);

        mockMvc.perform(get("/api/v1/products/low-stock?threshold=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].sku").value("LOW-STOCK"));
    }
}
