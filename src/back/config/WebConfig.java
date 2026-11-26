package com.moedaestudantil.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Expõe a pasta de upload de fotos de vantagem (fora do classpath, para sobreviver a
 * rebuilds) como recurso estático em /uploads/**.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String diretorioUpload;

    public WebConfig(@Value("${app.upload.diretorio}") String diretorioUpload) {
        this.diretorioUpload = diretorioUpload;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String caminhoAbsoluto = new File(diretorioUpload).getAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + caminhoAbsoluto + File.separator);
    }
}
