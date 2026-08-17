package com.isateca.security;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;

    AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public List<AppUser> list() {
        return appUserRepository.findAll();
    }

    @Transactional
    public AppUser save(AppUser appUser) {
        return appUserRepository.save(appUser);
    }

    @Transactional
    public void delete(AppUser appUser) {
        appUserRepository.delete(appUser);
    }
}
