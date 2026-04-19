package com.ecommerce.materiel_informatique.service;

import com.ecommerce.materiel_informatique.model.AppUser;
import com.ecommerce.materiel_informatique.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    @Autowired private AppUserRepository appUserRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        String normalizedLogin = login == null ? "" : login.trim().toLowerCase();
        AppUser user = appUserRepository.findByUsername(normalizedLogin)
                .or(() -> appUserRepository.findByEmail(normalizedLogin))
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + login));
        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole())
                .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedAdmin() {
        appUserRepository.findByUsername("admin").ifPresentOrElse(
            admin -> {
                boolean needsUpdate = !admin.getPassword().startsWith("$2a$") || admin.getRole().startsWith("ROLE_");
                if (needsUpdate) {
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setRole("ADMIN");
                    appUserRepository.save(admin);
                }
            },
            () -> appUserRepository.save(new AppUser("admin", passwordEncoder.encode("admin123"), "ADMIN"))
        );
    }
}
