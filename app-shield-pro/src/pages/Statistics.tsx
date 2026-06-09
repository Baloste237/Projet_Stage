import { useQuery } from "@tanstack/react-query";
import { BarChart3, ShieldAlert, Smartphone, Globe, ShieldCheck, AlertTriangle } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { fetchWebStats, fetchMobileStats } from "@/services/statsService";
import { StatsSkeletonLoader } from "@/components/StatsSkeletonLoader";
import { StatCard } from "@/components/StatCard";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, LineChart, Line, Legend,
  Radar, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis
} from "recharts";

// Colors corresponding to severity levels
const COLORS = {
  CRITICAL: "hsl(0, 80%, 55%)",
  HIGH: "hsl(25, 95%, 53%)",
  MEDIUM: "hsl(45, 95%, 50%)",
  LOW: "hsl(200, 60%, 55%)"
};

export default function Statistics() {
  const { data: webStats, isLoading: isWebLoading } = useQuery({
    queryKey: ["stats", "web"],
    queryFn: fetchWebStats,
  });

  const { data: mobileStats, isLoading: isMobileLoading } = useQuery({
    queryKey: ["stats", "mobile"],
    queryFn: fetchMobileStats,
  });

  if (isWebLoading || isMobileLoading) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-bold font-heading flex items-center gap-2">
            <BarChart3 className="h-6 w-6 text-primary" />Statistiques
          </h1>
          <p className="text-muted-foreground text-sm">Analyse détaillée de la sécurité de vos applications Web et Mobiles</p>
        </div>
        <StatsSkeletonLoader />
      </div>
    );
  }

  // Formatting Pie Chart Data
  const webPieData = [
    { name: "Critique", value: webStats?.criticalCount || 0, color: COLORS.CRITICAL },
    { name: "Élevé", value: webStats?.highCount || 0, color: COLORS.HIGH },
    { name: "Moyen", value: webStats?.mediumCount || 0, color: COLORS.MEDIUM },
    { name: "Faible", value: webStats?.lowCount || 0, color: COLORS.LOW },
  ].filter(item => item.value > 0);

  const mobilePieData = [
    { name: "Critique", value: mobileStats?.criticalCount || 0, color: COLORS.CRITICAL },
    { name: "Élevé", value: mobileStats?.highCount || 0, color: COLORS.HIGH },
    { name: "Moyen", value: mobileStats?.mediumCount || 0, color: COLORS.MEDIUM },
    { name: "Faible", value: mobileStats?.lowCount || 0, color: COLORS.LOW },
  ].filter(item => item.value > 0);

  return (
    <div className="space-y-10">
      <div>
        <h1 className="text-2xl font-bold font-heading flex items-center gap-2">
          <BarChart3 className="h-6 w-6 text-primary" />Statistiques de Sécurité
        </h1>
        <p className="text-muted-foreground text-sm">Tableau de bord interactif des vulnérabilités Web et Mobiles</p>
      </div>

      {/* ---------------------------------------------------- */}
      {/* PARTIE 1: STATISTIQUES WEB */}
      {/* ---------------------------------------------------- */}
      <section className="space-y-6">
        <div className="flex items-center gap-2 border-b border-border/50 pb-2">
          <Globe className="h-5 w-5 text-blue-500" />
          <h2 className="text-xl font-semibold text-blue-500">OWASP Top Web Vulnerabilities</h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard
            title="Total Vulnérabilités Web"
            value={webStats?.totalVulnerabilities || 0}
            icon={ShieldAlert}
            iconClassName="bg-blue-500/10 text-blue-500"
          />
          <StatCard
            title="Vulnérabilités Critiques"
            value={webStats?.criticalCount || 0}
            icon={AlertTriangle}
            iconClassName="bg-destructive/10 text-destructive"
          />
          <StatCard
            title="Scans Effectués"
            value={webStats?.totalScans || 0}
            icon={ShieldCheck}
            iconClassName="bg-success/10 text-success"
          />
          <StatCard
            title="Projet le plus vulnérable"
            value={webStats?.mostVulnerableProject || "N/A"}
            icon={BarChart3}
            iconClassName="bg-warning/10 text-warning"
          />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {/* Répartition par Sévérité - Web */}
          <Card className="border-border/50">
            <CardHeader className="pb-2"><CardTitle className="text-base">Répartition par Sévérité (Web)</CardTitle></CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                {webPieData.length > 0 ? (
                  <PieChart>
                    <Pie data={webPieData} cx="50%" cy="50%" innerRadius={60} outerRadius={100} paddingAngle={5} dataKey="value">
                      {webPieData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip contentStyle={{ backgroundColor: "hsl(220, 20%, 12%)", borderColor: "hsl(220, 15%, 20%)", color: "#fff" }} />
                    <Legend />
                  </PieChart>
                ) : (
                  <div className="h-full flex items-center justify-center text-muted-foreground">Aucune donnée disponible</div>
                )}
              </ResponsiveContainer>
            </CardContent>
          </Card>

          {/* Évolution des scans - Web */}
          <Card className="border-border/50">
            <CardHeader className="pb-2"><CardTitle className="text-base">Évolution des Vulnérabilités Web (12 mois)</CardTitle></CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                {webStats?.scanTrend && webStats.scanTrend.length > 0 ? (
                  <LineChart data={webStats.scanTrend}>
                    <CartesianGrid strokeDasharray="3 3" stroke="hsl(220, 15%, 20%)" />
                    <XAxis dataKey="period" stroke="hsl(215, 15%, 55%)" fontSize={12} />
                    <YAxis stroke="hsl(215, 15%, 55%)" fontSize={12} />
                    <Tooltip contentStyle={{ backgroundColor: "hsl(220, 20%, 12%)", borderColor: "hsl(220, 15%, 20%)" }} />
                    <Legend />
                    <Line type="monotone" dataKey="criticalCount" name="Critique" stroke={COLORS.CRITICAL} strokeWidth={2} />
                    <Line type="monotone" dataKey="highCount" name="Élevé" stroke={COLORS.HIGH} strokeWidth={2} />
                    <Line type="monotone" dataKey="mediumCount" name="Moyen" stroke={COLORS.MEDIUM} strokeWidth={2} />
                  </LineChart>
                ) : (
                  <div className="h-full flex items-center justify-center text-muted-foreground">Aucune donnée disponible</div>
                )}
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </div>

        {/* Bar Chart OWASP Categories - Web */}
        <Card className="border-border/50">
          <CardHeader className="pb-2"><CardTitle className="text-base">Vulnérabilités par Catégorie OWASP Web</CardTitle></CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={350}>
              {webStats?.owaspCategories && webStats.owaspCategories.length > 0 ? (
                <BarChart data={webStats.owaspCategories} layout="vertical" margin={{ left: 100 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="hsl(220, 15%, 20%)" horizontal={false} />
                  <XAxis type="number" stroke="hsl(215, 15%, 55%)" />
                  <YAxis type="category" dataKey="categoryName" stroke="hsl(215, 15%, 55%)" fontSize={11} width={150} />
                  <Tooltip contentStyle={{ backgroundColor: "hsl(220, 20%, 12%)", borderColor: "hsl(220, 15%, 20%)" }} />
                  <Legend />
                  <Bar dataKey="criticalCount" name="Critique" stackId="a" fill={COLORS.CRITICAL} />
                  <Bar dataKey="highCount" name="Élevé" stackId="a" fill={COLORS.HIGH} />
                  <Bar dataKey="mediumCount" name="Moyen" stackId="a" fill={COLORS.MEDIUM} />
                  <Bar dataKey="lowCount" name="Faible" stackId="a" fill={COLORS.LOW} />
                </BarChart>
              ) : (
                <div className="h-full flex items-center justify-center text-muted-foreground">Aucune donnée disponible</div>
              )}
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </section>

      {/* ---------------------------------------------------- */}
      {/* PARTIE 2: STATISTIQUES MOBILE */}
      {/* ---------------------------------------------------- */}
      <section className="space-y-6">
        <div className="flex items-center gap-2 border-b border-border/50 pb-2 mt-8">
          <Smartphone className="h-5 w-5 text-purple-500" />
          <h2 className="text-xl font-semibold text-purple-500">OWASP Mobile Top 10 (2024)</h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard
            title="Total Vulnérabilités Mobile"
            value={mobileStats?.totalVulnerabilities || 0}
            icon={ShieldAlert}
            iconClassName="bg-purple-500/10 text-purple-500"
          />
          <StatCard
            title="Vulnérabilités Critiques"
            value={mobileStats?.criticalCount || 0}
            icon={AlertTriangle}
            iconClassName="bg-destructive/10 text-destructive"
          />
          <StatCard
            title="APK Analysés"
            value={mobileStats?.totalApkAnalyzed || 0}
            icon={Smartphone}
            iconClassName="bg-primary/10 text-primary"
          />
          <StatCard
            title="Risque Global Moyen"
            value={mobileStats?.averageRiskScore ? `${mobileStats.averageRiskScore}/10` : "0/10"}
            icon={BarChart3}
            iconClassName="bg-warning/10 text-warning"
          />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {/* Répartition par Sévérité - Mobile */}
          <Card className="border-border/50">
            <CardHeader className="pb-2"><CardTitle className="text-base">Répartition par Sévérité (Mobile)</CardTitle></CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                {mobilePieData.length > 0 ? (
                  <PieChart>
                    <Pie data={mobilePieData} cx="50%" cy="50%" innerRadius={60} outerRadius={100} paddingAngle={5} dataKey="value">
                      {mobilePieData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip contentStyle={{ backgroundColor: "hsl(220, 20%, 12%)", borderColor: "hsl(220, 15%, 20%)", color: "#fff" }} />
                    <Legend />
                  </PieChart>
                ) : (
                  <div className="h-full flex items-center justify-center text-muted-foreground">Aucune donnée disponible</div>
                )}
              </ResponsiveContainer>
            </CardContent>
          </Card>

          {/* Évolution des scans - Mobile */}
          <Card className="border-border/50">
            <CardHeader className="pb-2"><CardTitle className="text-base">Évolution des Vulnérabilités Mobile (12 mois)</CardTitle></CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                {mobileStats?.scanTrend && mobileStats.scanTrend.length > 0 ? (
                  <LineChart data={mobileStats.scanTrend}>
                    <CartesianGrid strokeDasharray="3 3" stroke="hsl(220, 15%, 20%)" />
                    <XAxis dataKey="period" stroke="hsl(215, 15%, 55%)" fontSize={12} />
                    <YAxis stroke="hsl(215, 15%, 55%)" fontSize={12} />
                    <Tooltip contentStyle={{ backgroundColor: "hsl(220, 20%, 12%)", borderColor: "hsl(220, 15%, 20%)" }} />
                    <Legend />
                    <Line type="monotone" dataKey="criticalCount" name="Critique" stroke={COLORS.CRITICAL} strokeWidth={2} />
                    <Line type="monotone" dataKey="highCount" name="Élevé" stroke={COLORS.HIGH} strokeWidth={2} />
                    <Line type="monotone" dataKey="mediumCount" name="Moyen" stroke={COLORS.MEDIUM} strokeWidth={2} />
                  </LineChart>
                ) : (
                  <div className="h-full flex items-center justify-center text-muted-foreground">Aucune donnée disponible</div>
                )}
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </div>

        {/* Graphiques OWASP Categories - Mobile */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <Card className="border-border/50">
            <CardHeader className="pb-2"><CardTitle className="text-base">Vulnérabilités par Catégorie OWASP Mobile (Barres)</CardTitle></CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={400}>
                {mobileStats?.owaspCategories && mobileStats.owaspCategories.length > 0 ? (
                  <BarChart data={mobileStats.owaspCategories} layout="vertical" margin={{ left: 100 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="hsl(220, 15%, 20%)" horizontal={false} />
                    <XAxis type="number" stroke="hsl(215, 15%, 55%)" />
                    <YAxis type="category" dataKey="categoryName" stroke="hsl(215, 15%, 55%)" fontSize={11} width={180} />
                    <Tooltip contentStyle={{ backgroundColor: "hsl(220, 20%, 12%)", borderColor: "hsl(220, 15%, 20%)" }} />
                    <Legend />
                    <Bar dataKey="criticalCount" name="Critique" stackId="a" fill={COLORS.CRITICAL} />
                    <Bar dataKey="highCount" name="Élevé" stackId="a" fill={COLORS.HIGH} />
                    <Bar dataKey="mediumCount" name="Moyen" stackId="a" fill={COLORS.MEDIUM} />
                    <Bar dataKey="lowCount" name="Faible" stackId="a" fill={COLORS.LOW} />
                  </BarChart>
                ) : (
                  <div className="h-full flex items-center justify-center text-muted-foreground">Aucune donnée disponible</div>
                )}
              </ResponsiveContainer>
            </CardContent>
          </Card>

          <Card className="border-border/50">
            <CardHeader className="pb-2"><CardTitle className="text-base">Profil de Sécurité OWASP (Radar)</CardTitle></CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={400}>
                {mobileStats?.owaspCategories && mobileStats.owaspCategories.length > 0 ? (
                  <RadarChart cx="50%" cy="50%" outerRadius="80%" data={mobileStats.owaspCategories}>
                    <PolarGrid stroke="hsl(215, 15%, 30%)" />
                    <PolarAngleAxis dataKey="categoryId" stroke="hsl(215, 15%, 55%)" fontSize={12} />
                    <PolarRadiusAxis angle={30} domain={[0, 'auto']} stroke="hsl(215, 15%, 55%)" />
                    <Radar name="Total Vulnérabilités" dataKey="count" stroke={COLORS.HIGH} fill={COLORS.HIGH} fillOpacity={0.4} />
                    <Tooltip contentStyle={{ backgroundColor: "hsl(220, 20%, 12%)", borderColor: "hsl(220, 15%, 20%)", color: "#fff" }} />
                    <Legend />
                  </RadarChart>
                ) : (
                  <div className="h-full flex items-center justify-center text-muted-foreground">Aucune donnée disponible</div>
                )}
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </div>
      </section>
    </div>
  );
}
