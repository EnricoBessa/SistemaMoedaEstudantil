package com.moedaestudantil.repository;

import com.moedaestudantil.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    List<Transacao> findByAlunoCpf(String alunoCpf);
    List<Transacao> findByProfessorCpf(String professorCpf);
}