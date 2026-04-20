import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

const severityConfig = {
  critical: { label: "Critique", className: "bg-severity-critical/15 text-severity-critical border-severity-critical/30" },
  high: { label: "Élevé", className: "bg-severity-high/15 text-severity-high border-severity-high/30" },
  medium: { label: "Moyen", className: "bg-severity-medium/15 text-severity-medium border-severity-medium/30" },
  low: { label: "Faible", className: "bg-severity-low/15 text-severity-low border-severity-low/30" },
};

export function SeverityBadge({ severity }: { severity: "critical" | "high" | "medium" | "low" }) {
  const config = severityConfig[severity];
  return (
    <Badge variant="outline" className={cn("font-mono text-xs", config.className)}>
      {config.label}
    </Badge>
  );
}
