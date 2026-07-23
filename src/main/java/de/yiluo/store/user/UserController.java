package de.yiluo.store.user;

import de.yiluo.store.security.JwtService;
import de.yiluo.store.security.TokenBlacklist;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final TokenBlacklist tokenBlacklist;
    private final AuthenticationManager authenticationManager;

    public UserController(UserService userService, JwtService jwtService, TokenBlacklist tokenBlacklist, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.tokenBlacklist = tokenBlacklist;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    // instead of just return User
    // wrap it in ResponseEntity to customize http status code/header and message
    // @RequestBody turn json data into java obj
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    @GetMapping("/manage")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/manage/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully.");
    }

    // Basic Authentication (Basic Auth) approach
    // AuthenticationManager happen behind the scene
//    @PostMapping("/login")
//    // username/password is in request header, @AuthenticationPrincipal turn that info into java obj
//    public ResponseEntity<User> login(@AuthenticationPrincipal User user) {
//        if (user == null) {
//            // 401 means unauthorized
//            return ResponseEntity.status(401).build();
//        }
//        return ResponseEntity.ok(user);
//    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // check if username exists and if password correct
        // nothing happens if success
        // throw error if bad password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        UserDetails userDetails = userService.loadUserByUsername(username);
        String token = jwtService.generateToken(userDetails);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", username);

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                // .findFirst() return optional bc you are not sure if this is empty list
                // grantedAuthority.getAuthority() turn optional GrantedAuthority into optional string
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                // this take thing out of the optional wrapper
                .orElse("ROLE_USER");
        response.put("role", role);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            // blacklist the token
            tokenBlacklist.blacklistToken(jwt);
            return ResponseEntity.ok("Successfully blacklisted token and logged out.");
        }

        // can add so that frontend session storage is deleted

        // you cannot log out when not logged in
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No valid bearer token provided.");
    }

    @PutMapping("/manage/{id}/role")
    public ResponseEntity<User> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String newRole = request.get("role");
        if (newRole == null || newRole.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        User updatedUser = userService.updateUserRole(id, newRole);
        return ResponseEntity.ok(updatedUser);
    }

}