package de.yiluo.store.security;

import de.yiluo.store.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// when to do this filter is defined in SecurityConfig
@Component
// OncePerRequestFilter means filter only once for each request
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final TokenBlacklist tokenBlacklist;

    // @Lazy let JwtAuthenticationFilter finish initializing without waiting for UserService
    public JwtAuthenticationFilter(JwtService jwtService, @Lazy UserService userService, TokenBlacklist tokenBlacklist) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Override
    // overriding method from OncePerRequestFilter
    protected void doFilterInternal(
            // all 3 provided by servlet container
            // servlet filter request before it reach controller
            // so annotations like @RequestHeader("Authorization") cannot be used here
            // @NonNull means they will not be null
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException { // let caller of the method handle the error instead of method itself handling

        // for public paths
        String path = request.getRequestURI();
        if (path.equals("/api/users/register") || path.equals("/api/users/login") || path.endsWith(".html")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // check if request has jwt token
        // header starting with Bearer means request might have some kind of token
        // but should be checked in detail later
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // pass request and response obj on to rest of the filters
            // no jwt token, pass on to next filter
            filterChain.doFilter(request, response);
            return;
        }

        // to start with eyJhbG... in "Bearer eyJhbGciOiJIUzI1NiIsInR5..." (e.g.)
        jwt = authHeader.substring(7);
        // Block processing if the token has been recorded in the blacklist
        if (tokenBlacklist.isBlacklisted(jwt)) {
            // status is in response header
            // write is for response body
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token is invalidated (Logged Out).");
            return;
        }

        username = jwtService.extractUsername(jwt);
        //  == null means use this filter only when not yet having Authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userService.loadUserByUsername(username);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // normally UsernamePasswordAuthenticationToken is created by UsernamePasswordAuthenticationFilter
                // but now jwt token is ok, so jwt filter create the UsernamePasswordAuthenticationToken instead
                // so that the request does not need to go through the following UsernamePasswordAuthenticationFilter
                // UsernamePasswordAuthenticationToken is for registering that identity inside server's memory for the duration of the request
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // contain credential, but no need to check password bc using jwt
                        userDetails.getAuthorities()
                );
                // add ip address and session id to the UsernamePasswordAuthenticationToken
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}