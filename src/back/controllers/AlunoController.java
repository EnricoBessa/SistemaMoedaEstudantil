package com.moedaestudantil.controller;

import com.moedaestudantil.model.Aluno;
import com.moedaestudantil.repository.AlunoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alunos")
@CrossOrigin(origins = "*")
public class AlunoController {

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // CREATE / CADASTRAR
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Aluno aluno) {
        if (alunoRepository.existsById(aluno.getCpf())) {
            return ResponseEntity.badRequest().body("Já existe um aluno cadastrado com este CPF.");
        }
        aluno.setSaldoMoedas(0); // Inicializa com saldo zerado
        aluno.setSenha(passwordEncoder.encode(aluno.getSenha()));
        return ResponseEntity.ok(alunoRepository.save(aluno));
    }

    // READ / CONSULTAR TODOS
    @GetMapping
    public List<Aluno> listar() {
        return alunoRepository.findAll();
    }

    // READ / CONSULTAR UM
    @GetMapping("/{cpf}")
    public ResponseEntity<Aluno> buscar(@PathVariable String cpf) {
        return alunoRepository.findById(cpf)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // UPDATE / ALTERAR
    @PutMapping("/{cpf}")
    public ResponseEntity<?> atualizar(@PathVariable String cpf, @RequestBody Aluno dadosNovos) {
        return alunoRepository.findById(cpf)
                .map(aluno -> {
                    aluno.setNome(dadosNovos.getNome());
                    aluno.setEmail(dadosNovos.getEmail());
                    aluno.setEndereco(dadosNovos.getEndereco());
                    aluno.setCurso(dadosNovos.getCurso());
                    return ResponseEntity.ok(alunoRepository.save(aluno));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE / EXCLUIR
    @DeleteMapping("/{cpf}")
    public void deletar(@PathVariable String cpf) {
        alunoRepository.deleteById(cpf);
    }
}
