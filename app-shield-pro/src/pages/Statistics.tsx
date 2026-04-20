import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { vulnTrendData, projects } from "@/lib/mock-data";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar } from "recharts";
import { BarChart3 } from "lucide-react";

const projectScores = projects.map((p) => ({ name: p.name.split(" ")[0], score: p.score, vulns: p.vulnerabilities }));

export default function Statistics() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold font-heading flex items-center gap-2">
          <BarChart3 className="h-6 w-6 text-primary" />Statistiques
        </h1>
        <p className="text-muted-foreground text-sm">Analyse détaillée de la sécurité de vos projets</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <Card className="border-border/50">
          <CardHeader className="pb-2"><CardTitle className="text-base">Vulnérabilités par mois</CardTitle></CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={vulnTrendData}>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(220, 15%, 20%)" />
                <XAxis dataKey="month" stroke="hsl(215, 15%, 55%)" fontSize={12} />
                <YAxis stroke="hsl(215, 15%, 55%)" fontSize={12} />
                <Tooltip contentStyle={{ backgroundColor: "hsl(220, 20%, 12%)", border: "1px solid hsl(220, 15%, 20%)", borderRadius: "8px", color: "hsl(210, 20%, 90%)" }} />
                <Bar dataKey="critical" fill="hsl(0, 80%, 55%)" radius={[2, 2, 0, 0]} />
                <Bar dataKey="high" fill="hsl(25, 95%, 53%)" radius={[2, 2, 0, 0]} />
                <Bar dataKey="medium" fill="hsl(45, 95%, 50%)" radius={[2, 2, 0, 0]} />
                <Bar dataKey="low" fill="hsl(200, 60%, 55%)" radius={[2, 2, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card className="border-border/50">
          <CardHeader className="pb-2"><CardTitle className="text-base">Comparaison des projets</CardTitle></CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <RadarChart data={projectScores}>
                <PolarGrid stroke="hsl(220, 15%, 20%)" />
                <PolarAngleAxis dataKey="name" stroke="hsl(215, 15%, 55%)" fontSize={11} />
                <PolarRadiusAxis stroke="hsl(215, 15%, 55%)" fontSize={10} />
                <Radar name="Score" dataKey="score" stroke="hsl(190, 95%, 50%)" fill="hsl(190, 95%, 50%)" fillOpacity={0.2} />
              </RadarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card className="lg:col-span-2 border-border/50">
          <CardHeader className="pb-2"><CardTitle className="text-base">Score de sécurité par projet</CardTitle></CardHeader>
          <CardContent>
            <div className="space-y-4">
              {projects.map((p) => (
                <div key={p.id} className="flex items-center gap-4">
                  <span className="text-sm w-36 truncate">{p.name}</span>
                  <div className="flex-1 h-3 bg-muted/50 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all ${p.score >= 80 ? "bg-success" : p.score >= 60 ? "bg-severity-medium" : "bg-destructive"}`}
                      style={{ width: `${p.score}%` }}
                    />
                  </div>
                  <span className="text-sm font-mono w-12 text-right">{p.score}%</span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
