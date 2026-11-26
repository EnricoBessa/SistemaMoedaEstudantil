package com.moedaestudantil.event;

import com.moedaestudantil.model.Notificacao;
import com.moedaestudantil.repository.NotificacaoRepository;
import com.moedaestudantil.service.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Trata os eventos de transação de forma assíncrona (outra thread), para que a
 * criação da notificação e o envio de e-mail nunca bloqueiem a resposta HTTP do
 * envio/resgate de moedas.
 */
@Component
public class NotificacaoEventListener {

    private final NotificacaoRepository notificacaoRepository;
    private final EmailService emailService;

    public NotificacaoEventListener(NotificacaoRepository notificacaoRepository, EmailService emailService) {
        this.notificacaoRepository = notificacaoRepository;
        this.emailService = emailService;
    }

    @Async
    @EventListener
    public void aoReceberMoedas(MoedaRecebidaEvent evento) {
        String mensagem = "Você recebeu " + evento.getQuantidade() + " moedas de "
                + evento.getProfessor().getNome() + ": \"" + evento.getMotivo() + "\"";

        Notificacao notificacao = new Notificacao();
        notificacao.setAluno(evento.getAluno());
        notificacao.setMensagem(mensagem);
        notificacaoRepository.save(notificacao);

        emailService.enviar(evento.getAluno().getEmail(), "Você recebeu moedas!", mensagem);
    }

    @Async
    @EventListener
    public void aoResgatarVantagem(VantagemResgatadaEvent evento) {
        String mensagemAluno = "Resgate confirmado: " + evento.getVantagem().getDescricao()
                + " (cupom " + evento.getCodigoCupom() + ")";

        Notificacao notificacao = new Notificacao();
        notificacao.setAluno(evento.getAluno());
        notificacao.setMensagem(mensagemAluno);
        notificacaoRepository.save(notificacao);

        emailService.enviar(evento.getAluno().getEmail(), "Cupom de resgate", mensagemAluno
                + "\n\nApresente este código na empresa parceira para confirmar a troca.");

        if (evento.getVantagem().getEmpresaParceira() != null) {
            String mensagemEmpresa = "O aluno " + evento.getAluno().getNome()
                    + " resgatou a vantagem \"" + evento.getVantagem().getDescricao()
                    + "\" com o cupom " + evento.getCodigoCupom() + ". Confira o código antes de liberar o benefício.";
            emailService.enviar(evento.getVantagem().getEmpresaParceira().getEmail(),
                    "Novo resgate para conferência", mensagemEmpresa);
        }
    }
}
