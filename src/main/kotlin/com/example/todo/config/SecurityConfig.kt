package com.example.todo.config

import com.example.todo.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity
class SecurityConfig(private val jwtAuthenticationFilter: JwtAuthenticationFilter) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
                // REST API: stateless JWT
                .csrf { it.disable() }
                .cors(Customizer.withDefaults())
                .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
                .authorizeHttpRequests { auth ->
                    auth.requestMatchers(
                                    "/api/auth/register",
                                    "/api/auth/login",
                                    "/api/auth/refresh",
                                    "/api/auth/logout",
                                    "/api/auth/verify-email",
                                    "/api/auth/resend-verification",
                                    "/api/auth/forgot-password",
                                    "/api/auth/reset-password"
                            )
                            .permitAll()

                            // health/docs (optional; remove if not needed)
                            .requestMatchers("/actuator/health", "/actuator/info")
                            .permitAll()
                            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                            .permitAll()

                            // everything else requires JWT
                            .anyRequest()
                            .authenticated()
                }

                // JWT filter before username/password auth filter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter::class.java
                )

        return http.build()
    }
}
