import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { Smartphone, Play, Shield } from "lucide-react";
import { toast } from "sonner";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";

export default function ScanMobile() {
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
        // Filtrer seulement les scans mobiles (facultatif si le backend retourne tout, mais gardons les scans mobiles)
        const mobileScans = data.filter((s: any) => s.scanType === "SAST" && s.fileName && (s.fileName.endsWith(".apk") || s.fileName.endsWith(".ipa")));
        mobileScans.sort((a: any, b: any) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        setRealScans(mobileScans);
      }
    } catch (e) {
      console.error("Erreur de récupération des scans", e);
    }
  };

  useEffect(() => {
    fetchScans();
  }, []);

  const startScan = async () => {
    if (!file) {
      toast.error("Veuillez sélectionner un fichier APK/IPA pour l'analyse");
      return;
    }
    if (!project) {
      toast.error("Veuillez entrer un nom d'application");
      return;
    }
    
    setScanning(true);
    setProgress(0);
    toast.info("Upload de l'archive en cours...");
    
    const formData = new FormData();
    formData.append("projectName", project);
    formData.append("file", file);
    
    const token = localStorage.getItem("vulnscan-token");

    const xhr = new XMLHttpRequest();
    let interval: NodeJS.Timeout;

    // Progression 0-50% = Temps d'upload vers le Backend
    xhr.upload.onprogress = (event) => {
      if (event.lengthComputable) {
        const percent = Math.round((event.loaded / event.total) * 50);
        setProgress(percent);
      }
    };

    xhr.upload.onloadend = () => {
      toast.info("Upload terminé. MobSF analyse le code...");
      // Progression 50-95% = Temps d'analyse MobSF
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
        toast.success(`Scan Mobile terminé ! ${data.totalVulnerabilities} alertes relevées.`);
        fetchScans();
      } else {
        toast.error(xhr.status === 403 ? "Accès refusé - Rôle Administrateur ou Analyste requis" : "Erreur lors de l'analyse MobSF");
      }
    };

    xhr.onerror = () => {
      clearInterval(interval);
      setScanning(false);
      toast.error("Connexion au serveur échouée.");
    };

    xhr.open("POST", "/api/v1/analyze/mobile", true);
    if (token) xhr.setRequestHeader("Authorization", `Bearer ${token}`);
    
    xhr.send(formData);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold font-heading flex items-center gap-2">
          <Smartphone className="h-6 w-6 text-primary" />Scan Mobile
        </h1>
        <p className="text-muted-foreground text-sm">Analysez la sécurité de vos applications mobiles via MobSF</p>
      </div>

      <Card className="border-border/50">
        <CardHeader><CardTitle className="text-base">Configurer le scan</CardTitle></CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <label className="text-sm font-medium">Nom de l'application</label>
            <Input 
              placeholder="Ex: Mon Application Android" 
              value={project} 
              onChange={(e) => setProject(e.target.value)} 
              disabled={scanning}
            />
          </div>

          <div className="space-y-2 pt-2">
            <label className="text-sm font-medium">Application (.apk, .ipa)</label>
            <Input 
              type="file" 
              accept=".apk,.ipa" 
              onChange={(e) => setFile(e.target.files?.[0] || null)} 
              disabled={scanning}
            />
            <p className="text-xs text-muted-foreground">Fournissez le fichier mobile à analyser statiquement par MobSF.</p>
          </div>

          {scanning && (
            <div className="space-y-2 pt-2">
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">
                  {progress < 50 ? "Upload vers le serveur en cours..." : "MobSF analyse votre application..."}
                </span>
                <span className="font-mono">{Math.min(100, Math.round(progress))}%</span>
              </div>
              <Progress value={Math.min(100, progress)} className="h-2 transition-all duration-300" />
            </div>
          )}

          <Button onClick={startScan} disabled={!project || scanning || !file}>
            <Play className="h-4 w-4 mr-2" />Lancer le scan
          </Button>
        </CardContent>
      </Card>

      <Card className="border-border/50">
        <CardHeader><CardTitle className="text-base">Historique Récents (Vue Base de données)</CardTitle></CardHeader>
        <CardContent>
          <div className="space-y-3">
            {realScans.length === 0 ? (
              <p className="text-sm text-muted-foreground text-center py-4">Aucun scan récent, lancez une analyse pour commencer.</p>
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
                  <Badge variant="outline" className="font-mono text-xs">MOBILE</Badge>
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
