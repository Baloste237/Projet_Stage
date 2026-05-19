import React, { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Separator } from "@/components/ui/separator";
import { Settings as SettingsIcon, Users, Shield, Bell, ScanLine, Clock } from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";

export default function SettingsPage() {
  const [userData, setUserData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch("/api/auth/me", {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("vulnscan-token")}`
      }
    })
      .then(res => {
        if (!res.ok) throw new Error("Failed to fetch user");
        return res.json();
      })
      .then(data => {
        setUserData(data);
        setLoading(false);
      })
      .catch(err => {
        console.error(err);
        setLoading(false);
      });
  }, []);

  return (
    <div className="space-y-6 max-w-3xl">
      <div>
        <h1 className="text-2xl font-bold font-heading flex items-center gap-2">
          <SettingsIcon className="h-6 w-6 text-primary" />Paramètres du compte
        </h1>
        <p className="text-muted-foreground text-sm">Gérez vos informations personnelles et préférences de sécurité.</p>
      </div>

      <Card className="border-border/50">
        <CardHeader><CardTitle className="text-base flex items-center gap-2"><Users className="h-4 w-4" />Informations utilisateur</CardTitle></CardHeader>
        <CardContent className="space-y-4">
          {loading ? (
            <div className="space-y-4">
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-1/2" />
            </div>
          ) : userData ? (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Nom d'utilisateur</Label>
                <Input value={userData.username} readOnly className="bg-muted/50 cursor-not-allowed" />
              </div>
              <div className="space-y-2">
                <Label>Email</Label>
                <Input value={userData.email} readOnly className="bg-muted/50 cursor-not-allowed" />
              </div>
              <div className="space-y-2">
                <Label>Rôle actuel</Label>
                <div className="p-2 border rounded-md bg-primary/5 text-primary font-semibold w-fit text-sm">
                  {userData.role.replace("ROLE_", "")}
                </div>
              </div>
            </div>
          ) : (
            <p className="text-destructive">Impossible de charger les données utilisateur.</p>
          )}
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
