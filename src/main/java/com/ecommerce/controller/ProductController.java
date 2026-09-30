package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product catalog and search endpoints")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Show all products", description = "Retrieves the complete catalog of products available in the store.")
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Searches products by keyword in name, description, or category.")
    public ResponseEntity<ApiResponse<List<Product>>> searchProducts(
            @Parameter(description = "Keyword to search across product name, description, and category")
            @RequestParam(required = false, defaultValue = "") String keyword
    ) {
        List<Product> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(ApiResponse.success("Search results for: " + keyword, products));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves detailed information of a specific product.")
    public ResponseEntity<ApiResponse<Product>> getProductById(
            @Parameter(description = "ID of the product to retrieve")
            @PathVariable Long id
    ) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Retrieves all distinct product categories.")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = productService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category", description = "Retrieves products filtered by specific category name.")
    public ResponseEntity<ApiResponse<List<Product>>> getProductsByCategory(
            @Parameter(description = "Category name (e.g., Electronics, Fashion, Home)")
            @PathVariable String category
    ) {
        List<Product> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new product (Admin)", description = "Adds a new product to the catalog. Requires ROLE_ADMIN.")
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody Product product) {
        Product saved = productService.saveProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Product created", saved));
    }
}
