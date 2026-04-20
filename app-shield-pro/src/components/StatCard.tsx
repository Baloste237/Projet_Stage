import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import type { LucideIcon } from "lucide-react";

interface StatCardProps {
  title: string;
  value: string | number;
  icon: LucideIcon;
  trend?: string;
  trendUp?: boolean;
  className?: string;
  iconClassName?: string;
}

export function StatCard({ title, value, icon: Icon, trend, trendUp, className, iconClassName }: StatCardProps) {
  return (
    <Card className={cn("card-hover border-border/50", className)}>
      <CardContent className="p-5">
        <div className="flex items-start justify-between">
          <div>
            <p className="text-sm text-muted-foreground">{title}</p>
            <p className="text-2xl font-bold mt-1 font-heading">{value}</p>
            {trend && (
              <p className={cn("text-xs mt-1", trendUp ? "text-success" : "text-destructive")}>
                {trend}
              </p>
            )}
          </div>
          <div className={cn("p-2.5 rounded-lg", iconClassName || "bg-primary/10")}>
            <Icon className={cn("h-5 w-5", iconClassName ? "" : "text-primary")} />
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
