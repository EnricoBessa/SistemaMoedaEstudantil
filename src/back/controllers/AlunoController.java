package com.moedaestudantil.controller;

import com.moedaestudantil.model.Aluno;
import com.moedaestudantil.repository.AlunoRepository;
import com.moedaestudantil.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alunos")
@CrossOrigin(origins = "*")
public class AlunoController {

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UploadService uploadService;

    // CREATE / CADASTRAR
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Aluno aluno) {
        if (alunoRepository.existsById(aluno.getCpf())) {
            return ResponseEntity.badRequest().body("Já existe um aluno cadastrado com este CPF.");
        }
        aluno.setSaldoMoedas(0); // Inicializa com saldo zerado
        // Conta criada via Google não define senha própria; gera uma aleatória só pra
        // satisfazer a coluna NOT NULL (o login dessa conta é sempre via Google).
        String senha = (aluno.getSenha() == null || aluno.getSenha().isBlank())
                ? UUID.randomUUID().toString()
                : aluno.getSenha();
        aluno.setSenha(passwordEncoder.encode(senha));
        return ResponseEntity.ok(alunoRepository.save(aluno));
    }

    // UPLOAD DA FOTO DE PERFIL (o próprio aluno logado)
    @PostMapping("/{cpf}/avatar")
    public ResponseEntity<?> uploadAvatar(@PathVariable String cpf, @RequestParam("arquivo") MultipartFile arquivo,
                                           Principal principal) {
        if (principal == null || !cpf.equals(principal.getName())) {
            return ResponseEntity.status(403).body("Você só pode alterar a sua própria foto de perfil.");
        }
        Aluno aluno = alunoRepository.findById(cpf).orElse(null);
        if (aluno == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            aluno.setFotoPerfil(uploadService.salvarAvatar(arquivo));
        } catch (UploadService.ArquivoInvalidoException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        aluno.setSenha(null);
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
