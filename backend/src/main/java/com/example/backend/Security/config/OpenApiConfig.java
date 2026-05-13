package com.example.backend.Security.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration OpenAPI / Swagger professionnelle.
 * <p>
 * Fournit une documentation API complète pour la plateforme VulnScan Pro,
 * compatible JWT Bearer Token avec bouton "Authorize" intégré.
 * </p>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // ── Informations générales ──
                .info(new Info()
                        .title("VulnScan Pro — API DevSecOps")
                        .version("1.0.0")
                        .description("""
                                **VulnScan Pro** est une plateforme DevSecOps complète dédiée à l'analyse \
                                de sécurité des applications web et mobiles.

                                ### Fonctionnalités principales
                                - 🔐 **Authentification JWT** — Inscription, connexion et gestion de tokens sécurisés
                                - 🌐 **Scan Web (SAST)** — Analyse statique de code source via upload ZIP
                                - 📱 **Scan Mobile (SAST)** — Analyse d'applications Android (APK) via MobSF
                                - 🛡️ **Gestion des vulnérabilités** — Classification CWE, scoring CVSS, niveaux de sévérité
                                - 📊 **Rapports** — Génération de rapports PDF et JSON détaillés
                                - 📈 **Monitoring & Audit** — Suivi en temps réel des activités et journaux d'audit
                                - 👥 **Gestion utilisateurs** — Administration des comptes et des rôles (ADMIN / ANALYSTE)

                                ### Architecture technique
                                - **Backend** : Spring Boot 4.x / Spring Security / JWT (jjwt 0.12.x)
                                - **Base de données** : PostgreSQL
                                - **Moteur SAST** : Python (IA / ML)
                                - **Scan Mobile** : MobSF (Mobile Security Framework)
                                - **Frontend** : React.js

                                ### Authentification
                                Tous les endpoints protégés nécessitent un token JWT.  \
                                Cliquez sur le bouton **Authorize** 🔓 ci-dessus et saisissez votre token :
                                ```
                                Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                                ```
                                """)
                        .contact(new Contact()
                                .name("Loïc France Baloste")
                                .email("loicfranceb@gmail.com")
                                .url("https://github.com/Baloste237"))
                        .license(new License()
                                .name("Projet de Stage — Usage Interne")
                                .url("https://github.com/Baloste237/Projet_Stage"))
                )

                // ── Serveurs ──
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Serveur de développement local"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Serveur Docker")
                ))

                // ── Sécurité JWT (bouton Authorize) ──
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Saisissez votre token JWT obtenu via `/api/auth/login`. "
                                                + "Format : `Bearer <votre_token>`")
                        )
                )

                // ── Organisation par catégories (Tags) ──
                .tags(List.of(
                        new Tag().name("Authentification")
                                .description("Inscription, connexion, gestion des tokens JWT et réinitialisation de mot de passe"),
                        new Tag().name("Gestion Utilisateurs")
                                .description("CRUD complet des utilisateurs — Réservé aux administrateurs (ROLE_ADMIN)"),
                        new Tag().name("Administration")
                                .description("Gestion avancée des comptes : activation/désactivation, changement de rôles"),
                        new Tag().name("Scan Sécurité")
                                .description("Lancement et suivi des analyses SAST pour applications web (ZIP) et mobiles (APK)"),
                        new Tag().name("Vulnérabilités")
                                .description("Consultation, filtrage et suppression des vulnérabilités détectées (CWE, CVSS, sévérité)"),
                        new Tag().name("Rapports")
                                .description("Génération et téléchargement de rapports de scan au format PDF ou JSON"),
                        new Tag().name("Historique")
                                .description("Consultation de l'historique des scans et de l'évolution des vulnérabilités"),
                        new Tag().name("Monitoring & Logs")
                                .description("Journaux d'audit, statistiques système et monitoring en temps réel — ROLE_ADMIN"),
                        new Tag().name("Dashboard")
                                .description("Point d'entrée public du tableau de bord")
                ));
    }
}
