package com.moedaestudantil.controller;

import com.moedaestudantil.model.*;
import com.moedaestudantil.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transacoes")
@CrossOrigin(origins = "*")
public class TransacaoController {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private VantagemRepository vantagemRepository;

    // 1. ENVIO DE MOEDAS (Professor -> Aluno)
    @PostMapping("/enviar")
    public ResponseEntity<String> enviarMoedas(@RequestParam String professorCpf, 
                                               @RequestParam String alunoCpf, 
                                               @RequestParam int quantidade, 
                                               @RequestParam String motivo) {
        
        Professor professor = professorRepository.findById(professorCpf)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado!"));
        
        Aluno aluno = alunoRepository.findById(alunoCpf)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado!"));

        if (professor.getSaldoMoedas() < quantidade) {
            return ResponseEntity.badRequest().body("Erro: Saldo insuficiente de moedas!");
        }

        // Atualiza saldos
        professor.setSaldoMoedas(professor.getSaldoMoedas() - quantidade);
        aluno.setSaldoMoedas(aluno.getSaldoMoedas() + quantidade);

        professorRepository.save(professor);
        alunoRepository.save(aluno);

        // Cria a transação
        Transacao transacao = new Transacao();
        transacao.setProfessor(professor);
        transacao.setAluno(aluno);
        transacao.setQuantidade(quantidade);
        transacao.setMotivo(motivo);

        transacaoRepository.save(transacao);

        return ResponseEntity.ok("Moedas enviadas com sucesso! Notificação por email disparada para " + aluno.getEmail());
    }

    // 2. RESGATE DE VANTAGEM (Aluno -> Empresa Parceira)
    @PostMapping("/resgatar")
    public ResponseEntity<String> resgatarVantagem(@RequestParam String alunoCpf, 
                                                   @RequestParam Long vantagemId) {
        
        Aluno aluno = alunoRepository.findById(alunoCpf)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado!"));
        
        Vantagem vantagem = vantagemRepository.findById(vantagemId)
                .orElseThrow(() -> new RuntimeException("Vantagem não encontrada!"));

        if (aluno.getSaldoMoedas() < vantagem.getCustoMoedas()) {
            return ResponseEntity.badRequest().body("Erro: Saldo insuficiente para resgatar esta vantagem!");
        }

        // Desconta as moedas do aluno
        aluno.setSaldoMoedas(aluno.getSaldoMoedas() - vantagem.getCustoMoedas());
        alunoRepository.save(aluno);

        // Gera código de cupom único
        String codigoCupom = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Registra a transação de resgate
        Transacao transacao = new Transacao();
        transacao.setAluno(aluno);
        transacao.setVantagem(vantagem);
        transacao.setQuantidade(vantagem.getCustoMoedas());
        transacao.setCodigoCupom(codigoCupom);
        transacao.setMotivo("Resgate de vantagem: " + vantagem.getDescricao());

        transacaoRepository.save(transacao);

        return ResponseEntity.ok("Vantagem resgatada com sucesso! Código do cupom: " + codigoCupom + 
                                 " (E-mails enviados ao aluno e à empresa parceira).");
    }
}