package com.moedaestudantil.controller;

import com.moedaestudantil.model.Aluno;
import com.moedaestudantil.model.EmpresaParceira;
import com.moedaestudantil.model.Professor;
import com.moedaestudantil.repository.AlunoRepository;
import com.moedaestudantil.repository.EmpresaParceiraRepository;
import com.moedaestudantil.repository.ProfessorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Autenticação simplificada para fins didáticos: compara e-mail/senha em texto
 * puro contra a tabela do papel informado. Não deve ser usada como referência
 * de segurança em um sistema real (sem hashing, sem sessão/token).
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private EmpresaParceiraRepository empresaRepository;

    public static class LoginRequest {
        public String email;
        public String senha;
        public String tipo; // ALUNO | PROFESSOR | EMPRESA
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req.tipo == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Informe o tipo de usuário (ALUNO, PROFESSOR ou EMPRESA)."));
        }

        switch (req.tipo.toUpperCase()) {
            case "ALUNO": {
                Optional<Aluno> aluno = alunoRepository.findByEmail(req.email);
                if (aluno.isPresent() && aluno.get().getSenha().equals(req.senha)) {
                    aluno.get().setSenha(null);
                    return ResponseEntity.ok(Map.of("tipo", "ALUNO", "usuario", aluno.get()));
                }
                break;
            }
            case "PROFESSOR": {
                Optional<Professor> professor = professorRepository.findByEmail(req.email);
                if (professor.isPresent() && professor.get().getSenha().equals(req.senha)) {
                    professor.get().setSenha(null);
                    return ResponseEntity.ok(Map.of("tipo", "PROFESSOR", "usuario", professor.get()));
                }
                break;
            }
            case "EMPRESA": {
                Optional<EmpresaParceira> empresa = empresaRepository.findByEmail(req.email);
                if (empresa.isPresent() && empresa.get().getSenha().equals(req.senha)) {
                    empresa.get().setSenha(null);
                    return ResponseEntity.ok(Map.of("tipo", "EMPRESA", "usuario", empresa.get()));
                }
                break;
            }
            default:
                return ResponseEntity.badRequest().body(Map.of("erro", "Tipo de usuário inválido."));
        }

        return ResponseEntity.status(401).body(Map.of("erro", "E-mail ou senha inválidos."));
    }
}
