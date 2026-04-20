package com.example.backend.scan.service;

import com.example.backend.scan.dto.MobSFScanResponse;
import com.example.backend.scan.dto.MobSFUploadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.File;
import java.util.Base64;
import java.util.Map;

@Service
public class MobSFClient {
    private static final Logger log = LoggerFactory.getLogger(MobSFClient.class);
    private final WebClient mobsfWebClient;

    // ─────────────────────────────────────────────────────
    // Paramètres d'authentification MobSF
    // ─────────────────────────────────────────────────────
    @Value("${mobsf.api.key:}")
    private String mobsfApiKey;

    @Value("${mobsf.username:mobsf}")
    private String mobsfUsername;

    @Value("${mobsf.password:mobsf}")
    private String mobsfPassword;

    public MobSFClient(WebClient mobsfWebClient) {
        this.mobsfWebClient = mobsfWebClient;
    }

    // ─────────────────────────────────────────────────────
    // Méthode utilitaire pour ajouter l'authentification
    // ─────────────────────────────────────────────────────
    private WebClient.RequestHeadersSpec<?> ajouterAuth(WebClient.RequestHeadersSpec<?> request) {
        // Si on a une clé API, l'utiliser
        if (mobsfApiKey != null && !mobsfApiKey.isEmpty()) {
            return request.header("Authorization", mobsfApiKey.trim());
        }
        // Sinon, utiliser Basic Auth (username:password)
        else {
            String credentials = mobsfUsername + ":" + mobsfPassword;
            String encodedAuth = Base64.getEncoder().encodeToString(credentials.getBytes());
            return request.header("Authorization", "Basic " + encodedAuth);
        }
    }

    // ─────────────────────────────────────────────────────
    // 1. Upload de l'APK vers MobSF
    // ─────────────────────────────────────────────────────
    @Value("${mobsf.url:http://localhost:8008}")
    private String mobsfUrl;

    public MobSFUploadResponse uploadApk(File apkFile) {
        if (apkFile == null || !apkFile.exists() || apkFile.length() == 0) {
            log.error("Fichier APK introuvable ou vide. Impossible de l'envoyer à MobSF.");
            throw new RuntimeException("Fichier APK invalide ou vide : " + (apkFile != null ? apkFile.getAbsolutePath() : "null"));
        }

        log.info("[MobSF] Upload APK (Native HTTP) : {}", apkFile.getName());

        String boundary = "----MobSFOctetStreamBoundary" + System.currentTimeMillis();
        String CRLF = "\r\n";

        try {
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(mobsfUrl + "/api/v1/upload").openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            if (mobsfApiKey != null && !mobsfApiKey.isEmpty()) {
                connection.setRequestProperty("Authorization", mobsfApiKey.trim());
            }

            try (java.io.OutputStream output = connection.getOutputStream();
                 java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(output, "UTF-8"), true)) {

                writer.append("--").append(boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(apkFile.getName()).append("\"").append(CRLF);
                writer.append("Content-Type: application/octet-stream").append(CRLF);
                writer.append(CRLF).flush();

                java.nio.file.Files.copy(apkFile.toPath(), output);
                output.flush();

                writer.append(CRLF).flush();
                writer.append("--").append(boundary).append("--").append(CRLF).flush();
            }

            int responseCode = connection.getResponseCode();
            java.io.InputStream responseStream = (responseCode >= 200 && responseCode < 300) ?
                    connection.getInputStream() : connection.getErrorStream();

            String responseBody = new String(responseStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

            if (responseCode != 200) {
                log.error("[MobSF] Erreur HTTP d'upload natif (code {}) : \n{}", responseCode, responseBody);
                throw new RuntimeException("Échec upload APK vers MobSF : " + responseCode + " " + responseBody);
            }

            String hash = null;
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"hash\"\\s*:\\s*\"([^\"]+)\"").matcher(responseBody);
            if (matcher.find()) {
                hash = matcher.group(1);
            }
            if (hash == null) {
                throw new RuntimeException("Hash introuvable dans la réponse MobSF : " + responseBody);
            }

            MobSFUploadResponse response = new MobSFUploadResponse();
            response.setHash(hash);

            log.info("[MobSF] Upload réussi — Hash : {}", response.getHash());
            return response;

        } catch (Exception e) {
            log.error("[MobSF] Erreur système d'upload native : {}", e.getMessage(), e);
            throw new RuntimeException("Échec upload APK vers MobSF : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────
    // 2. Lancer l'analyse statique
    // ─────────────────────────────────────────────────────
    public MobSFScanResponse lancerAnalyse(String hash) {
        log.info("[MobSF] Lancement analyse — Hash : {}", hash);

        try {
            WebClient.RequestHeadersSpec<?> request = mobsfWebClient.post()
                    .uri("/api/v1/scan")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("hash", hash)
                            .with("re_scan", "0"));

            // Ajouter l'authentification
            request = ajouterAuth(request);

            MobSFScanResponse response = request
                    .retrieve()
                    .bodyToMono(MobSFScanResponse.class)
                    .timeout(java.time.Duration.ofMinutes(10))
                    .block();

            log.info("[MobSF] Analyse terminée — Score sécurité : {}", response.getSecurityScore());
            return response;

        } catch (WebClientResponseException e) {
            log.error("[MobSF] Erreur analyse : {} — {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Échec analyse MobSF : " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[MobSF] Erreur analyse : {}", e.getMessage(), e);
            throw new RuntimeException("Échec analyse MobSF : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────
    // 3. Récupérer le rapport JSON complet
    // ─────────────────────────────────────────────────────
    public Map<String, Object> obtenirRapportJson(String hash) {
        log.info("[MobSF] Récupération rapport JSON — Hash : {}", hash);

        try {
            WebClient.RequestHeadersSpec<?> request = mobsfWebClient.post()
                    .uri("/api/v1/report_json")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("hash", hash));

            // Ajouter l'authentification
            request = ajouterAuth(request);

            return request
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

        } catch (WebClientResponseException e) {
            log.error("[MobSF] Erreur rapport JSON : {} — {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Échec récupération rapport : " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[MobSF] Erreur rapport JSON : {}", e.getMessage(), e);
            throw new RuntimeException("Échec récupération rapport : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────
    // 4. Vérifier le statut de MobSF
    // ─────────────────────────────────────────────────────
    public boolean verifierStatut() {
        try {
            WebClient.RequestHeadersSpec<?> request = mobsfWebClient.get()
                    .uri("/api/v1/status");

            // Ajouter l'authentification
            request = ajouterAuth(request);

            Map<String, Object> status = request
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("[MobSF] Statut OK : {}", status);
            return true;
        } catch (Exception e) {
            log.error("[MobSF] Service indisponible : {}", e.getMessage());
            return false;
        }
    }
}