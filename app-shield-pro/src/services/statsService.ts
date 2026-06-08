import { getAuthHeaders } from "./adminService";

export interface OwaspCategoryDTO {
  categoryId: string;
  categoryName: string;
  count: number;
  percentage: number;
  criticalCount: number;
  highCount: number;
  mediumCount: number;
  lowCount: number;
  lastDetected: string | null;
}

export interface ScanTrendDTO {
  period: string;
  criticalCount: number;
  highCount: number;
  mediumCount: number;
  lowCount: number;
  totalScans: number;
}

export interface StatsWebDTO {
  totalVulnerabilities: number;
  criticalCount: number;
  highCount: number;
  mediumCount: number;
  lowCount: number;
  totalScans: number;
  mostVulnerableProject: string;
  lastScanDate: string | null;
  owaspCategories: OwaspCategoryDTO[];
  scanTrend: ScanTrendDTO[];
}

export interface StatsMobileDTO {
  totalVulnerabilities: number;
  criticalCount: number;
  highCount: number;
  mediumCount: number;
  lowCount: number;
  totalApkAnalyzed: number;
  averageRiskScore: number;
  mostVulnerableProject: string;
  lastScanDate: string | null;
  owaspCategories: OwaspCategoryDTO[];
  scanTrend: ScanTrendDTO[];
}

export interface DashboardStatsDTO {
  totalWebVulnerabilities: number;
  totalMobileVulnerabilities: number;
  totalWebScans: number;
  totalMobileScans: number;
  totalCritical: number;
  totalHigh: number;
  lastWebScanDate: string | null;
  lastMobileScanDate: string | null;
}

const handleResponse = async (res: Response) => {
  if (!res.ok) {
    const errText = await res.text();
    throw new Error(errText || "Erreur lors de la récupération des statistiques");
  }
  return res.json();
};

export const fetchDashboardStats = async (): Promise<DashboardStatsDTO> => {
  const res = await fetch("/api/stats/dashboard", {
    method: "GET",
    headers: getAuthHeaders(),
  });
  return handleResponse(res);
};

export const fetchWebStats = async (): Promise<StatsWebDTO> => {
  const res = await fetch("/api/stats/web", {
    method: "GET",
    headers: getAuthHeaders(),
  });
  return handleResponse(res);
};

export const fetchMobileStats = async (): Promise<StatsMobileDTO> => {
  const res = await fetch("/api/stats/mobile", {
    method: "GET",
    headers: getAuthHeaders(),
  });
  return handleResponse(res);
};
