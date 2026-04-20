import { useState } from "react";
import { vulnerabilities } from "@/lib/mock-data";
import { SeverityBadge } from "@/components/SeverityBadge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Badge } from "@/components/ui/badge";
import { Search, ShieldAlert } from "lucide-react";

export default function Vulnerabilities() {
  const [severity, setSeverity] = useState("all");
  const [projectFilter, setProjectFilter] = useState("all");
  const [search, setSearch] = useState("");

  const filtered = vulnerabilities.filter((v) => {
    if (severity !== "all" && v.severity !== severity) return false;
    if (projectFilter !== "all" && v.project !== projectFilter) return false;
    if (search && !v.name.toLowerCase().includes(search.toLowerCase())) return false;
    return true;
  });

  const uniqueProjects = [...new Set(vulnerabilities.map((v) => v.project))];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold font-heading flex items-center gap-2">
          <ShieldAlert className="h-6 w-6 text-primary" />Vulnérabilités
        </h1>
        <p className="text-muted-foreground text-sm">{vulnerabilities.length} vulnérabilités détectées</p>
      </div>

      <div className="flex flex-wrap gap-3">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input placeholder="Rechercher..." className="pl-9 w-56" value={search} onChange={(e) => setSearch(e.target.value)} />
        </div>
        <Select value={severity} onValueChange={setSeverity}>
          <SelectTrigger className="w-40"><SelectValue /></SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Toutes sévérités</SelectItem>
            <SelectItem value="critical">Critique</SelectItem>
            <SelectItem value="high">Élevé</SelectItem>
            <SelectItem value="medium">Moyen</SelectItem>
            <SelectItem value="low">Faible</SelectItem>
          </SelectContent>
        </Select>
        <Select value={projectFilter} onValueChange={setProjectFilter}>
          <SelectTrigger className="w-44"><SelectValue /></SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tous les projets</SelectItem>
            {uniqueProjects.map((p) => <SelectItem key={p} value={p}>{p}</SelectItem>)}
          </SelectContent>
        </Select>
      </div>

      <div className="space-y-3">
        {filtered.map((v) => (
          <Dialog key={v.id}>
            <DialogTrigger asChild>
              <Card className="card-hover border-border/50 cursor-pointer">
                <CardContent className="p-4 flex items-center justify-between flex-wrap gap-2">
                  <div className="flex items-center gap-4">
                    <SeverityBadge severity={v.severity} />
                    <div>
                      <p className="font-medium text-sm">{v.name}</p>
                      <p className="text-xs text-muted-foreground">{v.project} · {v.type} · {v.date}</p>
                    </div>
                  </div>
                  <Badge variant="outline" className="font-mono text-xs">
                    {v.status === "open" ? "Ouvert" : v.status === "in_progress" ? "En cours" : "Résolu"}
                  </Badge>
                </CardContent>
              </Card>
            </DialogTrigger>
            <DialogContent className="max-w-lg">
              <DialogHeader><DialogTitle className="flex items-center gap-2"><SeverityBadge severity={v.severity} />{v.name}</DialogTitle></DialogHeader>
              <div className="space-y-4">
                <div>
                  <h4 className="text-sm font-semibold mb-1">Description</h4>
                  <p className="text-sm text-muted-foreground">{v.description}</p>
                </div>
                <div>
                  <h4 className="text-sm font-semibold mb-1">Code vulnérable</h4>
                  <pre className="p-3 rounded-lg bg-muted/50 text-xs font-mono overflow-x-auto">{v.code}</pre>
                </div>
                <div>
                  <h4 className="text-sm font-semibold mb-1">Recommandation</h4>
                  <p className="text-sm text-success">{v.recommendation}</p>
                </div>
              </div>
            </DialogContent>
          </Dialog>
        ))}
      </div>
    </div>
  );
}
