package com.moedaestudantil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SistemaMoedaEstudantilApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaMoedaEstudantilApplication.class, args);
    }
}
