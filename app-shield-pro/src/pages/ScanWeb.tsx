import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Play, Globe, Shield } from "lucide-react";
import { toast } from "sonner";
import { Input } from "@/components/ui/input";

export default function ScanWeb() {
  const [scanType, setScanType] = useState("");
  const [project, setProject] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [scanning, setScanning] = useState(false);
  const [progress, setProgress] = useState(0);
  const [realScans, setRealScans] = useState<any[]>([]);

  const fetchScans = async () => {
    try {
      const token = localStorage.getItem("vulnscan-token");
      const res = await fetch("/api/v1/analyze", {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
        // Trier du plus récent au plus ancien
        data.sort((a: any, b: any) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        setRealScans(data);
      }
    } catch (e) {
      console.error("Erreur de récupération des scans", e);
    }
  };

  useEffect(() => {
    fetchScans();
  }, []);

  const startScan = async () => {
    if (scanType === "sast" && !file) {
      toast.error("Veuillez sélectionner un fichier ZIP pour l'analyse SAST");
      return;
    }
    
    setScanning(true);
    setProgress(0);
    toast.info("Upload de l'archive en cours...");
    
    const formData = new FormData();
    formData.append("projectName", project);
    
    if (scanType === "sast" && file) {
      formData.append("file", file);
    } else {
      formData.append("file", new Blob(["dummy payload"], { type: "text/plain" }), "dummy.zip");
    }
    
    const token = localStorage.getItem("vulnscan-token");

    // Utilisation de XMLHttpRequest pour traquer l'upload VRAI
    const xhr = new XMLHttpRequest();
    let interval: NodeJS.Timeout;

    // Progression 0-50% = Temps d'upload du Web ver le Backend
    xhr.upload.onprogress = (event) => {
      if (event.lengthComputable) {
        const percent = Math.round((event.loaded / event.total) * 50);
        setProgress(percent);
      }
    };

    xhr.upload.onloadend = () => {
      toast.info("Upload terminé. L'IA SAST analyse le code...");
      // Progression 50-95% = Temps d'analyse IA (simulation visuelle pendant l'attente)
      interval = setInterval(() => {
        setProgress((prev) => {
          if (prev >= 50 && prev < 95) return prev + 1;
          return prev;
        });
      }, 800);
    };

    xhr.onload = () => {
      clearInterval(interval);
      setScanning(false);
      setProgress(100);
      
      if (xhr.status >= 200 && xhr.status < 300) {
        const data = JSON.parse(xhr.responseText);
        toast.success(`Scan SAST terminé ! ${data.totalVulnerabilities} alertes relevées.`);
        fetchScans(); // Met à jour l'historique visuel
      } else {
        toast.error(xhr.status === 403 ? "Accès refusé - Rôle Administrateur ou Analyste requis" : "Erreur lors de l'analyse IA");
      }
    };

    xhr.onerror = () => {
      clearInterval(interval);
      setScanning(false);
      toast.error("Connexion au serveur échouée.");
    };

    xhr.open("POST", "/api/v1/analyze/web", true);
    if (token) xhr.setRequestHeader("Authorization", `Bearer ${token}`);
    
    xhr.send(formData);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold font-heading flex items-center gap-2 african-heading">
          <Globe className="h-7 w-7 text-primary" />Scan Web App
        </h1>
        <p className="text-muted-foreground mt-1">Analyser le code source d'une application web</p>
      </div>

      <Card className="border-border/50">
        <CardHeader><CardTitle className="text-base">Configurer le scan</CardTitle></CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Type de scan</label>
              <Select value={scanType} onValueChange={setScanType}>
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
                disabled={scanning}
              />
            </div>
          </div>

          {scanType === "sast" && (
            <div className="space-y-2 pt-2">
              <label className="text-sm font-medium">Code source (.zip)</label>
              <Input 
                type="file" 
                accept=".zip" 
                onChange={(e) => setFile(e.target.files?.[0] || null)} 
                disabled={scanning}
              />
              <p className="text-xs text-muted-foreground">Fournissez le projet Java zippé à analyser par le ML.</p>
            </div>
          )}

          {scanning && (
            <div className="space-y-2 pt-2">
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">
                  {progress < 50 ? "Upload de l'archive ZIP en cours..." : "L'IA analyse vos fichiers..."}
                </span>
                <span className="font-mono">{Math.min(100, Math.round(progress))}%</span>
              </div>
              <Progress value={Math.min(100, progress)} className="h-2 transition-all duration-300" />
            </div>
          )}

          <Button onClick={startScan} disabled={!scanType || !project || scanning || (scanType === 'sast' && !file)}>
            <Play className="h-4 w-4 mr-2" />Lancer le scan
          </Button>
        </CardContent>
      </Card>

      <Card className="border-border/50">
        <CardHeader><CardTitle className="text-base">Historique Récents (Vue Base de données)</CardTitle></CardHeader>
        <CardContent>
          <div className="space-y-3">
            {realScans.length === 0 ? (
              <p className="text-sm text-muted-foreground text-center py-4">Aucun scan récent, lancez une analyse SAST pour commencer.</p>
            ) : realScans.map((s) => (
              <div key={s.id} className="flex items-center justify-between p-3 rounded-lg bg-muted/30">
                <div className="flex items-center gap-3">
                  <Shield className="h-4 w-4 text-primary" />
                  <div>
                    <p className="text-sm font-medium">{s.projectName}</p>
                    <p className="text-xs text-muted-foreground">
                      {new Date(s.createdAt).toLocaleString()} · Statut: <span className={s.status === 'FAILED' ? 'text-destructive' : 'text-primary'}>{s.status}</span>
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <Badge variant="outline" className="font-mono text-xs">{s.scanType}</Badge>
                  <span className={`text-sm font-mono ${s.totalVulnerabilities > 0 ? 'text-destructive font-bold' : 'text-green-500'}`}>
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
