package ua.nincorporation.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.nincorporation.userservice.model.UserProvider;

import java.util.Optional;

public interface UserProviderRepository extends JpaRepository<UserProvider, Long> {
    Optional<UserProvider> findByProviderAndProviderUserId(String provider, String providerUserId);
    boolean existsByProviderAndProviderUserId(String provider, String providerUserId);
}
