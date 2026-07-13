package de.yiluo.store.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// integration test
@SpringBootTest
// look for application-test.properties
@ActiveProfiles("test")
// each test is a transaction, ie roll back when half done but fail
@Transactional
public class ProductControllerIT {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        productRepository.deleteAll();

        sampleProduct = new Product();
        sampleProduct.setName("Mechanical Keyboard");
        sampleProduct.setDescription("RGB Backlit mechanical keyboard");
        sampleProduct.setPrice(new BigDecimal("129.99"));
        sampleProduct.setStockQuantity(50);

        sampleProduct = productRepository.save(sampleProduct);
    }

    @Test
    void getAllProducts_ShouldReturnProductList() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$[0].price").value(129.99));
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() throws Exception {
        mockMvc.perform(get("/api/products/{id}", sampleProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.price").value(129.99));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_AsAdmin_ShouldReturnCreatedProduct() throws Exception {
        Product newProduct = new Product();
        newProduct.setName("Wireless Mouse");
        newProduct.setDescription("Ergonomic 2.4GHz mouse");
        newProduct.setPrice(new BigDecimal("49.99"));
        newProduct.setStockQuantity(100);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Wireless Mouse"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_AsAdmin_ShouldReturnUpdatedProduct() throws Exception {
        Product updatedDetails = new Product();
        updatedDetails.setName("Premium Keyboard");
        updatedDetails.setDescription("Upgraded aluminum casing");
        updatedDetails.setPrice(new BigDecimal("159.99"));
        updatedDetails.setStockQuantity(30);

        mockMvc.perform(put("/api/products/{id}", sampleProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Premium Keyboard"))
                .andExpect(jsonPath("$.price").value(159.99))
                .andExpect(jsonPath("$.stockQuantity").value(30));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_AsAdmin_ShouldDeleteProductAndReturnSuccessMessage() throws Exception {
        // 1. Delete the product
        mockMvc.perform(delete("/api/products/{id}", sampleProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Product deleted successfully."));

        // 2. Verify that trying to fetch it throws the expected RuntimeException
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            mockMvc.perform(get("/api/products/{id}", sampleProduct.getId()));
        });
    }
}