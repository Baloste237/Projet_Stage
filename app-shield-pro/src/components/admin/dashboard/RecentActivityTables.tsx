import React from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { SeverityBadge } from "@/components/SeverityBadge";
import { format } from "date-fns";
import { fr } from "date-fns/locale";

// Composant pour les derniers Scans
export const RecentScansTable = ({ scans }: { scans: any[] }) => {
  return (
    <Card className="col-span-1 lg:col-span-2 shadow-md hover:shadow-lg transition-all duration-300 overflow-hidden">
      <CardHeader>
        <CardTitle>Derniers Scans Effectués</CardTitle>
        <CardDescription>Historique récent des analyses de sécurité</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Type</TableHead>
                <TableHead>Cible</TableHead>
                <TableHead>Statut</TableHead>
                <TableHead>Date</TableHead>
                <TableHead>Utilisateur</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {scans?.length > 0 ? (
                scans.map((scan, idx) => (
                  <TableRow key={idx}>
                    <TableCell className="font-medium">
                      {scan.scanType === "SAST" && scan.targetOs?.toLowerCase() === "web" ? "Web" : 
                       scan.scanType === "SAST" ? "Mobile" : scan.scanType}
                    </TableCell>
                    <TableCell className="max-w-[200px] truncate" title={scan.targetUrl || scan.projectName}>
                      {scan.targetUrl || scan.projectName || "N/A"}
                    </TableCell>
                    <TableCell>
                      <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                        scan.status === "DONE" ? "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400" :
                        scan.status === "FAILED" ? "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400" :
                        "bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400"
                      }`}>
                        {scan.status}
                      </span>
                    </TableCell>
                    <TableCell>
                      {scan.createdAt ? format(new Date(scan.createdAt), 'dd MMM yyyy HH:mm', { locale: fr }) : "N/A"}
                    </TableCell>
                    <TableCell>{scan.user?.userName || "Système"}</TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={5} className="text-center text-muted-foreground py-6">
                    Aucun scan récent trouvé.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>
      </CardContent>
    </Card>
  );
};

// Composant pour les dernières Vulnérabilités
export const RecentVulnsTable = ({ vulns }: { vulns: any[] }) => {
  return (
    <Card className="col-span-1 lg:col-span-2 shadow-md hover:shadow-lg transition-all duration-300 overflow-hidden">
      <CardHeader>
        <CardTitle>Dernières Vulnérabilités Détectées</CardTitle>
        <CardDescription>Flux en temps réel des failles critiques et majeures</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Titre</TableHead>
                <TableHead>Sévérité</TableHead>
                <TableHead>Type/CWE</TableHead>
                <TableHead>CVSS</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {vulns?.length > 0 ? (
                vulns.map((v, idx) => (
                  <TableRow key={idx}>
                    <TableCell className="max-w-[300px] truncate font-medium" title={v.name || v.description}>
                      {v.name || "Vulnérabilité"}
                    </TableCell>
                    <TableCell>
                      <SeverityBadge severity={v.niv_grav} />
                    </TableCell>
                    <TableCell>
                      {v.type || v.cweId || "N/A"}
                    </TableCell>
                    <TableCell>
                      {v.cvss ? (
                        <span className={`font-semibold ${v.cvss >= 7.0 ? 'text-red-500' : v.cvss >= 4.0 ? 'text-yellow-500' : 'text-blue-500'}`}>
                          {v.cvss.toFixed(1)}
                        </span>
                      ) : "N/A"}
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={4} className="text-center text-muted-foreground py-6">
                    Aucune vulnérabilité récente trouvée.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>
      </CardContent>
    </Card>
  );
};
