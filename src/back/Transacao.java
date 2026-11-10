package com.moedaestudantil.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "tb_transacao")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantidade;
    private String motivo;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date data = new Date();
    
    private String codigoCupom; // Usado no resgate de vantagens

    @ManyToOne
    @JoinColumn(name = "professor_cpf", nullable = true)
    private Professor professor; // Nulo se for resgate de vantagem

    @ManyToOne
    @JoinColumn(name = "aluno_cpf", nullable = true)
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "vantagem_id", nullable = true)
    private Vantagem vantagem; // Nulo se for envio de moedas do professor

    public Transacao() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }
    public String getCodigoCupom() { return codigoCupom; }
    public void setCodigoCupom(String codigoCupom) { this.codigoCupom = codigoCupom; }
    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public Vantagem getVantagem() { return vantagem; }
    public void setVantagem(Vantagem vantagem) { this.vantagem = vantagem; }
}