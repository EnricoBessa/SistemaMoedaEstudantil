package com.moedaestudantil.service;

import com.moedaestudantil.event.MoedaRecebidaEvent;
import com.moedaestudantil.event.VantagemResgatadaEvent;
import com.moedaestudantil.model.*;
import com.moedaestudantil.repository.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final VantagemRepository vantagemRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TransacaoService(TransacaoRepository transacaoRepository,
                             ProfessorRepository professorRepository,
                             AlunoRepository alunoRepository,
                             VantagemRepository vantagemRepository,
                             ApplicationEventPublisher eventPublisher) {
        this.transacaoRepository = transacaoRepository;
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
        this.vantagemRepository = vantagemRepository;
        this.eventPublisher = eventPublisher;
    }

    public static class SaldoInsuficienteException extends RuntimeException {
        public SaldoInsuficienteException(String mensagem) { super(mensagem); }
    }

    public static class DadosInvalidosException extends RuntimeException {
        public DadosInvalidosException(String mensagem) { super(mensagem); }
    }

    public Transacao enviarMoedas(String professorCpf, String alunoCpf, int quantidade, String motivo) {
        Professor professor = professorRepository.findById(professorCpf)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado!"));

        Aluno aluno = alunoRepository.findById(alunoCpf)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado!"));

        if (quantidade <= 0) {
            throw new DadosInvalidosException("Erro: a quantidade de moedas deve ser maior que zero!");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new DadosInvalidosException("Erro: o motivo do reconhecimento é obrigatório!");
        }
        if (professor.getSaldoMoedas() < quantidade) {
            throw new SaldoInsuficienteException("Erro: Saldo insuficiente de moedas!");
        }

        professor.setSaldoMoedas(professor.getSaldoMoedas() - quantidade);
        aluno.setSaldoMoedas(aluno.getSaldoMoedas() + quantidade);
        professorRepository.save(professor);
        alunoRepository.save(aluno);

        Transacao transacao = new Transacao();
        transacao.setProfessor(professor);
        transacao.setAluno(aluno);
        transacao.setQuantidade(quantidade);
        transacao.setMotivo(motivo);
        transacao = transacaoRepository.save(transacao);

        eventPublisher.publishEvent(new MoedaRecebidaEvent(aluno, professor, quantidade, motivo));

        return transacao;
    }

    public Transacao resgatarVantagem(String alunoCpf, Long vantagemId) {
        Aluno aluno = alunoRepository.findById(alunoCpf)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado!"));

        Vantagem vantagem = vantagemRepository.findById(vantagemId)
                .orElseThrow(() -> new RuntimeException("Vantagem não encontrada!"));

        if (aluno.getSaldoMoedas() < vantagem.getCustoMoedas()) {
            throw new SaldoInsuficienteException("Erro: Saldo insuficiente para resgatar esta vantagem!");
        }

        aluno.setSaldoMoedas(aluno.getSaldoMoedas() - vantagem.getCustoMoedas());
        alunoRepository.save(aluno);

        String codigoCupom = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Transacao transacao = new Transacao();
        transacao.setAluno(aluno);
        transacao.setVantagem(vantagem);
        transacao.setQuantidade(vantagem.getCustoMoedas());
        transacao.setCodigoCupom(codigoCupom);
        transacao.setMotivo("Resgate de vantagem: " + vantagem.getDescricao());
        transacao = transacaoRepository.save(transacao);

        eventPublisher.publishEvent(new VantagemResgatadaEvent(aluno, vantagem, codigoCupom));

        return transacao;
    }
}
