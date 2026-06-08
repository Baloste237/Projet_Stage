import java.sql.*;
import java.io.*;
import java.security.MessageDigest;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;

public class VulnerableTestApp {
    
    // ─── CWE-798 : Hardcoded Password ─────────────────────────────────────
    private static final String DB_PASSWORD = "admin123";
    private static final String API_KEY     = "sk-prod-abc123xyz789";

    // ─── CWE-89 : SQL Injection ────────────────────────────────────────────
    public void loginUser(String username, String password) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost/db", "user", DB_PASSWORD);
        String query = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query); // SQL INJECTION directe
    }

    // ─── CWE-78 : Command Injection ───────────────────────────────────────
    public String executeCommand(String userInput) throws Exception {
        String cmd = "ping " + userInput;
        Process process = Runtime.getRuntime().exec(cmd); // COMMAND INJECTION
        return "Done";
    }

    // ─── CWE-22 : Path Traversal ──────────────────────────────────────────
    public String readFile(String filename) throws Exception {
        File file = new File("/var/data/" + filename); // PATH TRAVERSAL
        return new String(Files.readAllBytes(file.toPath()));
    }

    // ─── CWE-327 : Weak Cryptography (MD5) ────────────────────────────────
    public String hashPassword(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5"); // MD5 FAIBLE
        byte[] hash = md.digest(password.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    // ─── CWE-209 : Exception Handling Failure ─────────────────────────────
    public void dangerousOperation(String input) {
        try {
            int result = Integer.parseInt(input);
        } catch (Exception e) {
            // Exception silencieuse — CWE-209
        }
    }

    // ─── CWE-284 : Broken Access Control ─────────────────────────────────
    public String getAdminData(String userId) throws Exception {
        // Pas de vérification du rôle !
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost/db", "user", DB_PASSWORD);
        String query = "SELECT * FROM admin_data WHERE id = " + userId;
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        return rs.getString(1);
    }
}
