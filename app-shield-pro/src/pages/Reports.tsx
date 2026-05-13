import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { FileBarChart, Download, Eye, Shield, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { Pagination } from "@/components/Pagination";

export default function Reports() {
  const navigate = useNavigate();
  const [scans, setScans] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  // Pagination state
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);

  const fetchScans = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem("vulnscan-token");
      const res = await fetch(`/api/v1/analyze?page=${page}&size=${pageSize}`, {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
        setScans(data.content);
        setTotalPages(data.totalPages);
      }
    } catch (e) {
      console.error("Erreur chargement scans", e);
      toast.error("Erreur lors du chargement des rapports");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchScans();
  }, [page, pageSize]);

  const handleDownloadReport = async (scanId: number, format: 'pdf' | 'json') => {
    try {
      const token = localStorage.getItem("vulnscan-token");
      toast.info(`Génération du rapport ${format.toUpperCase()} en cours...`);
      const res = await fetch(`/api/v1/reports/${scanId}?format=${format}`, {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (!res.ok) { toast.error("Échec de la génération du rapport"); return; }
      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `Report_${scanId}.${format}`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      toast.success("Rapport téléchargé !");
    } catch (e) {
      toast.error("Erreur serveur lors du téléchargement");
    }
  };

  const handleDeleteReport = async (scanId: number) => {
    if (!window.confirm("Êtes-vous sûr de vouloir supprimer définitivement cet historique ?")) return;
    try {
      const token = localStorage.getItem("vulnscan-token");
      const res = await fetch(`/api/v1/reports/${scanId}`, {
        method: "DELETE",
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (res.ok) {
        toast.success("Rapport et historique supprimés");
        setScans(scans.filter((s) => s.id !== scanId));
      } else {
        toast.error("Échec de la suppression");
      }
    } catch (e) {
      toast.error("Erreur serveur lors de la suppression");
    }
  };

  if (loading) return <div className="p-4">Chargement de l'historique...</div>;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold font-heading flex items-center gap-2 african-heading">
          <FileBarChart className="h-7 w-7 text-primary" />
          Historique &amp; Rapports
        </h1>
        <p className="text-muted-foreground mt-1">Consultez et exportez vos rapports d'analyse en JSON ou PDF structuré</p>
      </div>

      <div className="space-y-4">
        {scans.length === 0 && (
          <p className="text-center text-muted-foreground py-10">Aucun scan dans l'historique.</p>
        )}

        {scans.map((s) => (
          <Card key={s.id} className="card-hover border-border/50">
            <CardContent className="p-5">
              <div className="flex items-center justify-between flex-wrap gap-4">
                <div className="flex items-center gap-4">
                  <div className="p-2.5 rounded-lg bg-primary/10">
                    <Shield className="h-5 w-5 text-primary" />
                  </div>
                  <div>
                    <h3 className="font-semibold">{s.projectName}</h3>
                    <p className="text-xs text-muted-foreground">{s.scanType} · {new Date(s.createdAt).toLocaleString()}</p>
                    <p className="text-xs text-muted-foreground mt-1 text-primary">Cible: {s.fileName}</p>
                  </div>
                </div>
                <div className="flex items-center gap-6">
                  <div className="text-center">
                    <p className={`text-lg font-bold ${s.totalVulnerabilities > 0 ? "text-destructive" : "text-success"}`}>
                      {s.totalVulnerabilities}
                    </p>
                    <p className="text-[10px] text-muted-foreground">Vulns</p>
                  </div>
                  <div className="text-center">
                    <p className={`text-lg font-bold ${s.status === 'COMPLETED' ? "text-success" : "text-warning"}`}>
                      {s.status}
                    </p>
                    <p className="text-[10px] text-muted-foreground">Statut</p>
                  </div>
                  <div className="flex gap-2 border-l border-border pl-6 flex-wrap justify-end max-w-[280px]">
                    <Button variant="secondary" size="sm" className="btn-african" onClick={() => navigate(`/scan/${s.id}/vulnerabilities`)}>
                      <Eye className="h-4 w-4 mr-2" /> Voir Vulns
                    </Button>
                    <Button variant="outline" size="sm" className="btn-african" onClick={() => handleDownloadReport(s.id, 'json')}>
                      <Download className="h-4 w-4 mr-2" /> JSON
                    </Button>
                    <Button variant="default" size="sm" className="btn-african" onClick={() => handleDownloadReport(s.id, 'pdf')}>
                      <Download className="h-4 w-4 mr-2" /> PDF
                    </Button>
                    <Button variant="destructive" size="sm" className="btn-african" onClick={() => handleDeleteReport(s.id)}>
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
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
    </div>
  );
}
