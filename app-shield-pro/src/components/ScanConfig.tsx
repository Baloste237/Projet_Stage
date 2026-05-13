import { useState, useEffect } from "react";
import { toast } from "sonner";
import { Settings, Save, Server, Smartphone, ShieldCheck } from "lucide-react";

interface ScanConfigState {
  webScanEnabled: boolean;
  mobileScanEnabled: boolean;
  scanLevel: "basic" | "advanced" | "expert";
}

const DEFAULT_CONFIG: ScanConfigState = {
  webScanEnabled: true,
  mobileScanEnabled: true,
  scanLevel: "advanced",
};

export const ScanConfig = () => {
  const [config, setConfig] = useState<ScanConfigState>(DEFAULT_CONFIG);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    const saved = localStorage.getItem("vulnscan-admin-config");
    if (saved) {
      try {
        setConfig(JSON.parse(saved));
      } catch (e) {
        console.error("Erreur de chargement de la configuration");
      }
    }
  }, []);

  const handleSave = () => {
    setIsSaving(true);
    setTimeout(() => {
      localStorage.setItem("vulnscan-admin-config", JSON.stringify(config));
      toast.success("Configuration sauvegardée avec succès");
      setIsSaving(false);
    }, 500); // Simulate API call
  };

  return (
    <div className="bg-card text-card-foreground rounded-xl border shadow-sm overflow-hidden mt-8">
      <div className="p-6 border-b border-border bg-muted/40">
        <h3 className="text-lg font-semibold flex items-center gap-2">
          <Settings className="h-5 w-5 text-primary" />
          Configuration du Moteur de Scan
        </h3>
        <p className="text-sm text-muted-foreground mt-1">Gérez l'activation et l'intensité des modules d'analyse SAST</p>
      </div>

      <div className="p-6 space-y-8">
        <div className="grid md:grid-cols-2 gap-8">
          {/* Web Scan Toggle */}
          <div className="flex items-start space-x-4 p-4 rounded-lg border border-border/50 bg-background">
            <div className={`p-2 rounded-full ${config.webScanEnabled ? "bg-primary/10 text-primary" : "bg-muted text-muted-foreground"}`}>
              <Server className="h-6 w-6" />
            </div>
            <div className="flex-1">
              <div className="flex items-center justify-between">
                <h4 className="font-medium">Scan Web</h4>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input 
                    type="checkbox" 
                    className="sr-only peer" 
                    checked={config.webScanEnabled}
                    onChange={(e) => setConfig({ ...config, webScanEnabled: e.target.checked })}
                  />
                  <div className="w-11 h-6 bg-muted peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                </label>
              </div>
              <p className="text-sm text-muted-foreground mt-1">Analyse des vulnérabilités des applications web (React, etc.)</p>
            </div>
          </div>

          {/* Mobile Scan Toggle */}
          <div className="flex items-start space-x-4 p-4 rounded-lg border border-border/50 bg-background">
            <div className={`p-2 rounded-full ${config.mobileScanEnabled ? "bg-primary/10 text-primary" : "bg-muted text-muted-foreground"}`}>
              <Smartphone className="h-6 w-6" />
            </div>
            <div className="flex-1">
              <div className="flex items-center justify-between">
                <h4 className="font-medium">Scan Mobile (MobSF)</h4>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input 
                    type="checkbox" 
                    className="sr-only peer" 
                    checked={config.mobileScanEnabled}
                    onChange={(e) => setConfig({ ...config, mobileScanEnabled: e.target.checked })}
                  />
                  <div className="w-11 h-6 bg-muted peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                </label>
              </div>
              <p className="text-sm text-muted-foreground mt-1">Analyse approfondie des fichiers APK via le moteur MobSF</p>
            </div>
          </div>
        </div>

        {/* Scan Level */}
        <div className="pt-4 border-t border-border/50">
          <h4 className="font-medium flex items-center gap-2 mb-4">
            <ShieldCheck className="h-5 w-5 text-muted-foreground" />
            Niveau d'analyse
          </h4>
          <div className="grid grid-cols-3 gap-4">
            {(["basic", "advanced", "expert"] as const).map((level) => (
              <button
                key={level}
                onClick={() => setConfig({ ...config, scanLevel: level })}
                className={`p-4 rounded-lg border text-left transition-all ${
                  config.scanLevel === level 
                    ? "border-primary bg-primary/5 shadow-sm" 
                    : "border-border hover:bg-muted/30"
                }`}
              >
                <div className="font-medium capitalize">{level === "basic" ? "Basique" : level === "advanced" ? "Avancé" : "Expert"}</div>
                <div className="text-xs text-muted-foreground mt-1">
                  {level === "basic" && "Vérifications de surface rapides"}
                  {level === "advanced" && "Analyse statique et dynamique recommandée"}
                  {level === "expert" && "Scan exhaustif avec fuzzing (très lent)"}
                </div>
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="p-4 border-t border-border bg-muted/20 flex justify-end">
        <button
          onClick={handleSave}
          disabled={isSaving}
          className="bg-primary text-primary-foreground hover:bg-primary/90 px-4 py-2 rounded-md font-medium text-sm flex items-center gap-2 transition-colors disabled:opacity-50"
        >
          {isSaving ? (
            <div className="h-4 w-4 rounded-full border-2 border-primary-foreground border-t-transparent animate-spin"></div>
          ) : (
            <Save className="h-4 w-4" />
          )}
          Enregistrer la configuration
        </button>
      </div>
    </div>
  );
};
