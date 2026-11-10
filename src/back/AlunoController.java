package com.moedaestudantil.controller;

import com.moedaestudantil.model.Aluno;
import com.moedaestudantil.repository.AlunoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alunos")
@CrossOrigin(origins = "*")
public class AlunoController {

    @Autowired
    private AlunoRepository alunoRepository;

    // CREATE / CADASTRAR
    @PostMapping
    public Aluno cadastrar(@RequestBody Aluno aluno) {
        aluno.setSaldoMoedas(0); // Inicializa com saldo zerado
        return alunoRepository.save(aluno);
    }

    // READ / CONSULTAR TODOS
    @GetMapping
    public List<Aluno> listar() {
        return alunoRepository.findAll();
    }

    // UPDATE / ALTERAR
    @PutMapping("/{cpf}")
    public Aluno atualizar(@PathVariable String cpf, @RequestBody Aluno dadosNovos) {
        Aluno aluno = alunoRepository.findById(cpf).orElseThrow(() -> new RuntimeException("Aluno não encontrado!"));
        aluno.setNome(dadosNovos.getNome());
        aluno.setEmail(dadosNovos.getEmail());
        aluno.setEndereco(dadosNovos.getEndereco());
        aluno.setCurso(dadosNovos.getCurso());
        return alunoRepository.save(aluno);
    }

    // DELETE / EXCLUIR
    @DeleteMapping("/{cpf}")
    public void deletar(@PathVariable String cpf) {
        alunoRepository.deleteById(cpf);
    }
}