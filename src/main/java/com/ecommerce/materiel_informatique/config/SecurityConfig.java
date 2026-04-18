package com.ecommerce.materiel_informatique.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 1. Pages publiques accessibles à tous (Catalogue, Détails produit, Login)
                    .requestMatchers("/", "/search", "/categorie/**", "/produit/**", "/cart/**", "/login", "/signup", "/css/**", "/js/**").permitAll()

                        // 2. Actions nécessitant d'être connecté (Panier, Checkout, Profil)
                        .requestMatchers("/panier", "/checkout", "/compte/**").authenticated()

                        // 3. Dashboards protégés par rôles
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/gerant/**").hasAnyRole("ADMIN", "GERANT")

                        // 4. Tout le reste nécessite une authentification
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // FORCE L'UTILISATION DE TA PAGE DE LOGIN "APPLE"
                        .loginPage("/login")
                        // Gère la redirection selon le rôle après une connexion réussie
                        .successHandler((request, response, authentication) -> {
                            var roles = authentication.getAuthorities().toString();
                            if (roles.contains("ROLE_ADMIN")) {
                                response.sendRedirect("/admin/dashboard");
                            } else if (roles.contains("ROLE_GERANT")) {
                                response.sendRedirect("/gerant/dashboard");
                            } else {
                                response.sendRedirect("/");
                            }
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        // Redirige vers ta page de login avec un message de succès après déconnexion
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        // Désactivation temporaire pour faciliter le développement
        http.csrf(csrf -> csrf.disable());
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

        manager.createUser(User.withUsername("admin")
                .password("{noop}admin123")
                .roles("ADMIN")
                .build());

        manager.createUser(User.withUsername("gerant")
                .password("{noop}gerant123")
                .roles("GERANT")
                .build());

        manager.createUser(User.withUsername("asmae")
                .password("{noop}client123")
                .roles("CLIENT")
                .build());

        return manager;
    }
}