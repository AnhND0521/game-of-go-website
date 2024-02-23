package io.github.aylesw.igo.repository;

import io.github.aylesw.igo.entity.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
    boolean existsByUsername(String username);
    Account findByUsername(String username);
}
