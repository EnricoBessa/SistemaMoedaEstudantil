package com.moedaestudantil.controller;

import com.moedaestudantil.model.Vantagem;
import com.moedaestudantil.repository.VantagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vantagens")
@CrossOrigin(origins = "*")
public class VantagemController {

    @Autowired
    private VantagemRepository vantagemRepository;

    // CADASTRAR VANTAGEM (Empresa Parceira)
    @PostMapping
    public Vantagem cadastrar(@RequestBody Vantagem vantagem) {
        return vantagemRepository.save(vantagem);
    }

    // LISTAR TODAS AS VANTAGENS DISPONÍVEIS
    @GetMapping
    public List<Vantagem> listar() {
        return vantagemRepository.findAll();
    }

    // EXCLUIR VANTAGEM
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        vantagemRepository.deleteById(id);
    }
}