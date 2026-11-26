package com.moedaestudantil.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of("image/png", "image/jpeg", "image/webp");
    private static final long TAMANHO_MAXIMO_BYTES = 5L * 1024 * 1024;

    private final Path diretorioUpload;

    public UploadService(@Value("${app.upload.diretorio}") String diretorioUpload) {
        this.diretorioUpload = Path.of(diretorioUpload);
    }

    public static class ArquivoInvalidoException extends RuntimeException {
        public ArquivoInvalidoException(String mensagem) { super(mensagem); }
    }

    /** Salva a foto de perfil em disco com um nome gerado e devolve a URL pública (/uploads/...). */
    public String salvarAvatar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ArquivoInvalidoException("Selecione um arquivo de imagem.");
        }
        if (!TIPOS_PERMITIDOS.contains(arquivo.getContentType())) {
            throw new ArquivoInvalidoException("Formato inválido. Envie uma imagem PNG, JPG ou WEBP.");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new ArquivoInvalidoException("Imagem maior que 5MB.");
        }

        String extensao = switch (arquivo.getContentType()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        String nomeArquivo = UUID.randomUUID() + extensao;

        try {
            Files.createDirectories(diretorioUpload);
            Files.copy(arquivo.getInputStream(), diretorioUpload.resolve(nomeArquivo));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao salvar a imagem.", e);
        }

        return "/uploads/" + nomeArquivo;
    }
}
