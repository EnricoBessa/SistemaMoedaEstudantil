package com.moedaestudantil.repository;

import com.moedaestudantil.model.EmpresaParceira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaParceiraRepository extends JpaRepository<EmpresaParceira, String> {
    Optional<EmpresaParceira> findByEmail(String email);
}
