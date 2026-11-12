package com.moedaestudantil.event;

import com.moedaestudantil.model.Aluno;
import com.moedaestudantil.model.Vantagem;

public class VantagemResgatadaEvent {

    private final Aluno aluno;
    private final Vantagem vantagem;
    private final String codigoCupom;

    public VantagemResgatadaEvent(Aluno aluno, Vantagem vantagem, String codigoCupom) {
        this.aluno = aluno;
        this.vantagem = vantagem;
        this.codigoCupom = codigoCupom;
    }

    public Aluno getAluno() { return aluno; }
    public Vantagem getVantagem() { return vantagem; }
    public String getCodigoCupom() { return codigoCupom; }
}
