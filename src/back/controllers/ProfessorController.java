package com.moedaestudantil.controller;

import com.moedaestudantil.model.Professor;
import com.moedaestudantil.repository.ProfessorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professores")
@CrossOrigin(origins = "*")
public class ProfessorController {

    @Autowired
    private ProfessorRepository professorRepository;

    // Professores já são pré-cadastrados pela instituição parceira.
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Professor professor) {
        if (professorRepository.existsById(professor.getCpf())) {
            return ResponseEntity.badRequest().body("Já existe um professor cadastrado com este CPF.");
        }
        if (professor.getSaldoMoedas() <= 0) {
            professor.setSaldoMoedas(1000);
        }
        return ResponseEntity.ok(professorRepository.save(professor));
    }

    @GetMapping
    public List<Professor> listar() {
        return professorRepository.findAll();
    }

    @GetMapping("/{cpf}")
    public ResponseEntity<Professor> buscar(@PathVariable String cpf) {
        return professorRepository.findById(cpf)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
