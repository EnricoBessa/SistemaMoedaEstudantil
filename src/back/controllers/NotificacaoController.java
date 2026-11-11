package com.moedaestudantil.controller;

import com.moedaestudantil.model.Notificacao;
import com.moedaestudantil.repository.NotificacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificacoes")
@CrossOrigin(origins = "*")
public class NotificacaoController {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    // LISTA AS NOTIFICAÇÕES DO ALUNO (mais recentes primeiro)
    @GetMapping("/aluno/{cpf}")
    public List<Notificacao> listarDoAluno(@PathVariable String cpf) {
        return notificacaoRepository.findByAlunoCpfOrderByDataCriacaoDesc(cpf);
    }

    // CONTADOR DE NÃO LIDAS, PARA O BADGE DO SINO
    @GetMapping("/aluno/{cpf}/nao-lidas")
    public Map<String, Long> contarNaoLidas(@PathVariable String cpf) {
        return Map.of("naoLidas", notificacaoRepository.countByAlunoCpfAndLidaFalse(cpf));
    }

    // MARCA UMA NOTIFICAÇÃO COMO LIDA
    @PatchMapping("/{id}/ler")
    public ResponseEntity<?> marcarComoLida(@PathVariable Long id) {
        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada!"));

        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);

        return ResponseEntity.ok(notificacao);
    }
}
