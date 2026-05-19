import React, { useEffect, useState } from "react";
import { ShieldAlert, Activity, Users, AlertTriangle, Bug, Smartphone, Globe, CheckCircle2, XCircle } from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { Navigate } from "react-router-dom";
import { StatCard } from "@/components/StatCard";
import { 
  VulnerabilitySeverityChart, 
  ScanEvolutionChart, 
  VulnsByTypeChart, 
  GlobalSecurityScoreChart, 
  WebVsMobileChart, 
  ScanStatusChart 
} from "@/components/admin/dashboard/DashboardCharts";
import { RecentScansTable, RecentVulnsTable } from "@/components/admin/dashboard/RecentActivityTables";
import { Skeleton } from "@/components/ui/skeleton";

const AdminMonitoringDashboard = () => {
  const { user } = useAuth();
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch("/api/admin/dashboard-stats", {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("vulnscan-token")}`
      }
    })
      .then(res => res.json())
      .then(stats => {
        setData(stats);
        setLoading(false);
      })
      .catch(err => {
        console.error("Failed to load dashboard stats", err);
        setLoading(false);
      });
  }, []);

  if (user?.role !== "ROLE_ADMIN") {
    return <Navigate to="/" replace />;
  }

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto space-y-8 animate-in fade-in duration-500 pb-12 p-4">
        <Skeleton className="h-12 w-1/3 mb-8" />
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Skeleton className="h-32" /><Skeleton className="h-32" />
          <Skeleton className="h-32" /><Skeleton className="h-32" />
        </div>
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <Skeleton className="h-[350px] col-span-2" />
          <Skeleton className="h-[350px]" />
        </div>
      </div>
    );
  }

  const { kpis, vulnBySeverity, scanEvolution, vulnsByType, scanStatus } = data || {};

  return (
    <div className="max-w-7xl mx-auto space-y-8 animate-in fade-in duration-500 pb-12">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center gap-3">
            <Activity className="h-8 w-8 text-primary" />
            Monitoring & Sécurité
          </h1>
          <p className="text-muted-foreground mt-2">Vue d'ensemble en temps réel de l'état de sécurité du système.</p>
        </div>
      </div>

      {/* 1. KPI Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard title="Score Global" value={`${kpis?.securityScore || 0}/100`} icon={ShieldAlert} iconClassName="bg-blue-500/10 text-blue-500" trend="Santé du système" trendUp={kpis?.securityScore > 50} />
        <StatCard title="Vulnérabilités Critiques" value={kpis?.criticalVulns || 0} icon={AlertTriangle} iconClassName="bg-red-500/10 text-red-500" trend="Attention immédiate requise" trendUp={false} />
        <StatCard title="Total Scans" value={kpis?.totalScans || 0} icon={Activity} iconClassName="bg-purple-500/10 text-purple-500" trend={`${kpis?.successScans || 0} réussis`} trendUp={true} />
        <StatCard title="Utilisateurs Actifs" value={kpis?.activeUsers || 0} icon={Users} iconClassName="bg-green-500/10 text-green-500" />
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard title="Scans Web" value={kpis?.webScans || 0} icon={Globe} className="bg-muted/30" />
        <StatCard title="Scans Mobile" value={kpis?.mobileScans || 0} icon={Smartphone} className="bg-muted/30" />
        <StatCard title="Failles High" value={kpis?.highVulns || 0} icon={Bug} className="bg-muted/30" iconClassName="text-orange-500" />
        <StatCard title="Scans Échoués" value={kpis?.failedScans || 0} icon={XCircle} className="bg-muted/30" iconClassName="text-red-500" />
      </div>

      {/* 2. Charts Row 1 */}
      <div className="grid gap-6 md:grid-cols-1 lg:grid-cols-3">
        <ScanEvolutionChart data={scanEvolution || []} />
        <GlobalSecurityScoreChart score={kpis?.securityScore || 0} />
      </div>

      {/* 3. Charts Row 2 */}
      <div className="grid gap-6 md:grid-cols-1 lg:grid-cols-3">
        <VulnerabilitySeverityChart data={vulnBySeverity || {}} />
        <VulnsByTypeChart data={vulnsByType || []} />
      </div>

      {/* 4. Charts Row 3 */}
      <div className="grid gap-6 md:grid-cols-1 lg:grid-cols-2">
        <WebVsMobileChart webScans={kpis?.webScans || 0} mobileScans={kpis?.mobileScans || 0} />
        <ScanStatusChart data={scanStatus || {}} />
      </div>

      {/* 5. Tables Row */}
      <div className="grid gap-6 md:grid-cols-1 lg:grid-cols-2">
        <RecentVulnsTable vulns={data?.recentVulns || []} />
        <RecentScansTable scans={data?.recentScans || []} />
      </div>

    </div>
  );
};

export default AdminMonitoringDashboard;
