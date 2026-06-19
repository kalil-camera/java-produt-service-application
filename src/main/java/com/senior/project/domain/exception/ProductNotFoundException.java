package com.senior.project.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a Product is not found
 */
public class ProductNotFoundException extends DomainException {
    public ProductNotFoundException(UUID id) {
        super("Product not found with id: " + id);
    }

    public ProductNotFoundException(String sku) {
        super("Product not found with sku: " + sku);
    }
}
