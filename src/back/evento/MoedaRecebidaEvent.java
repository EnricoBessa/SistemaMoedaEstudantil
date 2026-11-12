package com.moedaestudantil.event;

import com.moedaestudantil.model.Aluno;
import com.moedaestudantil.model.Professor;

public class MoedaRecebidaEvent {

    private final Aluno aluno;
    private final Professor professor;
    private final int quantidade;
    private final String motivo;

    public MoedaRecebidaEvent(Aluno aluno, Professor professor, int quantidade, String motivo) {
        this.aluno = aluno;
        this.professor = professor;
        this.quantidade = quantidade;
        this.motivo = motivo;
    }

    public Aluno getAluno() { return aluno; }
    public Professor getProfessor() { return professor; }
    public int getQuantidade() { return quantidade; }
    public String getMotivo() { return motivo; }
}
