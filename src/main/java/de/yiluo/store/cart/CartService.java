package de.yiluo.store.cart;

import de.yiluo.store.product.Product;
import de.yiluo.store.product.ProductService;
import de.yiluo.store.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional // every method in this class are transactions
public class CartService {

    private final CartRepository cartRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.productService = productService;
    }

    public List<CartItem> getCartContents(User user) {
        return cartRepository.findByUser(user);
    }

    public CartItem addProductToCart(User user, Long productId, Integer quantity) {
        Product product = productService.getProductById(productId);

        // check if item already exists in this user's cart
        Optional<CartItem> existingItem = cartRepository.findByUserAndProduct(user, product);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            return cartRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            return cartRepository.save(newItem);
        }
    }

    public CartItem updateQuantity(Long cartItemId, Integer quantity) {
        CartItem item = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        item.setQuantity(quantity);
        return cartRepository.save(item);
    }

    public void removeProductFromCart(Long cartItemId) {
        cartRepository.deleteById(cartItemId);
    }
}