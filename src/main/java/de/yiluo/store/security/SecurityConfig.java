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

// cors filter
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration // @Configuration/@Bean to prepare bean
@EnableWebSecurity // turn off default security setting and apply custom
public class SecurityConfig {
    // HttpSecurity http is temporary configuration builder object
    // http and jwtAuthFilter are Method Parameter Injection
    // @Bean turn output of a method into a bean
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        // method chaining: same as doing the methods one by one
        http
                // apply CORS rules using a bean provided in this file
                .cors(Customizer.withDefaults())
                // disabled bc cross site request will not have jwt token in header
                // but jwt not immune to xss since i store it in session storage
                .csrf(csrf -> csrf.disable())
                // so that server ram does not remember anything (stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // enforced by AuthorizationFilter
                .authorizeHttpRequests(auth -> auth
                        // User
                        .requestMatchers("/api/users/register").permitAll()
                        .requestMatchers("/api/users/login").permitAll()
                        .requestMatchers("/api/users/manage/**").hasRole("ADMIN")// automatically searches for "ROLE_ADMIN"

                        // Product
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        // Cart
                        .requestMatchers("/api/cart/**").authenticated()

                        // Order
                        .requestMatchers("/api/orders/**").authenticated()

                        // Payment
                        // webhook must be public for Stripe to reach it
                        .requestMatchers("/api/payments/webhook").permitAll()
                        .requestMatchers("/api/payments/create-intent").authenticated()

                        // for static folder files, will empty the folder in the end, testing just for now
                        .requestMatchers("/**").permitAll()

                        .anyRequest().authenticated()
                )

                // to add jwtAuthFilter before doing UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
                // so the order is 1 jwtFitler 2 UsernamePasswordAuthenticationFilter 3 AuthorizationFilter

                // put username and password in httpheader in every request for authentification
                // default approach
                //.httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // it handles first ever login when no jwt token yet
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // hashing with salt
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://www.yiluo.de"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        // apply the rules above
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
