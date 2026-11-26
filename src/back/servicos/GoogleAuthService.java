package com.moedaestudantil.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Valida o ID token do "Sign in with Google" (Google Identity Services) chamando o
 * endpoint tokeninfo do Google, que confere a assinatura e devolve os claims. Simples
 * e sem dependências extras; em produção com tráfego alto, o ideal seria trocar por
 * verificação local com a biblioteca google-api-client (cache das chaves públicas).
 */
@Service
public class GoogleAuthService {

    private static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private final RestTemplate restTemplate = new RestTemplate();
    private final String clientIdEsperado;

    public GoogleAuthService(@Value("${app.google.client-id}") String clientIdEsperado) {
        this.clientIdEsperado = clientIdEsperado;
    }

    public record PerfilGoogle(String googleId, String email, String nome) {}

    public static class TokenGoogleInvalidoException extends RuntimeException {
        public TokenGoogleInvalidoException(String mensagem) { super(mensagem); }
    }

    public PerfilGoogle validar(String credential) {
        if (clientIdEsperado == null || clientIdEsperado.isBlank()) {
            throw new TokenGoogleInvalidoException("Login com Google não está configurado no servidor.");
        }

        Map<String, String> claims;
        try {
            claims = restTemplate.getForObject(TOKENINFO_URL + credential, Map.class);
        } catch (RestClientException e) {
            throw new TokenGoogleInvalidoException("Não foi possível validar o login do Google.");
        }

        if (claims == null || !clientIdEsperado.equals(claims.get("aud"))) {
            throw new TokenGoogleInvalidoException("Token do Google inválido para esta aplicação.");
        }
        if (!"true".equals(claims.get("email_verified"))) {
            throw new TokenGoogleInvalidoException("O e-mail da conta Google não está verificado.");
        }

        return new PerfilGoogle(claims.get("sub"), claims.get("email"), claims.get("name"));
    }
}
