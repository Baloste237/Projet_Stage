import { useState, useEffect } from "react";
import { ShieldCheck, ShieldAlert, FolderGit2, Bug, TrendingDown } from "lucide-react";
import { StatCard } from "@/components/StatCard";
import { SeverityBadge } from "@/components/SeverityBadge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { PieChart, Pie, Cell, Legend, Tooltip, ResponsiveContainer } from "recharts";

export default function Dashboard() {
  const [scans, setScans] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchScans = async () => {
      try {
        const token = localStorage.getItem("vulnscan-token");
        const res = await fetch("/api/v1/analyze?size=100", { // Load more for stats, but still paginated
          headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.ok) {
          const data = await res.json();
          const scanList = data.content || data;
          setScans(scanList);
        }
      } catch (e) {
        console.error("Erreur chargement scans dashboard:", e);
      } finally {
        setLoading(false);
      }
    };
    fetchScans();
  }, []);

  // Compute KPIs
  const totalScans = scans.length;
  const totalVulns = scans.reduce((acc, curr) => acc + (curr.totalVulnerabilities || 0), 0);
  const criticalVulns = scans.reduce((acc, curr) => acc + (curr.criticalCount || 0), 0);
  const highVulns = scans.reduce((acc, curr) => acc + (curr.highCount || 0), 0);
  const mediumVulns = scans.reduce((acc, curr) => acc + (curr.mediumCount || 0), 0);
  const lowVulns = scans.reduce((acc, curr) => acc + (curr.lowCount || 0), 0);

  // Score arbitraire = 100 - pénalités
  const penalty = (criticalVulns * 5) + (highVulns * 3) + (mediumVulns * 1.5) + (lowVulns * 0.5);
  const globalScore = Math.max(0, Math.round(100 - penalty));

  // Severity Distribution for PieChart
  const severityDistribution = [
    { name: "CRITICAL", value: criticalVulns, fill: "hsl(0, 80%, 55%)" },
    { name: "HIGH", value: highVulns, fill: "hsl(25, 95%, 53%)" },
    { name: "MEDIUM", value: mediumVulns, fill: "hsl(45, 95%, 50%)" },
    { name: "LOW", value: lowVulns, fill: "hsl(200, 60%, 55%)" },
  ].filter(d => d.value > 0);

  if (loading && scans.length === 0) return <div className="p-4">Chargement du Dashboard...</div>;

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold font-heading african-heading">Tableau de bord</h1>
          <p className="text-muted-foreground">Vue d'ensemble de la sécurité de vos applications.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard title="Projets analysés" value={totalScans} icon={FolderGit2} />
        <StatCard title="Vulnérabilités" value={totalVulns} icon={Bug} />
        <StatCard title="Score global" value={`${globalScore}%`} icon={ShieldCheck} iconClassName="bg-success/10 text-success" />
        <StatCard title="Critiques" value={criticalVulns} icon={ShieldAlert} iconClassName="bg-destructive/10 text-destructive" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        
        {/* PieChart des sévérités globales */}
        <Card className="lg:col-span-1 border-border/50">
          <CardHeader className="pb-2">
            <CardTitle className="text-base">Répartition globale des sévérités</CardTitle>
          </CardHeader>
          <CardContent>
            {severityDistribution.length === 0 ? (
              <p className="text-sm text-center text-muted-foreground mt-10">Aucune vulnérabilité trouvée.</p>
            ) : (
              <ResponsiveContainer width="100%" height={280}>
                <PieChart>
                  <Pie data={severityDistribution} cx="50%" cy="50%" innerRadius={55} outerRadius={85} paddingAngle={4} dataKey="value">
                    {severityDistribution.map((entry, i) => (
                      <Cell key={i} fill={entry.fill} />
                    ))}
                  </Pie>
                  <Legend wrapperStyle={{ fontSize: "12px" }} />
                  <Tooltip contentStyle={{ backgroundColor: "hsl(220, 20%, 12%)", border: "1px solid hsl(220, 15%, 20%)", borderRadius: "8px", color: "hsl(210, 20%, 90%)" }} />
                </PieChart>
              </ResponsiveContainer>
            )}
          </CardContent>
        </Card>

        {/* Derniers scans */}
        <Card className="lg:col-span-2 border-border/50">
          <CardHeader className="pb-2">
            <CardTitle className="text-base">Historique récent ({scans.length} scans totaux)</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3 mt-4">
              {scans.slice(0, 5).map((s) => (
                <div key={s.id} className="flex items-center justify-between p-3 rounded-lg bg-muted/30">
                  <div>
                    <p className="text-sm font-medium">{s.projectName}</p>
                    <p className="text-xs text-muted-foreground">{s.scanType} · {new Date(s.createdAt).toLocaleString()}</p>
                  </div>
                  <div className="text-right">
                    <p className={`text-sm font-mono ${s.totalVulnerabilities > 0 ? "text-destructive" : "text-success"}`}>
                      {s.totalVulnerabilities} vulns
                    </p>
                    <p className="text-xs text-muted-foreground">Statut: {s.status}</p>
                  </div>
                </div>
              ))}
              {scans.length === 0 && (
                <p className="text-sm text-muted-foreground">Aucun scan dans la base de données.</p>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
