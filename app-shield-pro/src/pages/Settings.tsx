import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Separator } from "@/components/ui/separator";
import { Settings as SettingsIcon, Users, Shield, Bell, ScanLine } from "lucide-react";

export default function SettingsPage() {
  return (
    <div className="space-y-6 max-w-3xl">
      <div>
        <h1 className="text-2xl font-bold font-heading flex items-center gap-2">
          <SettingsIcon className="h-6 w-6 text-primary" />Paramètres
        </h1>
        <p className="text-muted-foreground text-sm">Configurez votre plateforme VulnScan</p>
      </div>

      <Card className="border-border/50">
        <CardHeader><CardTitle className="text-base flex items-center gap-2"><Users className="h-4 w-4" />Gestion des utilisateurs</CardTitle></CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label>Nom</Label>
              <Input defaultValue="Jean Dupont" />
            </div>
            <div className="space-y-2">
              <Label>Email</Label>
              <Input defaultValue="jean@exemple.com" />
            </div>
          </div>
          <Button size="sm">Mettre à jour</Button>
        </CardContent>
      </Card>

      <Card className="border-border/50">
        <CardHeader><CardTitle className="text-base flex items-center gap-2"><ScanLine className="h-4 w-4" />Configuration des scans</CardTitle></CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium">Scan automatique</p>
              <p className="text-xs text-muted-foreground">Lancer un scan à chaque push Git</p>
            </div>
            <Switch defaultChecked />
          </div>
          <Separator />
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium">Analyse approfondie</p>
              <p className="text-xs text-muted-foreground">Inclure l'analyse des dépendances tierces</p>
            </div>
            <Switch defaultChecked />
          </div>
          <Separator />
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium">Scan DAST automatique</p>
              <p className="text-xs text-muted-foreground">Lancer un DAST après chaque déploiement</p>
            </div>
            <Switch />
          </div>
        </CardContent>
      </Card>

      <Card className="border-border/50">
        <CardHeader><CardTitle className="text-base flex items-center gap-2"><Shield className="h-4 w-4" />Sécurité</CardTitle></CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium">Authentification à deux facteurs</p>
              <p className="text-xs text-muted-foreground">Ajouter une couche de sécurité supplémentaire</p>
            </div>
            <Switch />
          </div>
          <Separator />
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium">Sessions actives</p>
              <p className="text-xs text-muted-foreground">Gérer vos sessions connectées</p>
            </div>
            <Button variant="outline" size="sm">Voir les sessions</Button>
          </div>
        </CardContent>
      </Card>

      <Card className="border-border/50">
        <CardHeader><CardTitle className="text-base flex items-center gap-2"><Bell className="h-4 w-4" />Notifications</CardTitle></CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium">Vulnérabilités critiques</p>
              <p className="text-xs text-muted-foreground">Notification immédiate par email</p>
            </div>
            <Switch defaultChecked />
          </div>
          <Separator />
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium">Rapports hebdomadaires</p>
              <p className="text-xs text-muted-foreground">Résumé de sécurité chaque lundi</p>
            </div>
            <Switch defaultChecked />
          </div>
          <Separator />
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium">Scan terminé</p>
              <p className="text-xs text-muted-foreground">Notification quand un scan est complété</p>
            </div>
            <Switch />
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
