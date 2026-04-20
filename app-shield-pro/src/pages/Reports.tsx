import { reports } from "@/lib/mock-data";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { FileBarChart, Download, Share2, Eye } from "lucide-react";

export default function Reports() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold font-heading flex items-center gap-2">
          <FileBarChart className="h-6 w-6 text-primary" />Rapports
        </h1>
        <p className="text-muted-foreground text-sm">Consultez et exportez vos rapports d'analyse</p>
      </div>

      <div className="space-y-4">
        {reports.map((r) => (
          <Card key={r.id} className="card-hover border-border/50">
            <CardContent className="p-5">
              <div className="flex items-center justify-between flex-wrap gap-4">
                <div className="flex items-center gap-4">
                  <div className="p-2.5 rounded-lg bg-primary/10">
                    <FileBarChart className="h-5 w-5 text-primary" />
                  </div>
                  <div>
                    <h3 className="font-semibold">{r.project}</h3>
                    <p className="text-xs text-muted-foreground">{r.type} · {r.date}</p>
                  </div>
                </div>
                <div className="flex items-center gap-4">
                  <div className="text-center">
                    <p className="text-lg font-bold">{r.vulnerabilities}</p>
                    <p className="text-[10px] text-muted-foreground">Vulns</p>
                  </div>
                  <div className="text-center">
                    <p className={`text-lg font-bold ${r.riskScore >= 80 ? "text-success" : r.riskScore >= 60 ? "text-severity-medium" : "text-destructive"}`}>
                      {r.riskScore}%
                    </p>
                    <p className="text-[10px] text-muted-foreground">Score</p>
                  </div>
                  <div className="flex gap-1">
                    <Button variant="ghost" size="icon"><Eye className="h-4 w-4" /></Button>
                    <Button variant="ghost" size="icon"><Download className="h-4 w-4" /></Button>
                    <Button variant="ghost" size="icon"><Share2 className="h-4 w-4" /></Button>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
