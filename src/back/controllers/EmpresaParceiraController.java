package com.moedaestudantil.controller;

import com.moedaestudantil.model.EmpresaParceira;
import com.moedaestudantil.repository.EmpresaParceiraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/empresas")
@CrossOrigin(origins = "*")
public class EmpresaParceiraController {

    @Autowired
    private EmpresaParceiraRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody EmpresaParceira empresa) {
        if (empresaRepository.existsById(empresa.getCnpj())) {
            return ResponseEntity.badRequest().body("Já existe uma empresa cadastrada com este CNPJ.");
        }
        // Conta criada via Google não define senha própria; gera uma aleatória só pra
        // satisfazer a coluna NOT NULL (o login dessa conta é sempre via Google).
        String senha = (empresa.getSenha() == null || empresa.getSenha().isBlank())
                ? UUID.randomUUID().toString()
                : empresa.getSenha();
        empresa.setSenha(passwordEncoder.encode(senha));
        return ResponseEntity.ok(empresaRepository.save(empresa));
    }

    @GetMapping
    public List<EmpresaParceira> listar() {
        return empresaRepository.findAll();
    }

    @GetMapping("/{cnpj}")
    public ResponseEntity<EmpresaParceira> buscar(@PathVariable String cnpj) {
        return empresaRepository.findById(cnpj)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{cnpj}")
    public void deletar(@PathVariable String cnpj) {
        empresaRepository.deleteById(cnpj);
    }
}
