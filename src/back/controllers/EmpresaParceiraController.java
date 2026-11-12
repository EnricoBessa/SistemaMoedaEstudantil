package com.moedaestudantil.controller;

import com.moedaestudantil.model.EmpresaParceira;
import com.moedaestudantil.repository.EmpresaParceiraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        empresa.setSenha(passwordEncoder.encode(empresa.getSenha()));
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
