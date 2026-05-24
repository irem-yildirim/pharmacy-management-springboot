package com.pharmacy.config;

import com.pharmacy.entity.User;
import com.pharmacy.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            return new PharmacyUserDetails(user);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.*").permitAll()
                    .requestMatchers("/login", "/api/auth/login").permitAll()
                    .requestMatchers("/api/users/performance").authenticated()
                    .requestMatchers("/api/users/**").hasRole("ADMIN")
                    .requestMatchers("/dashboard/**", "/purchase/**", "/customer/**", "/settings/**").hasAnyRole("ADMIN", "PHARMACIST")
                    .requestMatchers("/api/purchases/**", "/api/brands/**", "/api/categories/**").hasAnyRole("ADMIN", "PHARMACIST")
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/api/auth/login")
                    .successHandler((request, response, authentication) -> {
                        boolean isCashier = authentication.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_CASHIER"));
                        response.sendRedirect(isCashier ? "/pos" : "/dashboard");
                    })
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
            )
            .csrf(csrf -> csrf
                    .ignoringRequestMatchers("/api/**")
            );

        return http.build();
    }
}
