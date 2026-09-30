package com.ecommerce.config;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.Customer;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Role;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.CustomerRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            CartRepository cartRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed default demo customer if none exists
        if (!customerRepository.existsByEmail("customer@ecommerce.com")) {
            Customer customer = new Customer(
                    "John Customer",
                    "customer@ecommerce.com",
                    passwordEncoder.encode("password123"),
                    "+1-555-0199",
                    "123 Market St, Suite 400, San Francisco, CA",
                    Role.ROLE_CUSTOMER
            );
            Customer savedCustomer = customerRepository.save(customer);
            cartRepository.save(new Cart(savedCustomer));
        }

        // Seed default demo admin
        if (!customerRepository.existsByEmail("admin@ecommerce.com")) {
            Customer admin = new Customer(
                    "Store Admin",
                    "admin@ecommerce.com",
                    passwordEncoder.encode("admin123"),
                    "+1-555-0100",
                    "Headquarters, New York, NY",
                    Role.ROLE_ADMIN
            );
            customerRepository.save(admin);
        }

        // Seed products if catalog is empty
        if (productRepository.count() == 0) {
            List<Product> sampleProducts = List.of(
                    new Product(
                            "Apple MacBook Pro 16\"",
                            "Apple M3 Pro chip, 18GB unified memory, 512GB SSD storage, Liquid Retina XDR display.",
                            new BigDecimal("2499.00"),
                            "Electronics",
                            25,
                            "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800"
                    ),
                    new Product(
                            "Sony WH-1000XM5 Wireless Headphones",
                            "Industry-leading noise canceling with two processors and 8 microphones, up to 30h battery life.",
                            new BigDecimal("399.99"),
                            "Electronics",
                            50,
                            "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800"
                    ),
                    new Product(
                            "Samsung Galaxy S24 Ultra",
                            "Titanium Gray, 256GB storage, AI-powered camera with 200MP sensor and S-Pen.",
                            new BigDecimal("1199.99"),
                            "Electronics",
                            40,
                            "https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=800"
                    ),
                    new Product(
                            "Nike Air Max 270",
                            "Men's athletic shoes featuring a large Max Air unit for responsive cushioning.",
                            new BigDecimal("159.95"),
                            "Footwear",
                            75,
                            "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800"
                    ),
                    new Product(
                            "Classic Minimalist Leather Watch",
                            "Stainless steel case, genuine Italian leather strap, Japanese quartz movement, 50m water resistant.",
                            new BigDecimal("189.00"),
                            "Accessories",
                            60,
                            "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800"
                    ),
                    new Product(
                            "Nespresso VertuoPlus Coffee & Espresso Maker",
                            "Single serve coffee machine using Centrifusion technology for barista-grade coffee.",
                            new BigDecimal("169.00"),
                            "Home & Kitchen",
                            30,
                            "https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=800"
                    ),
                    new Product(
                            "Ergonomic Mesh Office Chair",
                            "High back executive chair with adjustable lumbar support, 3D armrests, and breathable mesh.",
                            new BigDecimal("289.50"),
                            "Furniture",
                            20,
                            "https://images.unsplash.com/photo-1580481077195-c3c761b203c9?w=800"
                    ),
                    new Product(
                            "Levi's Original 501 Jeans",
                            "Classic straight leg fit, 100% heavyweight cotton denim with signature button fly.",
                            new BigDecimal("79.50"),
                            "Fashion",
                            100,
                            "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800"
                    )
            );
            productRepository.saveAll(sampleProducts);
        }
    }
}
