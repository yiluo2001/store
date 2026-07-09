package de.yiluo.store.cart;

import de.yiluo.store.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // @AuthenticationPrincipal let backend know who is the current user
    @GetMapping
    public ResponseEntity<List<CartItem>> getCart(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.getCartContents(user));
    }

    @PostMapping("/add")
    public ResponseEntity<CartItem> addToCart(
            @AuthenticationPrincipal User user,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.addProductToCart(user, productId, quantity));
    }

    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<CartItem> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(cartItemId, quantity));
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<String> removeFromCart(@PathVariable Long cartItemId) {
        cartService.removeProductFromCart(cartItemId);
        return ResponseEntity.ok("Item removed from cart.");
    }
}