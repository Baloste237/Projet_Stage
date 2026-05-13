import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { SeverityBadge } from "@/components/SeverityBadge";
import { ArrowLeft, ShieldAlert } from "lucide-react";
import { toast } from "sonner";

export default function ScanVulnerabilities() {
  const { scanId } = useParams();
  const navigate = useNavigate();
  const [vulns, setVulns] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchVulns = async () => {
      try {
        const token = localStorage.getItem("vulnscan-token");
        const res = await fetch(`/api/v1/vulnerabilities?scanId=${scanId}`, {
          headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.ok) {
          const data = await res.json();
          // The API now returns a Page object { content: [...], ... }
          const vulnerabilityList = data.content || data;
          
          // Sort descending severity roughly
          vulnerabilityList.sort((a: any, b: any) => {
             const weights: any = { CRITICAL: 4, HIGH: 3, MEDIUM: 2, LOW: 1, INFO: 0 };
             return (weights[b.niv_grav] || 0) - (weights[a.niv_grav] || 0);
          });
          setVulns(vulnerabilityList);
        } else {
          toast.error("Erreur de récupération des détails");
        }
      } catch (e) {
        console.error("Error fetching", e);
      } finally {
        setLoading(false);
      }
    };
    fetchVulns();
  }, [scanId]);

  if (loading) return <div className="p-4">Chargement...</div>;

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="outline" size="icon" className="btn-african" onClick={() => navigate("/reports")}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold font-heading flex items-center gap-2 african-heading">
            <ShieldAlert className="h-7 w-7 text-primary" />Vulnérabilités du Scan #{scanId}
          </h1>
          <p className="text-muted-foreground mt-1">{vulns.length} vulnérabilités détectées au total.</p>
        </div>
      </div>

      <div className="space-y-3">
        {vulns.length === 0 ? (
          <p className="text-center py-10 text-muted-foreground">Aucune vulnérabilité n'a été recensée pour ce scan.</p>
        ) : vulns.map((v) => (
          <Card key={v.id} className="card-hover border-border/50">
            <CardContent className="p-4 flex flex-col gap-3">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <SeverityBadge severity={v.niv_grav} />
                  <h3 className="font-semibold text-base">{v.type}</h3>
                </div>
                <div className="flex gap-2">
                  <Badge variant="outline">CVSS: {v.cvssScore}</Badge>
                  <Badge variant="outline">{v.cweId}</Badge>
                </div>
              </div>
              <p className="text-sm text-muted-foreground leading-relaxed">{v.description}</p>
              
              <div className="bg-muted/30 rounded-md p-3 mt-2 flex items-center justify-between text-xs font-mono">
                <span className="text-primary truncate" title={v.targetFile}>📁 {v.targetFile || "Code base"}</span>
                {v.targetLine && <span className="text-muted-foreground">Ligne: {v.targetLine}</span>}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
