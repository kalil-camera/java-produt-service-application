package com.senior.project.application.service;

import com.senior.project.application.dto.CreateProductRequestDto;
import com.senior.project.application.dto.ProductResponseDto;
import com.senior.project.application.mapper.ProductMapper;
import com.senior.project.domain.entity.Product;
import com.senior.project.domain.exception.ProductNotFoundException;
import com.senior.project.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService
 * 
 * Demonstrates:
 * - Proper unit testing with mocks
 * - Test isolation
 * - AAA pattern (Arrange, Act, Assert)
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private CreateProductRequestDto createRequestDto;
    private Product product;
    private ProductResponseDto responseDto;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        createRequestDto = CreateProductRequestDto.builder()
            .sku("SKU-TEST-001")
            .name("Test Product")
            .description("A test product")
            .price(BigDecimal.valueOf(99.99))
            .quantity(100)
            .category("Test Category")
            .build();

        product = Product.builder()
            .id(productId)
            .sku("SKU-TEST-001")
            .name("Test Product")
            .description("A test product")
            .price(BigDecimal.valueOf(99.99))
            .quantity(100)
            .category("Test Category")
            .status(Product.ProductStatus.ACTIVE)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();

        responseDto = ProductResponseDto.builder()
            .id(productId)
            .sku("SKU-TEST-001")
            .name("Test Product")
            .description("A test product")
            .price(BigDecimal.valueOf(99.99))
            .quantity(100)
            .category("Test Category")
            .status("ACTIVE")
            .build();
    }

    @Test
    void testCreateProduct_Success() {
        // Arrange
        when(productRepository.findBySku(createRequestDto.getSku())).thenReturn(Optional.empty());
        when(productMapper.toEntity(createRequestDto)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponseDto(product)).thenReturn(responseDto);

        // Act
        ProductResponseDto result = productService.createProduct(createRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(responseDto.getSku(), result.getSku());
        assertEquals(responseDto.getName(), result.getName());
        verify(productRepository, times(1)).findBySku(createRequestDto.getSku());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testCreateProduct_DuplicateSku() {
        // Arrange
        when(productRepository.findBySku(createRequestDto.getSku())).thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(createRequestDto));
        verify(productRepository, times(1)).findBySku(createRequestDto.getSku());
        verify(productRepository, never()).save(any());
    }

    @Test
    void testGetProductById_Success() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toResponseDto(product)).thenReturn(responseDto);

        // Act
        ProductResponseDto result = productService.getProductById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(responseDto.getId(), result.getId());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void testGetProductById_NotFound() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(productId));
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void testDeleteProduct_Success() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        productService.deleteProduct(productId);

        // Assert
        assertEquals(Product.ProductStatus.DISCONTINUED, product.getStatus());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(any(Product.class));
    }
}
