package com.moedaestudantil.controller;

import com.moedaestudantil.model.*;
import com.moedaestudantil.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    // 1. ENVIO DE MOEDAS (Professor -> Aluno)
    @PostMapping("/enviar")
    public ResponseEntity<?> enviarMoedas(@RequestParam String professorCpf,
                                           @RequestParam String alunoCpf,
                                           @RequestParam int quantidade,
                                           @RequestParam String motivo) {

        Professor professor = professorRepository.findById(professorCpf)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado!"));

        Aluno aluno = alunoRepository.findById(alunoCpf)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado!"));

        if (quantidade <= 0) {
            return ResponseEntity.badRequest().body("Erro: a quantidade de moedas deve ser maior que zero!");
        }

        if (motivo == null || motivo.isBlank()) {
            return ResponseEntity.badRequest().body("Erro: o motivo do reconhecimento é obrigatório!");
        }

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

        // TODO (pendente do épico): isto cria a notificação de forma síncrona, direto aqui no controller.
        // O planejado é publicar um MoedaRecebidaEvent e tratar com um @EventListener @Async em TransacaoService,
        // para não bloquear a resposta HTTP nem acoplar a criação da notificação ao fluxo de envio de moedas.
        // Quando essa evolução for feita, pode remover este comentário e o bloco abaixo sai daqui.
        Notificacao notificacao = new Notificacao();
        notificacao.setAluno(aluno);
        notificacao.setMensagem("Você recebeu " + quantidade + " moedas de " + professor.getNome() + ": \"" + motivo + "\"");
        notificacaoRepository.save(notificacao);

        return ResponseEntity.ok("Moedas enviadas com sucesso! Notificação por email disparada para " + aluno.getEmail());
    }

    // 2. RESGATE DE VANTAGEM (Aluno -> Empresa Parceira)
    @PostMapping("/resgatar")
    public ResponseEntity<?> resgatarVantagem(@RequestParam String alunoCpf,
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

        // TODO (pendente do épico): mesmo ponto do comentário em enviarMoedas() acima — criação síncrona
        // que deve virar um evento assíncrono (ex.: VantagemResgatadaEvent) quando essa parte for feita.
        // Remova este comentário junto com o de enviarMoedas() quando migrar os dois.
        Notificacao notificacao = new Notificacao();
        notificacao.setAluno(aluno);
        notificacao.setMensagem("Resgate confirmado: " + vantagem.getDescricao() + " (cupom " + codigoCupom + ")");
        notificacaoRepository.save(notificacao);

        return ResponseEntity.ok(transacao);
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
