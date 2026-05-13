import React, { useEffect, useState } from "react";
import { ShieldAlert, Activity, Users, AlertTriangle } from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { Navigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

const AdminMonitoringDashboard = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState({ totalLogs: 0, errorCount: 0, activeUsers: 0, scansInProgress: 0 });

  useEffect(() => {
    fetch("/api/admin/monitoring", {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("vulnscan-token")}`
      }
    })
      .then(res => res.json())
      .then(data => setStats(data))
      .catch(console.error);
  }, []);

  if (user?.role !== "ROLE_ADMIN") {
    return <Navigate to="/" replace />;
  }

  return (
    <div className="max-w-6xl mx-auto space-y-8 animate-in fade-in duration-500 pb-12">
      <h1 className="text-3xl font-bold tracking-tight flex items-center gap-3">
        <Activity className="h-8 w-8 text-primary" />
        Monitoring Dashboard
      </h1>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Users</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.activeUsers}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Scans In Progress</CardTitle>
            <Activity className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.scansInProgress}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Audit Logs</CardTitle>
            <ShieldAlert className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.totalLogs}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Recent Errors</CardTitle>
            <AlertTriangle className="h-4 w-4 text-red-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-red-500">{stats.errorCount}</div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default AdminMonitoringDashboard;
