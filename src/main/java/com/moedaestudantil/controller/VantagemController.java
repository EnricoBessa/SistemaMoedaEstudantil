package com.moedaestudantil.controller;

import com.moedaestudantil.model.Vantagem;
import com.moedaestudantil.repository.VantagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    // LISTAR VANTAGENS DISPONÍVEIS (opcionalmente filtradas por empresa)
    @GetMapping
    public List<Vantagem> listar(@RequestParam(required = false) String empresaCnpj) {
        if (empresaCnpj != null && !empresaCnpj.isBlank()) {
            return vantagemRepository.findByEmpresaParceiraCnpj(empresaCnpj);
        }
        return vantagemRepository.findAll();
    }

    // ATUALIZAR VANTAGEM
    @PutMapping("/{id}")
    public ResponseEntity<Vantagem> atualizar(@PathVariable Long id, @RequestBody Vantagem dadosNovos) {
        return vantagemRepository.findById(id)
                .map(vantagem -> {
                    vantagem.setDescricao(dadosNovos.getDescricao());
                    vantagem.setCustoMoedas(dadosNovos.getCustoMoedas());
                    if (dadosNovos.getFoto() != null) {
                        vantagem.setFoto(dadosNovos.getFoto());
                    }
                    return ResponseEntity.ok(vantagemRepository.save(vantagem));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // EXCLUIR VANTAGEM
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        vantagemRepository.deleteById(id);
    }
}
