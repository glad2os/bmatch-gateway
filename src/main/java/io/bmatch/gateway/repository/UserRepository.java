package io.bmatch.gateway.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import io.bmatch.gateway.model.User;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Mono<UserDetails> findByEmail(String email);

    Mono<UserDetails> findByUsername(String username);
}