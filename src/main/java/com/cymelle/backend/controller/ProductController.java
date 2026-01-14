package com.cymelle.backend.controller;

import com.cymelle.backend.model.Product;
import com.cymelle.backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cymelle.backend.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products")
public class ProductController {

    private final ProductService service;

    @GetMapping
    @Operation(summary = "Search or list all products", description = "Public endpoint to retrieve products items. Optional search parameter filters by name or category.")
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable
    ) {
        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(service.searchProducts(search, pageable));
        }
        return ResponseEntity.ok(service.getAllProducts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id)));
    }

    @PostMapping
    @Operation(
            summary = "Create a new product",
            description = "Protected endpoint. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product created successfully"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions (requires ADMIN)")
            }
    )
    public ResponseEntity<Product> createProduct(@RequestBody @Valid Product product) {
        return ResponseEntity.ok(service.saveProduct(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody @Valid Product product
    ) {
        return ResponseEntity.ok(service.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        service.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
}
