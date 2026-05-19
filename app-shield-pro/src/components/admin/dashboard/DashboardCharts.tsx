import React from "react";
import {
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  LineChart,
  Line,
  ResponsiveContainer,
  AreaChart,
  Area,
  RadialBarChart,
  RadialBar,
} from "recharts";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";

// Colors that match the security theme
const COLORS = {
  Critical: "#ef4444", // red-500
  High: "#f97316", // orange-500
  Medium: "#eab308", // yellow-500
  Low: "#3b82f6", // blue-500
  Info: "#8b5cf6", // violet-500
  Web: "#0ea5e9", // sky-500
  Mobile: "#10b981", // emerald-500
  Done: "#22c55e", // green-500
  Failed: "#ef4444", // red-500
  Processing: "#f59e0b", // amber-500
  Pending: "#64748b", // slate-500
};

// 2A. Répartition des vulnérabilités par sévérité
export const VulnerabilitySeverityChart = ({ data }: { data: Record<string, number> }) => {
  const chartData = Object.entries(data).map(([name, value]) => ({ name, value }));

  return (
    <Card className="col-span-1 shadow-md hover:shadow-lg transition-all duration-300">
      <CardHeader>
        <CardTitle>Vulnérabilités par Sévérité</CardTitle>
        <CardDescription>Répartition globale des failles détectées</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="h-[300px] w-full">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={chartData}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={100}
                paddingAngle={5}
                dataKey="value"
              >
                {chartData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[entry.name as keyof typeof COLORS] || "#ccc"} />
                ))}
              </Pie>
              <Tooltip
                contentStyle={{ borderRadius: "8px", backgroundColor: "rgba(0,0,0,0.8)", color: "#fff", border: "none" }}
              />
              <Legend verticalAlign="bottom" height={36} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
};

// 2B. Évolution des scans dans le temps
export const ScanEvolutionChart = ({ data }: { data: any[] }) => {
  // Reversing data to show oldest to newest (left to right) if needed, 
  // but assuming data comes sorted appropriately from backend
  const sortedData = [...data].sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());

  return (
    <Card className="col-span-1 lg:col-span-2 shadow-md hover:shadow-lg transition-all duration-300">
      <CardHeader>
        <CardTitle>Évolution de l'activité</CardTitle>
        <CardDescription>Nombre de scans et vulnérabilités sur les 7 derniers jours</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="h-[300px] w-full">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={sortedData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
              <defs>
                <linearGradient id="colorScans" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#8b5cf6" stopOpacity={0.8} />
                  <stop offset="95%" stopColor="#8b5cf6" stopOpacity={0} />
                </linearGradient>
                <linearGradient id="colorVulns" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#ef4444" stopOpacity={0.8} />
                  <stop offset="95%" stopColor="#ef4444" stopOpacity={0} />
                </linearGradient>
              </defs>
              <XAxis dataKey="date" stroke="#888888" fontSize={12} tickLine={false} axisLine={false} />
              <YAxis stroke="#888888" fontSize={12} tickLine={false} axisLine={false} />
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#333" opacity={0.2} />
              <Tooltip
                contentStyle={{ borderRadius: "8px", backgroundColor: "rgba(0,0,0,0.8)", color: "#fff", border: "none" }}
              />
              <Legend />
              <Area type="monotone" dataKey="scans" name="Scans lancés" stroke="#8b5cf6" fillOpacity={1} fill="url(#colorScans)" />
              <Area type="monotone" dataKey="vulns" name="Vulnérabilités" stroke="#ef4444" fillOpacity={1} fill="url(#colorVulns)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
};

// 2C. Vulnérabilités par type
export const VulnsByTypeChart = ({ data }: { data: any[] }) => {
  // data should be [{name: "CWE-79", value: 42}, ...]
  const chartData = data.map((d) => ({ name: d[0], value: d[1] }));

  return (
    <Card className="col-span-1 lg:col-span-2 shadow-md hover:shadow-lg transition-all duration-300">
      <CardHeader>
        <CardTitle>Top Types de Vulnérabilités</CardTitle>
        <CardDescription>Les catégories les plus fréquentes détectées</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="h-[300px] w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={chartData} layout="vertical" margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" horizontal={true} vertical={false} stroke="#333" opacity={0.2} />
              <XAxis type="number" stroke="#888888" fontSize={12} />
              <YAxis dataKey="name" type="category" stroke="#888888" fontSize={12} width={150} tick={{fill: '#888888'}} />
              <Tooltip
                cursor={{ fill: "rgba(255,255,255,0.1)" }}
                contentStyle={{ borderRadius: "8px", backgroundColor: "rgba(0,0,0,0.8)", color: "#fff", border: "none" }}
              />
              <Bar dataKey="value" name="Occurrences" fill="#f97316" radius={[0, 4, 4, 0]}>
                {chartData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={`hsl(24.6, 95%, ${Math.max(30, 70 - index * 10)}%)`} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
};

// 2D. Score global de sécurité
export const GlobalSecurityScoreChart = ({ score }: { score: number }) => {
  const data = [{ name: "Score", value: score, fill: score > 75 ? "#22c55e" : score > 50 ? "#eab308" : "#ef4444" }];

  return (
    <Card className="col-span-1 shadow-md hover:shadow-lg transition-all duration-300">
      <CardHeader>
        <CardTitle>Score Global de Sécurité</CardTitle>
        <CardDescription>Indice de santé globale de l'écosystème</CardDescription>
      </CardHeader>
      <CardContent className="flex justify-center items-center">
        <div className="h-[300px] w-full relative">
          <ResponsiveContainer width="100%" height="100%">
            <RadialBarChart cx="50%" cy="50%" innerRadius="70%" outerRadius="100%" barSize={20} data={data} startAngle={180} endAngle={0}>
              <RadialBar background={{ fill: 'rgba(0,0,0,0.1)' }} dataKey="value" cornerRadius={10} />
            </RadialBarChart>
          </ResponsiveContainer>
          <div className="absolute top-[60%] left-1/2 transform -translate-x-1/2 -translate-y-1/2 text-center">
            <span className="text-5xl font-extrabold" style={{ color: data[0].fill }}>
              {score}
            </span>
            <span className="text-muted-foreground text-xl">/100</span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

// 2E. Répartition Web vs Mobile
export const WebVsMobileChart = ({ webScans, mobileScans }: { webScans: number; mobileScans: number }) => {
  const data = [
    { name: "Web", value: webScans },
    { name: "Mobile", value: mobileScans },
  ];

  return (
    <Card className="col-span-1 shadow-md hover:shadow-lg transition-all duration-300">
      <CardHeader>
        <CardTitle>Répartition des Cibles</CardTitle>
        <CardDescription>Proportion des analyses Web vs Mobile</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="h-[300px] w-full">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie data={data} cx="50%" cy="50%" innerRadius={0} outerRadius={100} dataKey="value" label>
                <Cell fill={COLORS.Web} />
                <Cell fill={COLORS.Mobile} />
              </Pie>
              <Tooltip
                contentStyle={{ borderRadius: "8px", backgroundColor: "rgba(0,0,0,0.8)", color: "#fff", border: "none" }}
              />
              <Legend verticalAlign="bottom" height={36} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
};

// 2G. Statut des scans
export const ScanStatusChart = ({ data }: { data: Record<string, number> }) => {
  const chartData = Object.entries(data).map(([name, value]) => {
    // Map backend enums to nice labels
    let label = name;
    let colorKey = "Pending";
    if (name === "DONE") { label = "Terminés"; colorKey = "Done"; }
    if (name === "FAILED") { label = "Échoués"; colorKey = "Failed"; }
    if (name === "PROCESSING") { label = "En cours"; colorKey = "Processing"; }
    if (name === "PENDING") { label = "En attente"; colorKey = "Pending"; }
    return { name: label, value, fill: COLORS[colorKey as keyof typeof COLORS] };
  });

  return (
    <Card className="col-span-1 shadow-md hover:shadow-lg transition-all duration-300">
      <CardHeader>
        <CardTitle>Statut des Scans</CardTitle>
        <CardDescription>État d'avancement des analyses</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="h-[300px] w-full">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie data={chartData} cx="50%" cy="50%" innerRadius={60} outerRadius={100} paddingAngle={5} dataKey="value">
                {chartData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.fill} />
                ))}
              </Pie>
              <Tooltip
                contentStyle={{ borderRadius: "8px", backgroundColor: "rgba(0,0,0,0.8)", color: "#fff", border: "none" }}
              />
              <Legend verticalAlign="bottom" height={36} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
};
