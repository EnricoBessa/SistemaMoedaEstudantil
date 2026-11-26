package com.moedaestudantil.controller;

import com.moedaestudantil.model.Aluno;
import com.moedaestudantil.model.EmpresaParceira;
import com.moedaestudantil.model.Professor;
import com.moedaestudantil.repository.AlunoRepository;
import com.moedaestudantil.repository.EmpresaParceiraRepository;
import com.moedaestudantil.repository.ProfessorRepository;
import com.moedaestudantil.security.JwtUtil;
import com.moedaestudantil.service.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private GoogleAuthService googleAuthService;

    @Value("${app.google.client-id}")
    private String googleClientId;

    public static class LoginRequest {
        public String email;
        public String senha;
        public String tipo; // ALUNO | PROFESSOR | EMPRESA
    }

    public static class GoogleLoginRequest {
        public String credential; // ID token do Google Identity Services
        public String tipo; // ALUNO | PROFESSOR | EMPRESA
    }

    // Front-end usa isso pra saber qual Client ID carregar no botão "Entrar com Google".
    @GetMapping("/google/client-id")
    public Map<String, String> clientIdGoogle() {
        return Map.of("clientId", googleClientId == null ? "" : googleClientId);
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginComGoogle(@RequestBody GoogleLoginRequest req) {
        if (req.tipo == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Informe o tipo de usuário (ALUNO, PROFESSOR ou EMPRESA)."));
        }

        GoogleAuthService.PerfilGoogle perfil;
        try {
            perfil = googleAuthService.validar(req.credential);
        } catch (GoogleAuthService.TokenGoogleInvalidoException e) {
            return ResponseEntity.status(401).body(Map.of("erro", e.getMessage()));
        }

        switch (req.tipo.toUpperCase()) {
            case "ALUNO": {
                Optional<Aluno> alunoExistente = alunoRepository.findByEmail(perfil.email());
                if (alunoExistente.isPresent()) {
                    Aluno aluno = alunoExistente.get();
                    if (aluno.getGoogleId() == null) {
                        aluno.setGoogleId(perfil.googleId());
                        alunoRepository.save(aluno);
                    }
                    aluno.setSenha(null);
                    String token = jwtUtil.gerarToken(aluno.getCpf(), "ALUNO");
                    return ResponseEntity.ok(Map.of("tipo", "ALUNO", "usuario", aluno, "token", token));
                }
                return ResponseEntity.ok(Map.of("precisaCompletarCadastro", true, "tipo", "ALUNO", "google", perfil));
            }
            case "PROFESSOR": {
                Optional<Professor> professorExistente = professorRepository.findByEmail(perfil.email());
                if (professorExistente.isPresent()) {
                    Professor professor = professorExistente.get();
                    if (professor.getGoogleId() == null) {
                        professor.setGoogleId(perfil.googleId());
                        professorRepository.save(professor);
                    }
                    professor.setSenha(null);
                    String token = jwtUtil.gerarToken(professor.getCpf(), "PROFESSOR");
                    return ResponseEntity.ok(Map.of("tipo", "PROFESSOR", "usuario", professor, "token", token));
                }
                return ResponseEntity.status(404).body(Map.of("erro",
                        "Nenhum professor cadastrado com este e-mail. Professores são pré-cadastrados pela instituição."));
            }
            case "EMPRESA": {
                Optional<EmpresaParceira> empresaExistente = empresaRepository.findByEmail(perfil.email());
                if (empresaExistente.isPresent()) {
                    EmpresaParceira empresa = empresaExistente.get();
                    if (empresa.getGoogleId() == null) {
                        empresa.setGoogleId(perfil.googleId());
                        empresaRepository.save(empresa);
                    }
                    empresa.setSenha(null);
                    String token = jwtUtil.gerarToken(empresa.getCnpj(), "EMPRESA");
                    return ResponseEntity.ok(Map.of("tipo", "EMPRESA", "usuario", empresa, "token", token));
                }
                return ResponseEntity.ok(Map.of("precisaCompletarCadastro", true, "tipo", "EMPRESA", "google", perfil));
            }
            default:
                return ResponseEntity.badRequest().body(Map.of("erro", "Tipo de usuário inválido."));
        }
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
