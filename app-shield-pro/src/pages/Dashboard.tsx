import { ShieldCheck, ShieldAlert, FolderGit2, Bug, TrendingDown } from "lucide-react";
import { StatCard } from "@/components/StatCard";
import { SeverityBadge } from "@/components/SeverityBadge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { vulnTrendData, severityDistribution, vulnerabilities, scans } from "@/lib/mock-data";
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from "recharts";

export default function Dashboard() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold font-heading">Dashboard</h1>
        <p className="text-muted-foreground text-sm">Vue globale de la sécurité de vos applications</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard title="Projets analysés" value={5} icon={FolderGit2} trend="+2 ce mois" trendUp />
        <StatCard title="Vulnérabilités" value={28} icon={Bug} trend="-12% vs. mois dernier" trendUp />
        <StatCard title="Score global" value="72%" icon={ShieldCheck} iconClassName="bg-success/10 text-success" />
        <StatCard title="Critiques" value={3} icon={ShieldAlert} iconClassName="bg-destructive/10 text-destructive" trend="Action requise" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <Card className="lg:col-span-2 border-border/50">
          <CardHeader className="pb-2">
            <CardTitle className="text-base flex items-center gap-2">
              <TrendingDown className="h-4 w-4 text-primary" />
              Évolution des vulnérabilités
            </CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={280}>
              <AreaChart data={vulnTrendData}>
                <defs>
                  <linearGradient id="criticalGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="hsl(0, 80%, 55%)" stopOpacity={0.3} />
                    <stop offset="100%" stopColor="hsl(0, 80%, 55%)" stopOpacity={0} />
                  </linearGradient>
                  <linearGradient id="highGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="hsl(25, 95%, 53%)" stopOpacity={0.3} />
                    <stop offset="100%" stopColor="hsl(25, 95%, 53%)" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(220, 15%, 20%)" />
                <XAxis dataKey="month" stroke="hsl(215, 15%, 55%)" fontSize={12} />
                <YAxis stroke="hsl(215, 15%, 55%)" fontSize={12} />
                <Tooltip contentStyle={{ backgroundColor: "hsl(220, 20%, 12%)", border: "1px solid hsl(220, 15%, 20%)", borderRadius: "8px", color: "hsl(210, 20%, 90%)" }} />
                <Area type="monotone" dataKey="critical" stroke="hsl(0, 80%, 55%)" fill="url(#criticalGrad)" strokeWidth={2} />
                <Area type="monotone" dataKey="high" stroke="hsl(25, 95%, 53%)" fill="url(#highGrad)" strokeWidth={2} />
                <Area type="monotone" dataKey="medium" stroke="hsl(45, 95%, 50%)" fill="none" strokeWidth={2} strokeDasharray="5 5" />
                <Area type="monotone" dataKey="low" stroke="hsl(200, 60%, 55%)" fill="none" strokeWidth={1.5} strokeDasharray="3 3" />
              </AreaChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card className="border-border/50">
          <CardHeader className="pb-2">
            <CardTitle className="text-base">Répartition par sévérité</CardTitle>
          </CardHeader>
          <CardContent>
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
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <Card className="border-border/50">
          <CardHeader className="pb-2">
            <CardTitle className="text-base">Derniers scans</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {scans.slice(0, 4).map((s) => (
                <div key={s.id} className="flex items-center justify-between p-3 rounded-lg bg-muted/30">
                  <div>
                    <p className="text-sm font-medium">{s.project}</p>
                    <p className="text-xs text-muted-foreground">{s.type} · {s.date}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-mono">{s.vulnerabilities} vulns</p>
                    <p className="text-xs text-muted-foreground">{s.duration}</p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card className="border-border/50">
          <CardHeader className="pb-2">
            <CardTitle className="text-base">Dernières vulnérabilités</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {vulnerabilities.slice(0, 4).map((v) => (
                <div key={v.id} className="flex items-center justify-between p-3 rounded-lg bg-muted/30">
                  <div>
                    <p className="text-sm font-medium">{v.name}</p>
                    <p className="text-xs text-muted-foreground">{v.project} · {v.date}</p>
                  </div>
                  <SeverityBadge severity={v.severity} />
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
