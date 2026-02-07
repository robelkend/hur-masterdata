package com.rsoft.hurmanagement.hurmasterdata.security;

import com.rsoft.hurmanagement.hurmasterdata.entity.Utilisateur;
import com.rsoft.hurmanagement.hurmasterdata.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UtilisateurDetailsService implements UserDetailsService {
    private final UtilisateurRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = repository.findByIdentifiant(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur not found"));
        return new UtilisateurDetails(utilisateur);
    }
}
