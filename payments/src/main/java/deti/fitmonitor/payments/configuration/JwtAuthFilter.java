package deti.fitmonitor.payments.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import deti.fitmonitor.payments.services.JwtUtilService;

import org.springframework.security.core.authority.SimpleGrantedAuthority;


@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtilService jwtUtil;

    @Autowired
    public JwtAuthFilter(@Lazy JwtUtilService jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * This method is called for each request that comes to the server
     * @param request
     * @param response
     * @param filterChain
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Get the token from the header
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        List<String> roles = null;

        // Check if the token is not null and starts with Bearer
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);

            try {
                // Extract roles from the token
                roles = jwtUtil.extractRoles(token);

                if (jwtUtil.validateToken(token) && roles != null && !roles.isEmpty()) {

                    String usersub = jwtUtil.extractUserSub(token);

                    // Convert roles into a list of GrantedAuthority
                    List<GrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // Add prefix here
                            .collect(Collectors.toList());
                
                    // Create UsernamePasswordAuthenticationToken with authorities
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            usersub, // principal
                            null, // credentials
                            authorities // authorities
                    );
                
                    // Set the authentication details
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                    // Set the authenticated user in the security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                
            } catch (Exception e) {
                // If the token is invalid or an exception occurs, clear the security context
                SecurityContextHolder.clearContext();
                e.printStackTrace();
            }
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}