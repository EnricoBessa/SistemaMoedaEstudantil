package com.moedaestudantil.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_vantagem")
public class Vantagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String descricao;
    private String foto;
    private int custoMoedas;

    @ManyToOne
    @JoinColumn(name = "empresa_cnpj")
    private EmpresaParceira empresaParceira;

    public Vantagem() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }
    public int getCustoMoedas() { return custoMoedas; }
    public void setCustoMoedas(int custoMoedas) { this.custoMoedas = custoMoedas; }
    public EmpresaParceira getEmpresaParceira() { return empresaParceira; }
    public void setEmpresaParceira(EmpresaParceira empresaParceira) { this.empresaParceira = empresaParceira; }
}