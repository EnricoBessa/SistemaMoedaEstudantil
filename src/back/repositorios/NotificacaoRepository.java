package com.moedaestudantil.repository;

import com.moedaestudantil.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByAlunoCpfOrderByDataCriacaoDesc(String alunoCpf);
    long countByAlunoCpfAndLidaFalse(String alunoCpf);
}
