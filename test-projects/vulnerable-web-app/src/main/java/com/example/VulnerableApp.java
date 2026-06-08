package com.example;

public class VulnerableApp {
    // 1. Secrets codés en dur (triggers hardcoded secrets rule)
    private static final String api_key = "secret_key_123456789";
    private static final String password = "adminSuperSecretPassword123";

    // 2. Injection de commande OS (triggers command injection rule)
    public void runDiagnostics(String host) {
        try {
            Runtime.getRuntime().exec("ping -c 4 " + host);
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }

    // 3. Injection SQL (triggers SQL injection rule)
    public String getUserQuery(String userId) {
        return "SELECT * FROM users WHERE id = " + userId;
    }
}
