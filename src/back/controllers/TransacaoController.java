package com.moedaestudantil.controller;

import com.moedaestudantil.model.Transacao;
import com.moedaestudantil.repository.TransacaoRepository;
import com.moedaestudantil.service.TransacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
@CrossOrigin(origins = "*")
public class TransacaoController {

    @Autowired
    private TransacaoService transacaoService;

    @Autowired
    private TransacaoRepository transacaoRepository;

    // 1. ENVIO DE MOEDAS (Professor -> Aluno)
    @PostMapping("/enviar")
    public ResponseEntity<?> enviarMoedas(@RequestParam String professorCpf,
                                           @RequestParam String alunoCpf,
                                           @RequestParam int quantidade,
                                           @RequestParam String motivo) {
        try {
            transacaoService.enviarMoedas(professorCpf, alunoCpf, quantidade, motivo);
            return ResponseEntity.ok("Moedas enviadas com sucesso!");
        } catch (TransacaoService.DadosInvalidosException | TransacaoService.SaldoInsuficienteException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. RESGATE DE VANTAGEM (Aluno -> Empresa Parceira)
    @PostMapping("/resgatar")
    public ResponseEntity<?> resgatarVantagem(@RequestParam String alunoCpf,
                                               @RequestParam Long vantagemId) {
        try {
            Transacao transacao = transacaoService.resgatarVantagem(alunoCpf, vantagemId);
            return ResponseEntity.ok(transacao);
        } catch (TransacaoService.SaldoInsuficienteException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. EXTRATO DO ALUNO
    @GetMapping("/aluno/{cpf}")
    public List<Transacao> extratoAluno(@PathVariable String cpf) {
        return transacaoRepository.findByAlunoCpfOrderByDataDesc(cpf);
    }

    // 4. EXTRATO DO PROFESSOR (envios feitos)
    @GetMapping("/professor/{cpf}")
    public List<Transacao> extratoProfessor(@PathVariable String cpf) {
        return transacaoRepository.findByProfessorCpfOrderByDataDesc(cpf);
    }

    // 5. CUPONS A CONFERIR PELA EMPRESA PARCEIRA
    @GetMapping("/empresa/{cnpj}")
    public List<Transacao> cuponsDaEmpresa(@PathVariable String cnpj) {
        return transacaoRepository.findByVantagemEmpresaParceiraCnpjOrderByDataDesc(cnpj);
    }
}
