import { useState, useEffect, useRef } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Play, Globe, Shield, Loader2, CheckCircle2, XCircle, Clock } from "lucide-react";
import { toast } from "sonner";
import { Input } from "@/components/ui/input";

type ScanStatus = "PENDING" | "RUNNING" | "COMPLETED" | "FAILED" | "CANCELLED";

interface ScanData {
  id: number;
  projectName: string;
  fileName: string;
  status: ScanStatus;
  progress: number;
  currentStep: string;
  logs: string;
  totalVulnerabilities: number;
  criticalCount: number;
  highCount: number;
  createdAt: string;
  completedAt?: string;
  executionTime?: number;
  scanType: string;
}

function StatusBadge({ status }: { status: ScanStatus }) {
  const cfg: Record<ScanStatus, { label: string; className: string; icon: JSX.Element }> = {
    PENDING:   { label: "En attente",  className: "bg-yellow-500/20 text-yellow-400 border-yellow-500/40", icon: <Clock className="h-3 w-3" /> },
    RUNNING:   { label: "En cours",    className: "bg-blue-500/20 text-blue-400 border-blue-500/40",       icon: <Loader2 className="h-3 w-3 animate-spin" /> },
    COMPLETED: { label: "Terminé",     className: "bg-green-500/20 text-green-400 border-green-500/40",    icon: <CheckCircle2 className="h-3 w-3" /> },
    FAILED:    { label: "Échoué",      className: "bg-red-500/20 text-red-400 border-red-500/40",          icon: <XCircle className="h-3 w-3" /> },
    CANCELLED: { label: "Annulé",      className: "bg-gray-500/20 text-gray-400 border-gray-500/40",       icon: <XCircle className="h-3 w-3" /> },
  };
  const c = cfg[status] ?? cfg.PENDING;
  return (
    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full border text-xs font-medium ${c.className}`}>
      {c.icon}{c.label}
    </span>
  );
}

export default function ScanWeb() {
  const [scanType, setScanType] = useState("");
  const [project, setProject] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);

  // Active scan being tracked
  const [activeScanId, setActiveScanId] = useState<number | null>(null);
  const [activeScan, setActiveScan] = useState<ScanData | null>(null);

  // History
  const [recentScans, setRecentScans] = useState<ScanData[]>([]);

  const pollingRef = useRef<NodeJS.Timeout | null>(null);

  const getToken = () => localStorage.getItem("vulnscan-token");

  // ── Fetch 5 recent scans for history ────────────────────────────────────────
  const fetchRecentScans = async () => {
    try {
      const res = await fetch("/api/v1/analyze?size=20&sort=createdAt,desc", {
        headers: { Authorization: `Bearer ${getToken()}` },
      });
      if (res.ok) {
        const data = await res.json();
        const list: ScanData[] = Array.isArray(data) ? data : data.content || [];
        // Keep only web scans (no .apk / .ipa)
        const webScans = list.filter(
          (s) => !s.fileName?.endsWith(".apk") && !s.fileName?.endsWith(".ipa")
        );
        setRecentScans(webScans.slice(0, 5));
      }
    } catch (e) {
      console.error("Erreur récupération scans", e);
    }
  };

  // ── Poll a single scan by ID ─────────────────────────────────────────────────
  const pollScan = async (id: number) => {
    try {
      const res = await fetch(`/api/v1/analyze/${id}/progress`, {
        headers: { Authorization: `Bearer ${getToken()}` },
      });
      if (res.ok) {
        const scan: ScanData = await res.json();
        setActiveScan(scan);

        if (scan.status === "COMPLETED" || scan.status === "FAILED" || scan.status === "CANCELLED") {
          stopPolling();
          setActiveScanId(null);
          if (scan.status === "COMPLETED") {
            toast.success(`Scan terminé ! ${scan.totalVulnerabilities} vulnérabilité(s) détectée(s).`);
          } else {
            toast.error("Le scan a échoué ou a été annulé.");
          }
          fetchRecentScans();
        }
      }
    } catch (e) {
      console.error("Erreur polling scan", e);
    }
  };

  // ── Start / stop polling ────────────────────────────────────────────────────
  const startPolling = (id: number) => {
    stopPolling();
    pollingRef.current = setInterval(() => pollScan(id), 3000);
  };

  const stopPolling = () => {
    if (pollingRef.current) {
      clearInterval(pollingRef.current);
      pollingRef.current = null;
    }
  };

  // ── On mount: check if a scan was already in progress ───────────────────────
  useEffect(() => {
    fetchRecentScans();

    const savedId = localStorage.getItem("activeScanId_web");
    if (savedId) {
      const id = parseInt(savedId, 10);
      setActiveScanId(id);
      pollScan(id);
      startPolling(id);
    }

    return () => stopPolling();
  }, []);

  // ── Launch scan ──────────────────────────────────────────────────────────────
  const startScan = () => {
    if (scanType === "sast" && !file) {
      toast.error("Veuillez sélectionner un fichier ZIP pour l'analyse SAST");
      return;
    }
    if (!project.trim()) {
      toast.error("Veuillez entrer un nom de projet");
      return;
    }

    setUploading(true);
    setUploadProgress(0);

    const formData = new FormData();
    formData.append("projectName", project);
    formData.append("file", file ?? new Blob(["dummy"], { type: "text/plain" }), file?.name ?? "dummy.zip");

    const xhr = new XMLHttpRequest();

    xhr.upload.onprogress = (e) => {
      if (e.lengthComputable) setUploadProgress(Math.round((e.loaded / e.total) * 100));
    };

    xhr.onload = () => {
      setUploading(false);
      setUploadProgress(0);
      if (xhr.status >= 200 && xhr.status < 300) {
        const data: ScanData = JSON.parse(xhr.responseText);
        toast.success(`Scan lancé en arrière-plan (ID: ${data.id}). Vous pouvez naviguer librement.`);
        localStorage.setItem("activeScanId_web", String(data.id));
        setActiveScanId(data.id);
        setActiveScan(data);
        startPolling(data.id);
      } else if (xhr.status === 403) {
        toast.error("Accès refusé – Rôle Administrateur ou Analyste requis");
      } else {
        toast.error("Erreur lors du lancement du scan");
      }
    };

    xhr.onerror = () => {
      setUploading(false);
      toast.error("Connexion au serveur échouée");
    };

    xhr.open("POST", "/api/v1/analyze/web", true);
    xhr.setRequestHeader("Authorization", `Bearer ${getToken()}`);
    xhr.send(formData);
  };

  const isScanning = uploading || (activeScan && (activeScan.status === "RUNNING" || activeScan.status === "PENDING"));

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold font-heading flex items-center gap-2 african-heading">
          <Globe className="h-7 w-7 text-primary" />Scan Web App
        </h1>
        <p className="text-muted-foreground mt-1">Analyser le code source d'une application web</p>
      </div>

      {/* ── Configuration ── */}
      <Card className="border-border/50">
        <CardHeader><CardTitle className="text-base">Configurer le scan</CardTitle></CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Type de scan</label>
              <Select value={scanType} onValueChange={setScanType} disabled={!!isScanning}>
                <SelectTrigger><SelectValue placeholder="Choisir le type" /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="sast">SAST — Analyse statique (IA)</SelectItem>
                  <SelectItem value="dast">DAST — Analyse dynamique</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Nom de l'application</label>
              <Input
                id="projectNameInput"
                placeholder="Ex: Mon Application E-Commerce"
                value={project}
                onChange={(e) => setProject(e.target.value)}
                disabled={!!isScanning}
              />
            </div>
          </div>

          {scanType === "sast" && (
            <div className="space-y-2 pt-2">
              <label className="text-sm font-medium">Code source (.zip)</label>
              <Input type="file" accept=".zip" onChange={(e) => setFile(e.target.files?.[0] || null)} disabled={!!isScanning} />
              <p className="text-xs text-muted-foreground">Fournissez le projet Java zippé à analyser par le ML.</p>
            </div>
          )}

          {/* Upload progress */}
          {uploading && (
            <div className="space-y-2 pt-2">
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Upload de l'archive...</span>
                <span className="font-mono">{uploadProgress}%</span>
              </div>
              <Progress value={uploadProgress} className="h-2" />
            </div>
          )}

          <Button id="startScanBtn" onClick={startScan} disabled={!scanType || !project || !!isScanning || (scanType === "sast" && !file)}>
            {isScanning ? <Loader2 className="h-4 w-4 mr-2 animate-spin" /> : <Play className="h-4 w-4 mr-2" />}
            {isScanning ? "Scan en cours..." : "Lancer le scan"}
          </Button>
        </CardContent>
      </Card>

      {/* ── Active scan tracker (persists across navigation) ── */}
      {activeScan && (activeScan.status === "RUNNING" || activeScan.status === "PENDING") && (
        <Card className="border-blue-500/40 bg-blue-500/5">
          <CardHeader className="pb-2">
            <CardTitle className="text-base flex items-center gap-2">
              <Loader2 className="h-4 w-4 animate-spin text-blue-400" />
              Scan en cours — {activeScan.projectName}
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">{activeScan.currentStep || "Initialisation..."}</span>
              <span className="font-mono font-bold text-blue-400">{activeScan.progress}%</span>
            </div>
            <Progress value={activeScan.progress} className="h-3" />
            <div className="flex items-center gap-3 text-xs text-muted-foreground">
              <span>ID: #{activeScan.id}</span>
              <StatusBadge status={activeScan.status} />
              <span className="text-blue-400">● Mise à jour toutes les 3s</span>
            </div>
            {activeScan.logs && (
              <pre className="text-xs bg-muted/40 rounded p-2 max-h-24 overflow-y-auto whitespace-pre-wrap text-muted-foreground">
                {activeScan.logs}
              </pre>
            )}
          </CardContent>
        </Card>
      )}

      {/* ── Historique ── */}
      <Card className="border-border/50">
        <CardHeader>
          <CardTitle className="text-base">Historique récent</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {recentScans.length === 0 ? (
              <p className="text-sm text-muted-foreground text-center py-4">Aucun scan récent.</p>
            ) : recentScans.map((s) => (
              <div key={s.id} className="flex items-center justify-between p-3 rounded-lg bg-muted/30">
                <div className="flex items-center gap-3">
                  <Shield className="h-4 w-4 text-primary shrink-0" />
                  <div>
                    <p className="text-sm font-medium">{s.projectName}</p>
                    <p className="text-xs text-muted-foreground">
                      {new Date(s.createdAt).toLocaleString()}
                      {s.executionTime && <span> · {(s.executionTime / 1000).toFixed(1)}s</span>}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <StatusBadge status={s.status} />
                  <Badge variant="outline" className="font-mono text-xs">{s.scanType}</Badge>
                  <span className={`text-sm font-mono ${s.totalVulnerabilities > 0 ? "text-destructive font-bold" : "text-green-500"}`}>
                    {s.totalVulnerabilities} vulns
                  </span>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
