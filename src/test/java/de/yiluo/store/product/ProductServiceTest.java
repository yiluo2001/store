package de.yiluo.store.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setName("Mechanical Keyboard");
        sampleProduct.setDescription("RGB Backlit");
        sampleProduct.setPrice(new BigDecimal("99.99"));
        sampleProduct.setStockQuantity(50);
    }

    @Test
    void getAllProducts_ShouldReturnList() {
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct));

        List<Product> products = productService.getAllProducts();

        assertFalse(products.isEmpty());
        assertEquals(1, products.size());
        assertEquals("Mechanical Keyboard", products.get(0).getName());
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        Product foundProduct = productService.getProductById(1L);

        assertNotNull(foundProduct);
        assertEquals(1L, foundProduct.getId());
    }

    @Test
    void getProductById_WhenProductDoesNotExist_ShouldThrowRuntimeException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.getProductById(99L);
        });
        assertEquals("Product not found with id: 99", exception.getMessage());
    }

    @Test
    void saveProduct_ShouldSaveAndReturnProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        Product savedProduct = productService.saveProduct(sampleProduct);

        assertNotNull(savedProduct);
        assertEquals("Mechanical Keyboard", savedProduct.getName());
    }

    @Test
    void updateProduct_ShouldModifyExistingPropertiesAndSave() {
        Product updatedData = new Product();
        updatedData.setName("Upgraded Keyboard");
        updatedData.setDescription("Wireless RGB Backlit");
        updatedData.setPrice(new BigDecimal("129.99"));
        updatedData.setStockQuantity(30);

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.updateProduct(1L, updatedData);

        assertNotNull(result);
        assertEquals("Upgraded Keyboard", result.getName());
        assertEquals("Wireless RGB Backlit", result.getDescription());
        assertEquals(new BigDecimal("129.99"), result.getPrice());
        assertEquals(30, result.getStockQuantity());
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    void deleteProduct_ShouldInvokeRepositoryDelete() {
        productService.deleteProduct(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }
}