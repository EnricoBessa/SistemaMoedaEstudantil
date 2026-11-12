package com.moedaestudantil.config;

import com.moedaestudantil.model.*;
import com.moedaestudantil.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Popula o banco com dados de demonstração ao subir a aplicação, para que o
 * front-end tenha algo para mostrar sem exigir cadastro manual antes.
 * Login de todos os usuários de demonstração: senha "123456".
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final InstituicaoRepository instituicaoRepository;
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final EmpresaParceiraRepository empresaRepository;
    private final VantagemRepository vantagemRepository;
    private final TransacaoRepository transacaoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(InstituicaoRepository instituicaoRepository,
                       AlunoRepository alunoRepository,
                       ProfessorRepository professorRepository,
                       EmpresaParceiraRepository empresaRepository,
                       VantagemRepository vantagemRepository,
                       TransacaoRepository transacaoRepository,
                       PasswordEncoder passwordEncoder) {
        this.instituicaoRepository = instituicaoRepository;
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.empresaRepository = empresaRepository;
        this.vantagemRepository = vantagemRepository;
        this.transacaoRepository = transacaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (instituicaoRepository.count() > 0) {
            return; // já populado
        }

        Instituicao puc = new Instituicao();
        puc.setNome("PUC Minas");
        instituicaoRepository.save(puc);

        Instituicao ufmg = new Instituicao();
        ufmg.setNome("UFMG");
        instituicaoRepository.save(ufmg);

        Instituicao cefet = new Instituicao();
        cefet.setNome("Cefet-MG");
        instituicaoRepository.save(cefet);

        Professor professor = new Professor();
        professor.setCpf("11111111111");
        professor.setNome("Ricardo Alves");
        professor.setEmail("ricardo.alves@puc.br");
        professor.setSenha(passwordEncoder.encode("123456"));
        professor.setDepartamento("Engenharia de Software");
        professor.setSaldoMoedas(590);
        professor.setInstituicao(puc);
        professorRepository.save(professor);

        Aluno aluna = new Aluno();
        aluna.setCpf("22222222222");
        aluna.setNome("Camila Duarte");
        aluna.setEmail("camila.duarte@puc.br");
        aluna.setSenha(passwordEncoder.encode("123456"));
        aluna.setRg("MG1234567");
        aluna.setEndereco("Rua das Acácias, 120 - Belo Horizonte/MG");
        aluna.setCurso("Engenharia de Software");
        aluna.setSaldoMoedas(1240);
        aluna.setInstituicao(puc);
        alunoRepository.save(aluna);

        EmpresaParceira restaurante = new EmpresaParceira();
        restaurante.setCnpj("11222333000144");
        restaurante.setNome("Restaurante Universitário");
        restaurante.setEmail("contato@ru.com.br");
        restaurante.setSenha(passwordEncoder.encode("123456"));
        empresaRepository.save(restaurante);

        Vantagem desconto = new Vantagem();
        desconto.setDescricao("Desconto 15% no RU");
        desconto.setCustoMoedas(80);
        desconto.setEmpresaParceira(restaurante);
        vantagemRepository.save(desconto);

        Vantagem kit = new Vantagem();
        kit.setDescricao("Kit de material gráfico");
        kit.setCustoMoedas(120);
        kit.setEmpresaParceira(restaurante);
        vantagemRepository.save(kit);

        Transacao reconhecimento = new Transacao();
        reconhecimento.setProfessor(professor);
        reconhecimento.setAluno(aluna);
        reconhecimento.setQuantidade(150);
        reconhecimento.setMotivo("Ótima participação na apresentação da Sprint 02");
        transacaoRepository.save(reconhecimento);
    }
}
