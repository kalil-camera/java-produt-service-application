package com.senior.project.application.mapper;

import com.senior.project.application.dto.CreateProductRequestDto;
import com.senior.project.application.dto.ProductResponseDto;
import com.senior.project.application.dto.UpdateProductRequestDto;
import com.senior.project.domain.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for Product entity and DTOs.
 * 
 * Demonstrates:
 * - Compile-time safe mapping
 * - Null handling strategies
 * - Partial update mapping
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {

    /**
     * Convert Product entity to response DTO
     */
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    ProductResponseDto toResponseDto(Product product);

    /**
     * Convert create request DTO to Product entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Product toEntity(CreateProductRequestDto dto);

    /**
     * Update entity from request DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromDto(UpdateProductRequestDto dto, @MappingTarget Product product);

    /**
     * Helper method to convert status enum to string
     */
    default String statusToString(Product.ProductStatus status) {
        return status != null ? status.name() : null;
    }
}
