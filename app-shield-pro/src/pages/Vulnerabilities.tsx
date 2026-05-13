import { useState, useEffect } from "react";
import { SeverityBadge } from "@/components/SeverityBadge";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Badge } from "@/components/ui/badge";
import { Search, ShieldAlert } from "lucide-react";
import { toast } from "sonner";
import { Pagination } from "@/components/Pagination";

export default function Vulnerabilities() {
  const [severity, setSeverity] = useState("all");
  const [projectFilter, setProjectFilter] = useState("all");
  const [search, setSearch] = useState("");
  const [vulnerabilities, setVulnerabilities] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  // Pagination state
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    const fetchVulns = async () => {
      setLoading(true);
      try {
        const token = localStorage.getItem("vulnscan-token");
        const sevQuery = severity !== "all" ? `&severity=${severity.toUpperCase()}` : "";
        const res = await fetch(`/api/v1/vulnerabilities?page=${page}&size=${pageSize}${sevQuery}`, {
          headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.ok) {
          const data = await res.json();
          setVulnerabilities(data.content);
          setTotalPages(data.totalPages);
        } else {
          toast.error("Échec de la récupération des vulnérabilités");
        }
      } catch (e) {
        toast.error("Erreur de connexion au serveur");
      } finally {
        setLoading(false);
      }
    };
    fetchVulns();
  }, [severity, page, pageSize]);

  // Filtres texte et projet appliqués côté front pour la fluidité
  const filtered = vulnerabilities.filter((v) => {
    const projName = v.scan?.projectName || "Inconnu";
    if (projectFilter !== "all" && projName !== projectFilter) return false;
    if (search && !v.type?.toLowerCase().includes(search.toLowerCase()) && !v.cweId?.toLowerCase().includes(search.toLowerCase())) return false;
    return true;
  });

  const uniqueProjects = [...new Set(vulnerabilities.map((v) => v.scan?.projectName || "Inconnu"))];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold font-heading flex items-center gap-2 african-heading">
          <ShieldAlert className="h-7 w-7 text-primary" />
          Toutes les Vulnérabilités
        </h1>
        <p className="text-muted-foreground mt-1">Gestion centralisée des alertes de tous vos scans</p>
      </div>

      <div className="flex flex-wrap gap-3">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Rechercher par type ou CWE..."
            className="pl-9 w-64"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <Select value={severity} onValueChange={(v) => { setSeverity(v); setPage(0); }}>
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
            {uniqueProjects.map((p) => (
              <SelectItem key={String(p)} value={String(p)}>{p}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {loading && (
        <div className="p-4 text-center text-muted-foreground">Recherche en cours...</div>
      )}

      {!loading && (
        <div className="space-y-3">
          {filtered.length === 0 && (
            <p className="text-muted-foreground text-center py-6">Aucune vulnérabilité trouvée.</p>
          )}

          {filtered.map((v) => (
            <Dialog key={v.id}>
              <DialogTrigger asChild>
                <Card className="card-hover border-border/50 cursor-pointer">
                  <CardContent className="p-4 flex items-center justify-between flex-wrap gap-2">
                    <div className="flex items-center gap-4">
                      <SeverityBadge severity={v.niv_grav} />
                      <div>
                        <p className="font-medium text-sm">{v.type}</p>
                        <p className="text-xs text-muted-foreground">
                          {v.scan?.projectName || "Inconnu"} · Cible: {v.scan?.fileName || "ND"} · CWE: {v.cweId}
                        </p>
                      </div>
                    </div>
                    <Badge variant="outline" className="font-mono text-xs">
                      {v.scan?.status === "COMPLETED" ? "Traité" : "Analyse en cours"}
                    </Badge>
                  </CardContent>
                </Card>
              </DialogTrigger>
              <DialogContent className="max-w-xl max-h-[80vh] overflow-y-auto">
                <DialogHeader>
                  <DialogTitle className="flex items-center gap-2">
                    <SeverityBadge severity={v.niv_grav} />
                    {v.type}
                  </DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                  <div>
                    <h4 className="text-sm font-semibold mb-1">Description</h4>
                    <p className="text-sm text-muted-foreground leading-relaxed">{v.description}</p>
                  </div>
                  {(v.targetFile || v.targetLine) && (
                    <div>
                      <h4 className="text-sm font-semibold mb-1">Fichier impacté</h4>
                      <div className="p-3 rounded-lg bg-muted/50 text-xs font-mono">
                        <span className="text-primary">{v.targetFile || "Fichier inconnu"}</span>
                        {v.targetLine && (
                          <span className="text-muted-foreground ml-2">Ligne: {v.targetLine}</span>
                        )}
                      </div>
                    </div>
                  )}
                  <div className="flex gap-4">
                    <div>
                      <h4 className="text-sm font-semibold mb-1">CWE</h4>
                      <Badge>{v.cweId}</Badge>
                    </div>
                    <div>
                      <h4 className="text-sm font-semibold mb-1">Score CVSS</h4>
                      <Badge variant="outline">{v.cvssScore}</Badge>
                    </div>
                  </div>
                </div>
              </DialogContent>
            </Dialog>
          ))}

          {totalPages > 1 && (
            <Pagination
              currentPage={page}
              totalPages={totalPages}
              onPageChange={setPage}
              pageSize={pageSize}
              onPageSizeChange={(newSize) => {
                setPageSize(newSize);
                setPage(0);
              }}
            />
          )}
        </div>
      )}
    </div>
  );
}
