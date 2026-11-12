package com.moedaestudantil.controller;

import com.moedaestudantil.model.Aluno;
import com.moedaestudantil.model.EmpresaParceira;
import com.moedaestudantil.model.Professor;
import com.moedaestudantil.repository.AlunoRepository;
import com.moedaestudantil.repository.EmpresaParceiraRepository;
import com.moedaestudantil.repository.ProfessorRepository;
import com.moedaestudantil.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

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
                if (aluno.isPresent() && passwordEncoder.matches(req.senha, aluno.get().getSenha())) {
                    aluno.get().setSenha(null);
                    String token = jwtUtil.gerarToken(aluno.get().getCpf(), "ALUNO");
                    return ResponseEntity.ok(Map.of("tipo", "ALUNO", "usuario", aluno.get(), "token", token));
                }
                break;
            }
            case "PROFESSOR": {
                Optional<Professor> professor = professorRepository.findByEmail(req.email);
                if (professor.isPresent() && passwordEncoder.matches(req.senha, professor.get().getSenha())) {
                    professor.get().setSenha(null);
                    String token = jwtUtil.gerarToken(professor.get().getCpf(), "PROFESSOR");
                    return ResponseEntity.ok(Map.of("tipo", "PROFESSOR", "usuario", professor.get(), "token", token));
                }
                break;
            }
            case "EMPRESA": {
                Optional<EmpresaParceira> empresa = empresaRepository.findByEmail(req.email);
                if (empresa.isPresent() && passwordEncoder.matches(req.senha, empresa.get().getSenha())) {
                    empresa.get().setSenha(null);
                    String token = jwtUtil.gerarToken(empresa.get().getCnpj(), "EMPRESA");
                    return ResponseEntity.ok(Map.of("tipo", "EMPRESA", "usuario", empresa.get(), "token", token));
                }
                break;
            }
            default:
                return ResponseEntity.badRequest().body(Map.of("erro", "Tipo de usuário inválido."));
        }

        return ResponseEntity.status(401).body(Map.of("erro", "E-mail ou senha inválidos."));
    }
}
