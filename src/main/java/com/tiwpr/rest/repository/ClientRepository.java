package com.tiwpr.rest.repository;

import com.tiwpr.rest.model.dao.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByClientId(Long clientId);

    Page<Client> findAll(Pageable pageable);

    List<Client> findAll();
}
