package com.moedaestudantil.controller;

import com.moedaestudantil.model.EmpresaParceira;
import com.moedaestudantil.repository.EmpresaParceiraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@CrossOrigin(origins = "*")
public class EmpresaParceiraController {

    @Autowired
    private EmpresaParceiraRepository empresaRepository;

    @PostMapping
    public EmpresaParceira cadastrar(@RequestBody EmpresaParceira empresa) {
        return empresaRepository.save(empresa);
    }

    @GetMapping
    public List<EmpresaParceira> listar() {
        return empresaRepository.findAll();
    }

    @DeleteMapping("/{cnpj}")
    public void deletar(@PathVariable String cnpj) {
        empresaRepository.deleteById(cnpj);
    }
}