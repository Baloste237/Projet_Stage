export const vulnerabilities = [
  { id: "VULN-001", name: "SQL Injection", severity: "critical" as const, project: "E-Commerce API", type: "SAST", date: "2026-03-28", status: "open", description: "Unsanitized user input directly concatenated into SQL query string.", code: "const query = `SELECT * FROM users WHERE id = '${req.params.id}'`;", recommendation: "Use parameterized queries or prepared statements." },
  { id: "VULN-002", name: "Cross-Site Scripting (XSS)", severity: "high" as const, project: "Dashboard Web", type: "DAST", date: "2026-03-27", status: "open", description: "Reflected XSS vulnerability in search parameter.", code: "document.innerHTML = userInput;", recommendation: "Sanitize all user inputs and use Content Security Policy headers." },
  { id: "VULN-003", name: "Insecure Deserialization", severity: "critical" as const, project: "Payment Service", type: "SAST", date: "2026-03-26", status: "in_progress", description: "Untrusted data deserialized without validation.", code: "const obj = JSON.parse(externalData);", recommendation: "Validate and sanitize all serialized data before deserialization." },
  { id: "VULN-004", name: "Broken Authentication", severity: "high" as const, project: "Mobile App API", type: "DAST", date: "2026-03-25", status: "open", description: "Session tokens not invalidated after password change.", code: "// No session invalidation logic", recommendation: "Invalidate all active sessions when credentials are changed." },
  { id: "VULN-005", name: "Sensitive Data Exposure", severity: "medium" as const, project: "E-Commerce API", type: "SAST", date: "2026-03-24", status: "resolved", description: "API key hardcoded in source code.", code: "const API_KEY = 'sk_live_abc123...';", recommendation: "Use environment variables for sensitive configuration." },
  { id: "VULN-006", name: "Security Misconfiguration", severity: "medium" as const, project: "Dashboard Web", type: "DAST", date: "2026-03-23", status: "open", description: "CORS policy allows all origins.", code: "Access-Control-Allow-Origin: *", recommendation: "Restrict CORS to trusted domains only." },
  { id: "VULN-007", name: "Outdated Dependencies", severity: "low" as const, project: "Payment Service", type: "SAST", date: "2026-03-22", status: "open", description: "Using lodash 4.17.15 with known prototype pollution.", code: "\"lodash\": \"^4.17.15\"", recommendation: "Update to latest patched version." },
  { id: "VULN-008", name: "Insufficient Logging", severity: "low" as const, project: "Mobile App API", type: "DAST", date: "2026-03-21", status: "resolved", description: "Failed login attempts not logged.", code: "// No audit logging", recommendation: "Implement comprehensive audit logging for security events." },
];

export const projects = [
  { id: "1", name: "E-Commerce API", language: "TypeScript", lastScan: "2026-03-28", status: "critical", vulnerabilities: 12, score: 45 },
  { id: "2", name: "Dashboard Web", language: "React", lastScan: "2026-03-27", status: "warning", vulnerabilities: 5, score: 72 },
  { id: "3", name: "Payment Service", language: "Java", lastScan: "2026-03-26", status: "warning", vulnerabilities: 8, score: 65 },
  { id: "4", name: "Mobile App API", language: "Python", lastScan: "2026-03-25", status: "good", vulnerabilities: 2, score: 89 },
  { id: "5", name: "Auth Microservice", language: "Go", lastScan: "2026-03-24", status: "good", vulnerabilities: 1, score: 95 },
];

export const scans = [
  { id: "SCAN-001", project: "E-Commerce API", type: "SAST", date: "2026-03-28", duration: "4m 32s", vulnerabilities: 5, status: "completed" },
  { id: "SCAN-002", project: "Dashboard Web", type: "DAST", date: "2026-03-27", duration: "12m 15s", vulnerabilities: 3, status: "completed" },
  { id: "SCAN-003", project: "Payment Service", type: "SAST", date: "2026-03-26", duration: "6m 48s", vulnerabilities: 4, status: "completed" },
  { id: "SCAN-004", project: "Mobile App API", type: "DAST", date: "2026-03-25", duration: "8m 22s", vulnerabilities: 1, status: "completed" },
  { id: "SCAN-005", project: "Auth Microservice", type: "SAST", date: "2026-03-24", duration: "2m 10s", vulnerabilities: 0, status: "completed" },
];

export const reports = [
  { id: "RPT-001", project: "E-Commerce API", date: "2026-03-28", vulnerabilities: 12, riskScore: 45, type: "Full Scan" },
  { id: "RPT-002", project: "Dashboard Web", date: "2026-03-27", vulnerabilities: 5, riskScore: 72, type: "DAST Only" },
  { id: "RPT-003", project: "Payment Service", date: "2026-03-26", vulnerabilities: 8, riskScore: 65, type: "SAST Only" },
  { id: "RPT-004", project: "Mobile App API", date: "2026-03-25", vulnerabilities: 2, riskScore: 89, type: "Full Scan" },
];

export const vulnTrendData = [
  { month: "Oct", critical: 8, high: 12, medium: 18, low: 25 },
  { month: "Nov", critical: 6, high: 15, medium: 20, low: 22 },
  { month: "Dec", critical: 10, high: 11, medium: 16, low: 28 },
  { month: "Jan", critical: 5, high: 9, medium: 22, low: 20 },
  { month: "Feb", critical: 4, high: 8, medium: 15, low: 18 },
  { month: "Mar", critical: 3, high: 7, medium: 12, low: 15 },
];

export const severityDistribution = [
  { name: "Critical", value: 3, fill: "hsl(0, 80%, 55%)" },
  { name: "High", value: 7, fill: "hsl(25, 95%, 53%)" },
  { name: "Medium", value: 12, fill: "hsl(45, 95%, 50%)" },
  { name: "Low", value: 15, fill: "hsl(200, 60%, 55%)" },
];
