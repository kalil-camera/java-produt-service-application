package com.senior.project.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for updating an existing product.
 * 
 * Demonstrates:
 * - Partial updates with optional fields
 * - Input validation
 * - Clear separation from creation logic
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequestDto {

    @Size(min = 5, max = 255, message = "Name must be between 5 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have valid format")
    private BigDecimal price;

    @Min(value = 0, message = "Quantity cannot be negative")
    @Max(value = 999999, message = "Quantity exceeds maximum limit")
    private Integer quantity;

    @Size(min = 3, max = 50, message = "Category must be between 3 and 50 characters")
    private String category;

    @Pattern(regexp = "ACTIVE|INACTIVE|DISCONTINUED", message = "Invalid status value")
    private String status;
}
