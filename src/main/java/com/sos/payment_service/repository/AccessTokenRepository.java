package com.sos.payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sos.payment_service.models.AccessToken;
import java.util.Optional;


@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
 Optional<AccessToken> findByToken(String token);

}
