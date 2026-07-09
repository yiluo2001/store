package de.yiluo.store.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // @Configuration/@Bean to prepare bean
@EnableWebSecurity // turn off default security setting and apply custom
public class SecurityConfig {
    // HttpSecurity http is temporary configuration builder object
    // throws Exception let caller of the method to handle potential exception
    // instead of the method go wrong itself
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        // method chaining: same as doing the methods one by one
        http
                // if jwt is inside httponly cookie, then immune to xss
                // anti-csrf token is inside html so immune to csrf
                // they are used together to fight both xss and csrf
                // but here jwt is inside header, so also immune to csrf
                // so anti-csrf token is not needed, but open to xss
                // but front end framework fight xss for me
                .cors(Customizer.withDefaults())
                // .cors(cors -> cors.configure(http))
                .csrf(csrf -> csrf.disable())
                // so that server ram does not remember anything
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/register").permitAll()
                        .requestMatchers("/api/users/login").permitAll()
                        .requestMatchers("/api/users/manage/**").hasRole("ADMIN")

                        // Product
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        // Cart
                        .requestMatchers("/api/cart/**").authenticated()

                        // Order Rules
                        .requestMatchers("/api/orders/**").authenticated()

                        // Payment
                        // must be public for Stripe to reach it
                        // users must log in to purchase
                        .requestMatchers("/api/payments/webhook").permitAll()
                        .requestMatchers("/api/payments/create-intent").authenticated()

                        // will empty static folder in the end, testing just for now
                        .requestMatchers("/**").permitAll()

                        .requestMatchers("/admin-products.html").permitAll()
                        .anyRequest().authenticated()
                )

                // to add jwtAuthFilter before doing UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                // put username and password in httpheader in every request for authentification
                // default approach
                //.httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // so that we can perform manual log-in later
    // authenticationManager.authenticate(...)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // hashing with salt
    }
}
