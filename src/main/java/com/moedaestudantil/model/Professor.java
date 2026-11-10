package com.moedaestudantil.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_professor")
public class Professor {

    @Id
    private String cpf;
    private String nome;
    private String email;
    private String senha;
    private String departamento;
    private int saldoMoedas = 1000; // Começa com 1000 moedas por semestre

    @ManyToOne
    @JoinColumn(name = "instituicao_id")
    private Instituicao instituicao;

    public Professor() {}

    // Getters e Setters
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public int getSaldoMoedas() { return saldoMoedas; }
    public void setSaldoMoedas(int saldoMoedas) { this.saldoMoedas = saldoMoedas; }
    public Instituicao getInstituicao() { return instituicao; }
    public void setInstituicao(Instituicao instituicao) { this.instituicao = instituicao; }
}
