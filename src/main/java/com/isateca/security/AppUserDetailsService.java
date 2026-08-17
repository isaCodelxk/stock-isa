package com.isateca.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    AppUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        var authority = "ROLE_" + appUser.getRole().getName().toUpperCase().replace(' ', '_');
        return User.withUsername(appUser.getUsername())
                .password(appUser.getPasswordHash())
                .authorities(new SimpleGrantedAuthority(authority))
                .disabled(!appUser.isActive())
                .build();
    }
}
