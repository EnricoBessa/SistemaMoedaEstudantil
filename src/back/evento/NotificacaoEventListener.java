package com.moedaestudantil.event;

import com.moedaestudantil.model.Notificacao;
import com.moedaestudantil.repository.NotificacaoRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Trata os eventos de transação de forma assíncrona (outra thread), para que a
 * criação da notificação nunca bloqueie a resposta HTTP do envio/resgate de moedas.
 */
@Component
public class NotificacaoEventListener {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoEventListener(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    @Async
    @EventListener
    public void aoReceberMoedas(MoedaRecebidaEvent evento) {
        Notificacao notificacao = new Notificacao();
        notificacao.setAluno(evento.getAluno());
        notificacao.setMensagem("Você recebeu " + evento.getQuantidade() + " moedas de "
                + evento.getProfessor().getNome() + ": \"" + evento.getMotivo() + "\"");
        notificacaoRepository.save(notificacao);
    }

    @Async
    @EventListener
    public void aoResgatarVantagem(VantagemResgatadaEvent evento) {
        Notificacao notificacao = new Notificacao();
        notificacao.setAluno(evento.getAluno());
        notificacao.setMensagem("Resgate confirmado: " + evento.getVantagem().getDescricao()
                + " (cupom " + evento.getCodigoCupom() + ")");
        notificacaoRepository.save(notificacao);
    }
}
