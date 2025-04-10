package ru.emiren.tg_news.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers(HttpMethod.DELETE).hasRole(ADMIN_ACCESS)
//                        .requestMatchers("/admin/**").hasRole(ADMIN_ACCESS)
                        .requestMatchers("/sql/**").permitAll()
                        .requestMatchers("/api/**").permitAll()
//                        .requestMatchers("/api/admin/**").hasRole(ADMIN_ACCESS)
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/thirdParty/", "favicon.ico",
                                "/resources/css/**", "/resources/js/**", "/resources/img/**", "/resources/thirdParty/**").permitAll()
                        .requestMatchers("/sql/**").permitAll()
                        .anyRequest().permitAll())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new MaxRequestFilter(), CsrfFilter.class)
        ;
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
