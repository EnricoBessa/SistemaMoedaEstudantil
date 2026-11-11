package com.moedaestudantil.controller;

import com.moedaestudantil.model.Instituicao;
import com.moedaestudantil.repository.InstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instituicoes")
@CrossOrigin(origins = "*")
public class InstituicaoController {

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @GetMapping
    public List<Instituicao> listar() {
        return instituicaoRepository.findAll();
    }

    @PostMapping
    public Instituicao cadastrar(@RequestBody Instituicao instituicao) {
        return instituicaoRepository.save(instituicao);
    }
}
